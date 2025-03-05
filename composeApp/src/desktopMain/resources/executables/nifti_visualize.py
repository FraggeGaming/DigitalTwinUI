import nibabel as nib
import numpy as np
import matplotlib.pyplot as plt
import io
import base64
import sys
import json

def load_nifti_image(path):
    """ Load a NIfTI image and return its data as a NumPy array. """
    img = nib.load(path).get_fdata()
    return img

def slice_nifti(img):
    """ Extract slices from the NIfTI image in Axial, Coronal, and Sagittal views. """
    axial_slices = [img[:, :, i] for i in range(img.shape[2])]
    coronal_slices = [img[:, i, :] for i in range(img.shape[1])]
    sagittal_slices = [img[i, :, :] for i in range(img.shape[0])]
    return axial_slices, coronal_slices, sagittal_slices

def convert_to_base64(image_array):
    """ Convert a NumPy image array to a Base64-encoded PNG string. """
    fig, ax = plt.subplots(figsize=(2, 2))
    ax.imshow(np.rot90(image_array), cmap="gray")
    ax.axis("off")

    buf = io.BytesIO()
    plt.savefig(buf, format="png", bbox_inches="tight", pad_inches=0)
    plt.close(fig)
    
    encoded_image = base64.b64encode(buf.getvalue()).decode("utf-8")
    return encoded_image

def generate_images(path):
    """ Load NIfTI, generate slices, and return Base64-encoded images in JSON format. """
    img = load_nifti_image(path)
    axial_slices, coronal_slices, sagittal_slices = slice_nifti(img)

    axial_encoded = [convert_to_base64(slice) for slice in axial_slices]
    coronal_encoded = [convert_to_base64(slice) for slice in coronal_slices]
    sagittal_encoded = [convert_to_base64(slice) for slice in sagittal_slices]

    output_data = {
        "axial": axial_encoded,
        "coronal": coronal_encoded,
        "sagittal": sagittal_encoded
    }

    print(json.dumps(output_data))  # Output JSON to stdout for Kotlin to read

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python main.py <path_to_nifti_file>")
        sys.exit(1)

    nifti_path = sys.argv[1]
    generate_images(nifti_path)
