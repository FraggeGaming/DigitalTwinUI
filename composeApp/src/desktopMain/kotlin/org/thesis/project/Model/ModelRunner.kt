package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.nio.file.Paths

class ModelRunner(
    private val niftiRepo: NiftiRepo,
    private val fileUploader: FileUploadController
) {

    private val _mLModels = MutableStateFlow<List<AIModel>>(emptyList())
    val mLModels: StateFlow<List<AIModel>> = _mLModels.asStateFlow()


    init {
        val demoModels = listOf(
            AIModel(
                title = "CT to PET",
                description = "CT to PET translation using a Pix2Pix patchGan",
                inputModality = "CT",
                outputModality = "PET"
            ),
            AIModel(
                title = "PET to CT",
                description = "PET to CT translation",
                inputModality = "PET",
                outputModality = "CT"
            ),
        )

        _mLModels.value = demoModels
    }

    suspend fun runModel() = coroutineScope {



        fileUploader.uploadedFileMetadata.value.forEach { file ->
            val input = mutableListOf<String>()
            val output = mutableListOf<String>()
            println("Model: ${file.model}")

            println("Simulate backend")
            println("No backend... setting output same as input")

            val inputDeferred = async(Dispatchers.IO) { fileUploader.loadNifti(file) }
            input.add(inputDeferred.await())
            println("TESTING, $input")


            val returnedNifti = sendNiftiToServer(file, "http://localhost:8000/process")
            returnedNifti?.let {

                val outputFileName = "predicted_${file.title}.nii.gz"
                val outputDir = Paths.get("src/desktopMain/resources/output/").toFile()
                val outputFilePath = File(outputDir, outputFileName)

                outputDir.mkdirs() // Make sure the output directory exists
                it.copyTo(outputFilePath, overwrite = true) // ✅ Copy the returned file

                val predictedMetadata = UploadFileMetadata(
                    filePath = outputFilePath.absolutePath,
                    title = outputFileName,
                    modality = file.model?.outputModality ?: "",
                    region = file.region,
                    model = file.model,
                    groundTruthFilePath = ""
                )

                val outputDeferred = async(Dispatchers.IO) { fileUploader.loadNifti(predictedMetadata) }
                output.add(outputDeferred.await())
            }

            if (file.groundTruthFilePath.isNotBlank()){
                val fileName = file.groundTruthFilePath.substringAfterLast("/")

                //Create new UploadFileMetadata for the ground truth
                val newGTFile = UploadFileMetadata(
                    filePath = file.groundTruthFilePath,
                    title = "$fileName GT",
                    modality = file.model?.outputModality ?: "",
                    region = file.region,
                    model = file.model,
                    groundTruthFilePath = ""
                )
                val inputGT = async(Dispatchers.IO) { fileUploader.loadNifti(newGTFile) }
                input.add(inputGT.await())
                println("TESTING, $input")
            }
            //TODO run model here
            //TODO parse nifti

            val title = file.title
            println("TEST: $title")
            println("Added mapping for $title : $input, $output")
            System.out.flush()
            niftiRepo.addFileMapping(title, input, output)
        }

    }
}