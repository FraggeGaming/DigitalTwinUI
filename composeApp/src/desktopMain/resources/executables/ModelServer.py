import os
from flask import Flask, request, send_file
import nibabel as nib
import json
import subprocess
import csv

app = Flask(__name__)

@app.route("/process", methods=["POST"])
def process_nifti():
    uploaded_file = request.files.get("file")
    metadata_json = request.form.get("metadata")

    if not uploaded_file or not metadata_json:
        return "Missing file or metadata", 400

    metadata = json.loads(metadata_json)
    print("Received metadata:", metadata)

    try:
        # Ensure 'uploads' folder exists
        os.makedirs("uploads", exist_ok=True)

        # Save uploaded file to disk
        upload_path = os.path.join("uploads", f"{metadata['title']}_uploaded.nii.gz")
        uploaded_file.save(upload_path)
        print(f"Saved uploaded file to: {upload_path}")

        # run model
        csv_path = "upload_log.csv"
        with open(csv_path, mode="a", newline="") as file:
            writer = csv.writer(file)
            writer.writerow(["CTres.nii.gz"])
        runModel(upload_path)

        output_path = os.path.abspath("C:/Coding/Python/Nifti/Server/output/generated_image_1.nii.gz")

        if os.path.exists(csv_path):
            os.remove(csv_path)

        if not os.path.exists(output_path):
            return "Output file not found", 500

        # Load the model's output
        if not os.path.exists(output_path):
            return "Output file not found", 500

        # ✅ Denormalize the data
        img = nib.load(output_path)
        data = img.get_fdata()
        denormalized_data = data * 20  # scale up from [0, 1] to [0, 20]

        # ✅ Save the denormalized image (overwrite or rename)
        denorm_img = nib.Nifti1Image(denormalized_data, img.affine, img.header)
        nib.save(denorm_img, output_path)

        print(f"Returning output file: {output_path}")
        return send_file(output_path, mimetype="application/octet-stream", as_attachment=True)

    except Exception as e:
        return f"Error: {e}", 500

def runModel(upload_path):
    data_path = 'C:/Coding/Python/Nifti/Server/codice_curriculum/'
    # Componi il comando da eseguire
    name = 'SORTED+GROUPED_district+WARMUP_1'
    which_epoch = 'BEST_final_200'

    test_district = 'legs'
    command = f"python test_interface.py --gpu_ids -1 --dataroot {data_path} --test_district {test_district} --which_epoch {which_epoch} --name {name}" # test_stitching

    try:
        subprocess.run(command, shell=True, check=True)
    except subprocess.CalledProcessError as e:
        print(f"Errore durante l'esecuzione del comando: {e}")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
    #runModel()