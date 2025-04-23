import os
import sys
import json
import csv
import subprocess
import nibabel as nib
from flask import Flask, request, send_file, jsonify
from dataclasses import dataclass, asdict


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
LOG_FILE = os.path.join(BASE_DIR, "upload_log.csv")
OUTPUT_PATH = os.path.join(OUTPUT_DIR, "generated_image_1.nii.gz")


# Ensure necessary folders exist
os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(UPLOAD_DIR, exist_ok=True)


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
        upload_path = os.path.join(UPLOAD_DIR, f"{metadata['title']}_uploaded.nii.gz")
        uploaded_file.save(upload_path)
        print(f"Saved uploaded file to: {upload_path}")




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
        if model_id == 1:
            runModel(upload_path)
        if model_id == 2:
            runModel(upload_path)




        if not os.path.exists(OUTPUT_PATH):
            return "Output file not found", 500


        # Denormalize output
        img = nib.load(OUTPUT_PATH)
        data = img.get_fdata()
        denormalized_data = data * 20
        denorm_img = nib.Nifti1Image(denormalized_data, img.affine, img.header)
        nib.save(denorm_img, OUTPUT_PATH)


        print(f"Returning output file: {OUTPUT_PATH}")
        return send_file(OUTPUT_PATH, mimetype="application/octet-stream", as_attachment=True)


    except Exception as e:
        return f"Error: {e}", 500



def runModel(upload_path):
    name = 'SORTED+GROUPED_district+WARMUP_1'
    which_epoch = 'BEST_final_200'
    test_district = 'legs'


    command = (
        f'python "{TEST_SCRIPT}" '
        f'--gpu_ids -1 '
        f'--dataroot "{DATA_PATH}" '
        f'--test_district {test_district} '
        f'--which_epoch {which_epoch} '
        f'--name "{name}" '
        f'--checkpoints_dir "{CHECKPOINTS_DIR}"'
    )
    print(f"Running model command:\n{command}")
    try:
        subprocess.run(command, shell=True, check=True)
    except subprocess.CalledProcessError as e:
        print(f"Error running command: {e}")


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)



