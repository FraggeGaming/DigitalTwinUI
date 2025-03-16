import nibabel as nib
import numpy as np
import json
import sys
import multiprocessing
import os

def load_nifti_image(path):
    """Load a NIfTI image and return its data and voxel spacing as a tuple."""
    nii_img = nib.load(path)
    voxel_spacing = nii_img.header['pixdim'][1:4].tolist()  # [x, y, z] spacing
    return nii_img.get_fdata(), voxel_spacing

def generate_voxel_volume(path):
    img = load_nifti_image(path)  # Shape: (Z, Y, X)

    # Transpose to (X, Y, Z) so Kotlin can use .get(x,y,z) directly
    img = np.transpose(img, (2, 1, 0))  # Now: shape = (X, Y, Z)

    shape = img.shape
    voxel_volume = img.tolist()

    output_data = {
        "width": shape[0],    # X
        "height": shape[1],   # Y
        "depth": shape[2],    # Z
        "voxel_volume": voxel_volume
    }

    print(json.dumps(output_data))


def generate_voxel_volume_binary_json(path, output_dir):
    img, voxel_spacing = load_nifti_image(path)  # Shape: (Z, Y, X)
    img = np.transpose(img, (2, 1, 0))  # Transpose to (X, Y, Z)
    img = np.flip(img, axis=(0, 1))

    width, height, depth = img.shape
    base_name = os.path.basename(path)
    base_name = base_name.replace(".nii.gz", "").replace(".nii", "")

    npy_filename = f"{base_name}_voxel_volume.npy"
    npy_path = os.path.join(output_dir, npy_filename)
    np.save(npy_path, img)

    meta = {
        "width": width,    # X
        "height": height,   # Y
        "depth": depth,    # Z
        "voxel_spacing": voxel_spacing,  # [x, y, z]
        "npy_path": npy_path
    }

    print(json.dumps(meta))  # Output the metadata only

if __name__ == "__main__":
    multiprocessing.freeze_support()

    if len(sys.argv) < 3:
        print("Usage: python script.py <path_to_nifti_file> <output_dir>")
        sys.exit(1)

    nifti_path = sys.argv[1]
    output_dir = sys.argv[2]
    generate_voxel_volume_binary_json(nifti_path, output_dir)

