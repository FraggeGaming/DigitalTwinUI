import os
from flask import Flask, request, send_file
import nibabel as nib
import json

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

        # Load and run model
        img = nib.load(upload_path)
        data = img.get_fdata()
        processed_img = nib.Nifti1Image(data, img.affine)



        # Save output to same folder
        output_path = os.path.join("uploads", f"{metadata['title']}_processed.nii.gz")
        nib.save(processed_img, output_path)
        print(f"Saved processed file to: {output_path}")

        return send_file(output_path, mimetype="application/octet-stream", as_attachment=True)

    except Exception as e:
        return f"Error: {e}", 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)