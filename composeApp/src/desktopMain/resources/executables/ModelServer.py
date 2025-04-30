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

#Dataclass for providing the models
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


#the running processes and its lock is for keeping track of the process so it can be discarded if the user wants to cancel
running_processes = {}  #save the running process that runs the model
lock = threading.Lock() #lock for saving the process

progress_state = {}  #Dict for accessing the latest progress update for each running job_id
progress_lock = threading.Lock()

@app.route("/modalities", methods=["GET"])
def get_modalities():
    try:
        modalities = sorted(set(model.inputModality for model in AVAILABLE_MODELS))
        return jsonify(modalities)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/regions", methods=["GET"])
def get_regions():
    try:
        regions = sorted(set(model.region for model in AVAILABLE_MODELS))
        return jsonify(regions)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

#Function to kill the subprocess
def kill_subprocess(process):
    if platform.system() == "Windows":
        print("[Server] Sending CTRL_BREAK_EVENT to Windows process")
        process.send_signal(signal.CTRL_BREAK_EVENT)
    else:
        print("[Server] Killing process group on Unix")
        os.killpg(os.getpgid(process.pid), signal.SIGKILL)
    process.wait()

#Get call to fetch the compatible models for the UI based on region, modality etc. Returns the compatible models back to the UI
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


#function for reading the process stdout to get progress update from the inference. Saving the progress inside progress_state dict for the said job_id
def read_progress(job_id, process):
    marker = "::PROGRESS::"
    for raw_line in process.stdout:
        line = raw_line.strip()

        # Skip blank lines
        if not line:
            continue

        if marker in line:
            try:
                _, payload = line.split(marker, 1)
                data = json.loads(payload.strip())
                with progress_lock:
                    progress_state[job_id] = data
            except Exception as e:
                print(f"[{job_id}] Failed to parse progress: {e}")

        print(f"[{job_id} LOG]", line)


#Fetches the progress json gathered from the stdout of the inference
@app.route("/progress/<job_id>")
def get_progress(job_id):
    with progress_lock:
        progress = progress_state.get(job_id)
    if not progress:
        return '', 204
    return jsonify(progress)

#Fetches the nifti based on the job_id. The nifti needs to be saved as {job_id}.nii.gz in order for the function to find it
@app.route("/download/<job_id>", methods=["GET"])
def download_output(job_id):

    nifti_output = os.path.join(OUTPUT_DIR, f"{job_id}.nii.gz")
    img = nib.load(nifti_output)
    data = img.get_fdata()
    denormalized_data = data * 20
    denorm_img = nib.Nifti1Image(denormalized_data, img.affine, img.header)
    nib.save(denorm_img, nifti_output)

    remove_uploaded_nifti(job_id)
    if not os.path.exists(nifti_output):
        return "Output file not ready yet.", 404

    return send_file(nifti_output, mimetype="application/octet-stream", as_attachment=True)


#function to remove the uploaded nifti file. Gets called after the user has downloaded the generated nifti
def remove_uploaded_nifti(job_id):
    path = os.path.join(UPLOAD_DIR, f"{job_id}.nii.gz")
    if os.path.exists(path):
        os.remove(path)
        print(f"Deleted progress file: {path}")
    else:
        print(f"Progress file not found: {path}")



#Cancels the job if its running.
@app.route("/cancel/<job_id>", methods=["POST"])
def cancel_job(job_id):
    print(f"[Server] Trying to cancel job: '{job_id}'")
    print(f"[Server] Available running jobs: {list(running_processes.keys())}")
    with lock:
        process = running_processes.get(job_id)
        if process:
            kill_subprocess(process)
            del running_processes[job_id]
            remove_uploaded_nifti(job_id)

            return jsonify({"status": "Cancelled"}), 200
        else:
            return jsonify({"error": "No such job running"}), 404


#Starts a subprocess and runs the model inference for the said model and the user added nifti
@app.route("/process", methods=["POST"])
def process_nifti():
    uploaded_file = request.files.get("file")
    metadata_json = request.form.get("metadata")

    if not uploaded_file or not metadata_json:
        return "Missing file or metadata", 400

    metadata = json.loads(metadata_json)
    print("Received metadata:", metadata)


    # Save uploaded NIfTI file
    upload_path = os.path.join(UPLOAD_DIR, f"{metadata['title']}.nii.gz")
    uploaded_file.save(upload_path)
    print(f"Saved uploaded file to: {upload_path}")

    job_id = metadata['title']
    mod = metadata['modality']
    region = metadata['region']
    model = metadata['model']
    model_id = model.get("id")

    name = 'SORTED+GROUPED_district+WARMUP_1'
    which_epoch = 'BEST_final_200'
    test_district = region


    command = [
        "python", TEST_SCRIPT,
        "--gpu_ids", "-1",
        "--json_id", job_id,
        "--dataroot", DATA_PATH,
        "--test_district", test_district,
        "--which_epoch", str(which_epoch),
        "--name", name,
        "--out_path", OUTPUT_DIR,
        "--upload_dir", upload_path,
        "--checkpoints_dir", CHECKPOINTS_DIR,
    ]

    print(f"Running model command:\n{command}")
    try:
        process = subprocess.Popen(
            command,
            shell=False,
            stdout=subprocess.PIPE,    #capture stdout so we can read it from the parent process
            stderr=subprocess.STDOUT,
            text=True,
            creationflags=subprocess.CREATE_NEW_PROCESS_GROUP if platform.system() == "Windows" else 0,
            preexec_fn=os.setsid if platform.system() != "Windows" else None,
        )

        threading.Thread(
            target=read_progress,
            args=(job_id, process),
            daemon=True
        ).start()

        running_processes[job_id] = process
        return jsonify({"status": "Running model"}), 200

    except Exception as e:
        print(f"Error running command: {e}")
        return f"Error starting model: {e}", 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)