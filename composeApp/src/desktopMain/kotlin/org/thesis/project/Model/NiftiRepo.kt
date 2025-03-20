package org.thesis.project.Model

import kotlinx.coroutines.flow.*

class NiftiRepo {
    //Stores the niftiData by Filename
    private val _niftiImages = MutableStateFlow<Map<String, NiftiData>>(emptyMap())
    val niftiImages: StateFlow<Map<String, NiftiData>> = _niftiImages


    fun store(filename: String, data: NiftiData) {
        _niftiImages.update { currentMap ->
            currentMap + (filename to data)
            //filename to NiftiData
        }
    }

    fun get(filename: String): NiftiData? {
        val niftiData = _niftiImages.value[filename] ?: return null
        return niftiData
    }

    fun getSlicesFromVolume(view: NiftiView, filename: String): Triple<Array<Array<Array<Float>>>, Float, String> {
        val images = get(filename) ?: return Triple(emptyArray(), 1f, "")
        val spacing = images.voxelSpacing

        //because of transpose when parsing nifti, (Z, Y, X) → (X, Y, Z), but we don't transpose spacing
        return when (view) {
            NiftiView.AXIAL -> {
                Triple(images.voxelVolume, spacing[2], images.modality)
            }

            NiftiView.CORONAL -> {
                Triple(images.coronalVoxelSlices, spacing[1], images.modality)
            }

            NiftiView.SAGITTAL -> {
                Triple( images.sagittalVoxelSlices, spacing[0], images.modality)
            }
        }
    }

    //Connects multiple filenames together, ex: Patient_1 : List("CT_img1", "PET_real"), List("Syntet_PET")
    private val _fileMapping = MutableStateFlow<Map<String, Pair<List<String>, List<String>>>>(emptyMap())
    val fileMapping: StateFlow<Map<String, Pair<List<String>, List<String>>>> = _fileMapping



    //Add or update an entry in the mapping
    fun addFileMapping(key: String, firstList: List<String>, secondList: List<String>) {
        _fileMapping.update { currentMap ->
            currentMap + (key to Pair(firstList, secondList))
        }
    }

    //Retrieve a specific mapping by key
    fun getFileMapping(key: String): Pair<List<String>, List<String>>? {
        return _fileMapping.value[key]
    }

    //Get all keys (filenames)
    fun getFileMappingKeys(): List<String> {
        return _fileMapping.value.keys.toList()
    }

    //Remove an entry from the mapping
    fun removeFileMapping(key: String) {
        _fileMapping.update { currentMap ->
            currentMap - key
        }
    }

    //Check if a key exists in the mapping
    fun hasFileMapping(key: String): Boolean {
        return _fileMapping.value.containsKey(key)
    }

}