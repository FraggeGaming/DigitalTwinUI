import nibabel as nib
import numpy as np
import cv2
import base64
import io
import json
import sys
import multiprocessing
import os

def load_nifti_image(path):
    """Load a NIfTI image and return its data as a NumPy array."""
    img = nib.load(path).get_fdata()
    return img

def slice_nifti(img):
    """ Extract slices efficiently using NumPy indexing. """
    axial_slices = np.moveaxis(img, 2, 0)
    coronal_slices = np.moveaxis(img, 1, 0)
    sagittal_slices = np.moveaxis(img, 0, 0)
    return axial_slices, coronal_slices, sagittal_slices

def convert_to_base64(image_array):
    """ Convert a NumPy image array to a Base64-encoded PNG string using OpenCV. """
    normalized_img = cv2.normalize(image_array, None, 0, 255, cv2.NORM_MINMAX)
    img_uint8 = np.uint8(normalized_img)

    _, buffer = cv2.imencode(".png", img_uint8)
    encoded_image = base64.b64encode(buffer).decode("utf-8")

    return encoded_image

def process_slices(slice_list):
    """ Process image slices in parallel using multiprocessing (Only in Python mode). """
    if getattr(sys, 'frozen', False):  # ✅ Detect if running as an .exe
        return [convert_to_base64(slice) for slice in slice_list]  # **Run in single-threaded mode**
    else:
        with multiprocessing.Pool() as pool:
            return pool.map(convert_to_base64, slice_list)

def generate_images(path):
    """ Load NIfTI, generate slices, and return Base64-encoded images in JSON format. """
    img = load_nifti_image(path)
    axial_slices, coronal_slices, sagittal_slices = slice_nifti(img)

    axial_encoded = process_slices(axial_slices)
    coronal_encoded = process_slices(coronal_slices)
    sagittal_encoded = process_slices(sagittal_slices)

    output_data = {
        "axial": axial_encoded,
        "coronal": coronal_encoded,
        "sagittal": sagittal_encoded
    }

    print(json.dumps(output_data))  # Output JSON to stdout for Kotlin to read

# ✅ Fix multiprocessing issue when using PyInstaller
if __name__ == "__main__":
    multiprocessing.freeze_support()

    if len(sys.argv) < 2:
        print("Usage: python nifti_visualize.py <path_to_nifti_file>")
        sys.exit(1)

    nifti_path = sys.argv[1]
    generate_images(nifti_path)
