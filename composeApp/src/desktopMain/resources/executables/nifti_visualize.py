import nibabel as nib
import numpy as np
import cv2
import base64
import json
import multiprocessing
import sys

def load_nifti_image(path):
    """Load a NIfTI image and return its data as a NumPy array."""
    img = nib.load(path).get_fdata()  # Load raw voxel data
    return img

def slice_nifti(img):
    """Extract slices efficiently using NumPy indexing."""
    axial_slices = np.moveaxis(img, 2, 0)
    coronal_slices = np.moveaxis(img, 1, 0)
    sagittal_slices = np.moveaxis(img, 0, 0)
    return axial_slices, coronal_slices, sagittal_slices

def convert_to_base64(image_array):
    """Convert a NumPy image array to a Base64-encoded PNG string using OpenCV."""
    normalized_img = cv2.normalize(image_array, None, 0, 255, cv2.NORM_MINMAX)
    img_uint8 = np.uint8(normalized_img)
    #img_uint8 = cv2.rotate(img_uint8, cv2.ROTATE_90_CLOCKWISE)  # Adjust rotation if needed

    _, buffer = cv2.imencode(".png", img_uint8)
    encoded_image = base64.b64encode(buffer).decode("utf-8")
    return encoded_image

def process_slices(slice_list):
    """Process image slices in parallel using multiprocessing."""
    if getattr(sys, 'frozen', False):
        return [convert_to_base64(slice) for slice in slice_list]  # Single-threaded mode for .exe
    else:
        with multiprocessing.Pool() as pool:
            return pool.map(convert_to_base64, slice_list)


def extract_voxel_data(slice_list):
    """Ensure the voxel data remains in 3D (depth -> rows -> columns)."""
    return [[[float(v) for v in row] for row in slice] for slice in slice_list]


def get_nifti_shape(nifti_path):
    """Load a NIfTI file and return its shape (width, height, depth)."""
    img = nib.load(nifti_path)
    return img.shape  # (width, height, depth)

def generate_images(path):
    """Load NIfTI, generate slices, and output JSON with Base64 images and raw voxel data."""
    img = load_nifti_image(path)
    width, height, depth = img.shape
    axial_slices, coronal_slices, sagittal_slices = slice_nifti(img)

    axial_encoded = process_slices(axial_slices)
    coronal_encoded = process_slices(coronal_slices)
    sagittal_encoded = process_slices(sagittal_slices)

    axial_voxels = extract_voxel_data(axial_slices)
    coronal_voxels = extract_voxel_data(coronal_slices)
    sagittal_voxels = extract_voxel_data(sagittal_slices)

    output_data = {
        "width": width,         # You may include width if needed
        "height": height,       # Similarly, height
        "depth": depth,         # And depth (number of slices)
        "axial": axial_encoded,
        "axial_voxels": axial_voxels,
        "coronal": coronal_encoded,
        "coronal_voxels": coronal_voxels,
        "sagittal": sagittal_encoded,
        "sagittal_voxels": sagittal_voxels
    }

    print(json.dumps(output_data))  # Output JSON to stdout for Kotlin to read

if __name__ == "__main__":
    multiprocessing.freeze_support()

    if len(sys.argv) < 2:
        print("Usage: python nifti_visualize.py <path_to_nifti_file>")
        sys.exit(1)

    nifti_path = sys.argv[1]
    generate_images(nifti_path)
