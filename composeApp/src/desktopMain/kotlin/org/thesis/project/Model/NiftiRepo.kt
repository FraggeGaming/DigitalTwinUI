package org.thesis.project.Model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.nd4j.linalg.api.ndarray.INDArray
import java.io.File

/**
 * Class that stores the nifti images, and is responsible for the volume and their slices
 * */
class NiftiRepo(val imageController: ImageController, savedMappingPath: File) {
    //Stores the niftiData by Filename
    private val _niftiImages = MutableStateFlow<Map<String, NiftiData>>(emptyMap())
    val niftiImages: StateFlow<Map<String, NiftiData>> = _niftiImages
    val jsonMapper = JsonMappingController(savedMappingPath)

    fun store(id: String, data: NiftiData) {
        _niftiImages.update { currentMap ->
            currentMap + (id to data)
            //filename to NiftiData
        }
        imageController.updateSelectedData(id, true)


    }
    fun get(id: String): NiftiData? {
        val niftiData = _niftiImages.value[id] ?: return null
        return niftiData
    }

    fun delete(id: String) {
        imageController.removeSelectedData(id)
        _niftiImages.update { currentMap ->
            val nifti = currentMap[id]
            nifti?.clearData()
            currentMap - id
        }
    }

    fun getAxialSlice(z: Int, nifti: NiftiData): INDArray {
        return nifti.voxelVolume_ind.tensorAlongDimension(z.toLong(), 1, 2)
    }


    fun getCoronalSlice(y: Int, nifti: NiftiData): INDArray {
        return nifti.voxelVolume_ind.tensorAlongDimension(y.toLong(), 0, 1)
    }

    fun getSagittalSlice(x: Int, nifti: NiftiData): INDArray {
        return nifti.voxelVolume_ind.tensorAlongDimension(x.toLong(), 0, 2)
    }


    fun getSliceInd(view: NiftiView, images: NiftiData, slice: Int): Triple<INDArray, Float, String> {
        val spacing = images.voxelSpacing

        return when (view) {
            NiftiView.AXIAL -> {
                Triple(getAxialSlice(slice, images), spacing[2], images.modality)
            }

            NiftiView.CORONAL -> {
                Triple(getCoronalSlice(slice, images), spacing[1], images.modality)
            }

            NiftiView.SAGITTAL -> {
                Triple(getSagittalSlice(slice, images), spacing[0], images.modality)
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
        val mappings = getFileMapping(key)

        if (mappings != null) {
            mappings.first.forEach { data ->
                if (imageController.selectedData.value.contains(data))
                    imageController.removeSelectedData(data)
                delete(data)
            }

            mappings.second.forEach { data ->
                delete(data)
            }
        }

        _fileMapping.update { currentMap ->
            currentMap - key
        }

        System.gc()
    }

    //Check if a key exists in the mapping
    fun hasFileMapping(key: String): Boolean {
        return _fileMapping.value.containsKey(key)
    }

    fun getNameFromNiftiId(id: String): String {
        //print("Nifti with id: ${id}: ${get(id)}")
        return get(id)?.name ?: "NaN"
    }


    fun updateFileMappingInput(key: String, newInput: List<String>) {
        _fileMapping.update { currentMap ->
            val existingOutput = currentMap[key]?.second ?: emptyList()
            currentMap + (key to (newInput to existingOutput))
        }
    }

    fun updateFileMappingOutput(key: String, newOutput: List<String>) {
        _fileMapping.update { currentMap ->
            val existingInput = currentMap[key]?.first ?: emptyList()
            currentMap + (key to (existingInput to newOutput))
        }
    }

    fun fullDelete(mapping: FileMappingFull){
        jsonMapper.removeMapping(mapping)
        if (hasFileMapping(mapping.title)){
            removeFileMapping(mapping.title)
        }
    }
}