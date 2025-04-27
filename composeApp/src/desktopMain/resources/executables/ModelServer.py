import os
import sys
import json
import csv
import subprocess
import nibabel as nib
from flask import Flask, request, send_file, jsonify
from dataclasses import dataclass, asdict
from subprocess import Popen
import threading
import platform
import signal
app = Flask(__name__)

@dataclass
class AIModel:
    id: int
    title: str
    description: str
    inputModality: str
    outputModality: str
    region: str

AVAILABLE_MODELS = [
    AIModel(
        id=1,
        title="CT-to-PET (Brain)",
        description="Converts brain CT scans to synthetic PET images.",
        inputModality="CT",
        outputModality="PET",
        region="Brain"
    ),
    AIModel(
        id=2,
        title="CT-to-PET (Total Body)",
        description="Converts full-body CT scans to PET.",
        inputModality="CT",
        outputModality="PET",
        region="Total Body"
    ),
]

# Base directory (handles normal + PyInstaller executable)
BASE_DIR = getattr(sys, '_MEIPASS', os.path.abspath(os.path.dirname(__file__)))


# Paths relative to the base directory
DATA_PATH = os.path.join(BASE_DIR, "codice_curriculum")
TEST_SCRIPT = os.path.join(DATA_PATH, "test_interface.py")
CHECKPOINTS_DIR = os.path.join(BASE_DIR, "checkpoints")
OUTPUT_DIR = os.path.join(BASE_DIR, "output")
UPLOAD_DIR = os.path.join(BASE_DIR, "uploads")
PROGRESS_DIR = os.path.join(BASE_DIR, "progress_files")


# Ensure necessary folders exist
os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(UPLOAD_DIR, exist_ok=True)
os.makedirs(PROGRESS_DIR, exist_ok=True)
running_processes = {}  #save the running process that runs the model
lock = threading.Lock() #lock for saving the process

def start_subprocess(command, env):
    if platform.system() == "Windows":
        process = subprocess.Popen(
            command,
            shell=True,
            env=env,
            stdout=sys.stdout,
            stderr=sys.stderr,
            creationflags=subprocess.CREATE_NEW_PROCESS_GROUP  # Windows
        )
    else:
        process = subprocess.Popen(
            command,
            shell=True,
            env=env,
            stdout=sys.stdout,
            stderr=sys.stderr,
            preexec_fn=os.setsid  # Linux/macOS
        )
    return process

def kill_subprocess(process):
    if platform.system() == "Windows":
        print("[Server] Sending CTRL_BREAK_EVENT to Windows process")
        process.send_signal(signal.CTRL_BREAK_EVENT)
    else:
        print("[Server] Killing process group on Unix")
        os.killpg(os.getpgid(process.pid), signal.SIGKILL)
    process.wait()


@app.route("/getmodels", methods=["POST"])
def get_models():
    print("Received POST request to /getmodels")

    try:
        data = request.get_json()
        print("Raw JSON received:", data)

        if not data:
            print("No JSON data received.")
            return jsonify({"error": "Missing JSON data"}), 400

        modality = data.get("modality")
        region = data.get("region")

        print(f"Filtering models for modality='{modality}', region='{region}'")

        if not modality or not region:
            print("Missing modality or region in the request.")
            return jsonify({"error": "Missing modality or region"}), 400

        filtered_models = [
            asdict(model)
            for model in AVAILABLE_MODELS
            if model.inputModality == modality and model.region == region
        ]

        print(f"Found {len(filtered_models)} matching models.")
        for model in filtered_models:
            print("Matched model:", model)

        return jsonify(filtered_models)

    except Exception as e:
        print("Exception occurred while handling /getmodels:", str(e))
        return jsonify({"error": str(e)}), 500


@app.route("/progress/<job_id>", methods=["GET"])
def get_progress(job_id):
    path = os.path.join(PROGRESS_DIR, f"progress_{job_id}.json")
    if not os.path.exists(path):
        #If the file does not exist, just return a "waiting" progress status
        return jsonify({
            "step": 0,
            "total": 1,  # prevent divide-by-zero
            "percent": 0.0,
            "job_id": job_id,
            "finished": False
        })

    try:
        with open(path, 'r') as f:
            progress = json.load(f)
        return jsonify(progress)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/download/<job_id>", methods=["GET"])
def download_output(job_id):

    nifti_output = os.path.join(OUTPUT_DIR, f"{job_id}.nii.gz")
    img = nib.load(nifti_output)
    data = img.get_fdata()
    denormalized_data = data * 20
    denorm_img = nib.Nifti1Image(denormalized_data, img.affine, img.header)
    nib.save(denorm_img, nifti_output)

    remove_progress_file(job_id)
    remove_uploaded_nifti(job_id)
    if not os.path.exists(nifti_output):
        return "Output file not ready yet.", 404

    return send_file(nifti_output, mimetype="application/octet-stream", as_attachment=True)

def remove_progress_file(job_id):
    path = os.path.join(PROGRESS_DIR, f"progress_{job_id}.json")
    if os.path.exists(path):
        os.remove(path)
        print(f"Deleted progress file: {path}")
    else:
        print(f"Progress file not found: {path}")

def remove_uploaded_nifti(job_id):
    path = os.path.join(UPLOAD_DIR, f"{job_id}.nii.gz")
    if os.path.exists(path):
        os.remove(path)
        print(f"Deleted progress file: {path}")
    else:
        print(f"Progress file not found: {path}")

@app.route("/cancel/<job_id>", methods=["POST"])
def cancel_job(job_id):
    print(f"[Server] Trying to cancel job: '{job_id}'")
    print(f"[Server] Available running jobs: {list(running_processes.keys())}")
    with lock:
        process = running_processes.get(job_id)
        if process:
            kill_subprocess(process)
            del running_processes[job_id]
            remove_progress_file(job_id)
            remove_uploaded_nifti(job_id)

            return jsonify({"status": "Cancelled"}), 200
        else:
            return jsonify({"error": "No such job running"}), 404

@app.route("/process", methods=["POST"])
def process_nifti():
    uploaded_file = request.files.get("file")
    metadata_json = request.form.get("metadata")


    if not uploaded_file or not metadata_json:
        return "Missing file or metadata", 400



    metadata = json.loads(metadata_json)
    print("Received metadata:", metadata)


    try:
        # Save uploaded NIfTI file
        upload_path = os.path.join(UPLOAD_DIR, f"{metadata['title']}.nii.gz")
        uploaded_file.save(upload_path)
        print(f"Saved uploaded file to: {upload_path}")

        job_id = metadata['title']


        print(f"Upload path to be written into CSV: {upload_path}")
        print(f"Does file exist? {os.path.exists(upload_path)}")


        # Create correct test.csv file in the same folder that `--dataroot` points to
        test_csv_path = os.path.join(DATA_PATH, "test.csv")
        with open(test_csv_path, mode="w") as f:
            f.write(upload_path.replace("\\", "/") + "\n")

        mod = metadata['modality']
        region = metadata['region']
        model = metadata['model']

        model_id = model.get("id")

        #run different models based on the id (specified at the top of the script)
        #if model_id == 1:
        #   runModel(upload_path)
        #if model_id == 2:
        #    runModel(upload_path)

        name = 'SORTED+GROUPED_district+WARMUP_1'
        which_epoch = 'BEST_final_200'
        test_district = region

        # Launch subprocess with env var
        env = os.environ.copy()

        env["PROGRESS_FILE"] = os.path.join(PROGRESS_DIR, f"progress_{job_id}.json")
        command = (
            f'python "{TEST_SCRIPT}" '
            f'--gpu_ids -1 '
            f'--json_id "{job_id}" '
            f'--dataroot "{DATA_PATH}" '
            f'--test_district "{test_district}" '
            f'--which_epoch {which_epoch} '
            f'--name "{name}" '
            f'--out_path "{OUTPUT_DIR}" '
            f'--upload_dir "{upload_path}" '
            f'--checkpoints_dir "{CHECKPOINTS_DIR}"'
        )

        print(f"Running model command:\n{command}")
        try:
            process = start_subprocess(command, env)
            with lock:
                running_processes[job_id] = process

        except Exception as e:
            print(f"Error running command: {e}")
            return f"Error starting model: {e}", 500


        print("We Got HERE atleast------------")


        return jsonify({"status": "Running model"}), 200


        #return send_file(nifti_output, mimetype="application/octet-stream", as_attachment=True)


    except Exception as e:
        print("what happened here?")
        return f"Error: {e}", 500




import tempfile
import threading
import time




if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)



