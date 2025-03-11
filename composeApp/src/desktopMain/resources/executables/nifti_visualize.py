import nibabel as nib
import numpy as np
import json
import sys
import multiprocessing

def load_nifti_image(path):
    """Load a NIfTI image and return its data as a NumPy array."""
    return nib.load(path).get_fdata()

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


if __name__ == "__main__":
    multiprocessing.freeze_support()

    if len(sys.argv) < 2:
        print("Usage: python nifti_voxel_export.py <path_to_nifti_file>")
        sys.exit(1)

    nifti_path = sys.argv[1]
    generate_voxel_volume(nifti_path)
