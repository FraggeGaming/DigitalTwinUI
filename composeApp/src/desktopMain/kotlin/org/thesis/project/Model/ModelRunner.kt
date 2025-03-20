package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.forEach

class ModelRunner(
    private val niftiRepo: NiftiRepo,
    private val fileUploader: FileUploadController
) {

    suspend fun runModel() = coroutineScope {

        fileUploader.uploadedFileMetadata.value.forEach { file ->
            println("Model: ${file.model}")

            println("Simulate backend")
            println("No backend... setting output same as input")

            val inputDeferred = async(Dispatchers.IO) { fileUploader.loadNifti(file) }
            val input = inputDeferred.await()
            println("TESTING, $input")


            // outputFiles.add((uploadedFileMetadata.value!!.filePath))
            //val outputDeferred = async(Dispatchers.IO) { loadNifti(outputNiftiFile) }

            val output = input// Placeholder until real model output is ready


            val title = file.title
            println("TEST: $title")
            println("Added mapping for $title : ${listOf(input)}, ${listOf(output)}")
            System.out.flush()
            if (title != null) {
                niftiRepo.addFileMapping(title, listOf(input), listOf(output))
            }
            else{
                println("no title, could not add to mapping")
            }
        }

    }

    private val _mLModels = MutableStateFlow<List<AIModel>>(emptyList())
    val mLModels: StateFlow<List<AIModel>> = _mLModels.asStateFlow()


    init {
        val demoModels = listOf(
            AIModel(
                title = "CT to PET",
                description = "Segmentation of brain tumor from CT scans.",
                inputModality = "CT",
                outputModality = "PET"
            ),
            AIModel(
                title = "PET to CT",
                description = "Detection of lung nodules from PET images.",
                inputModality = "PET",
                outputModality = "CT"
            ),
        )

        _mLModels.value = demoModels
    }
}