package org.thesis.project.Model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class NiftiRepo {
    //Stores the niftiData by Filename
    private val _niftiImages = MutableStateFlow<Map<String, NiftiData>>(emptyMap())
    val niftiImages: StateFlow<Map<String, NiftiData>> = _niftiImages
    val jsonMapper = JsonMappingController()

    fun store(id: String, data: NiftiData) {
        _niftiImages.update { currentMap ->
            currentMap + (id to data)
            //filename to NiftiData
        }
    }
    fun get(id: String): NiftiData? {
        val niftiData = _niftiImages.value[id] ?: return null
        return niftiData
    }

    fun delete(id: String) {
        _niftiImages.update { currentMap ->
            currentMap - id
        }
    }

    fun getSlicesFromVolume(view: NiftiView, images: NiftiData): Triple<Array<Array<Array<Float>>>, Float, String> {
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
                Triple(images.sagittalVoxelSlices, spacing[0], images.modality)
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
            println("Deleting ${mapping}")
            mapping.inputs.forEach { data ->
                delete(data.id)
            }

            mapping.outputs.forEach { data ->
                delete(data.id)
            }
        }

        println("\n-----Remaining data-----")

        println("Nifti Images cached: \n")
        niftiImages.value.forEach { data ->
            println("${data.value.id} : ${data.value.name}\n")
        }


        println("File mapping cached: \n${fileMapping.value.values}\n")

        println("Hard saved mappings: \n")
        jsonMapper.mappings.value.forEach { data ->
            println("Title: ${data.title}")
            println("Inputs: ${data.inputs.first().id} : ${data.inputs.first().id}\n")
        }

       

    }
}