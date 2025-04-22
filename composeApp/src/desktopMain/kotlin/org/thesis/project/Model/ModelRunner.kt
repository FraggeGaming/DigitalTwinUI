package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.nio.file.Paths
import java.util.*

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

        //copy all uploaded files into safe output folder
        val outputDir = Paths.get(PathStrings.OUTPUT_PATH_GZ.toString()).toFile()

        fileUploader.uploadedFileMetadata.value.forEachIndexed { index, file ->
            val originalFile = File(file.filePath)

            val randomId = UUID.randomUUID().toString().substring(0, 8)
            val newFilename = "${originalFile.nameWithoutExtension}_$randomId.nii.gz"
            val newFile = File(outputDir, newFilename)

            // Copy the file
            originalFile.copyTo(newFile, overwrite = true)

            // Use your provided updateMetadata method to update the entry
            fileUploader.updateMetadata(
                index,
                file.copy(filePath = newFile.absolutePath)
            )
        }


        fileUploader.uploadedFileMetadata.value.forEach { file ->
            val input = mutableListOf<String>()
            val output = mutableListOf<String>()
            var inputNifti: NiftiData?
            var gt_inputnifti: NiftiData? = null
            var outputNifti: NiftiData? = null
            println("Model: ${file.model}")

            //Fetching/parsing the input nifti
            val inputDeferred = async(Dispatchers.IO) { fileUploader.loadNifti(file) }
            val inputString = inputDeferred.await()
            inputNifti = niftiRepo.get(inputString) //Getting the nifti for voxelSpacing transfer
            input.add(inputString)

            if (inputNifti != null){
                inputNifti.gz_path = file.filePath
            }

            val returnedNifti = sendNiftiToServer(file, PathStrings.SERVER_IP.toString())
            returnedNifti?.let {

                val predictedMetadata = UploadFileMetadata(
                    filePath = it.absolutePath,
                    title = it.name,
                    modality = file.model?.outputModality ?: "",
                    region = file.region,
                    model = file.model,
                    groundTruthFilePath = ""
                )

                val outputDeferred = async(Dispatchers.IO) { fileUploader.loadNifti(predictedMetadata) }
                val outputString = outputDeferred.await()
                outputNifti = niftiRepo.get(outputString) //Getting the nifti for voxelSpacing transfer
                output.add(outputString)

                if (outputNifti != null){
                    outputNifti!!.gz_path = predictedMetadata.filePath
                }
            }

            //VoxelSpacing transfer
            if (outputNifti != null && inputNifti != null){
               outputNifti!!.voxelSpacing = inputNifti.voxelSpacing
            }



            //If ground truth file was uploaded. Parse that as well
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
                val gt_outputString = inputGT.await()
                gt_inputnifti = niftiRepo.get(gt_outputString)
                input.add(gt_outputString)

                if (gt_inputnifti != null){
                    gt_inputnifti.gz_path = newGTFile.filePath
                }
            }

            val title = file.title
            println("TEST: $title")
            println("Added mapping for $title : $input, $output")
            System.out.flush()


            println("----------Input--------")
            println(inputNifti)
            println("-----------output-----------")
            println(outputNifti)
            if (gt_inputnifti != null){
                println("---------GT-----------")
                println(gt_inputnifti)
            }


            niftiRepo.addFileMapping(title, input, output)

            val inputs = listOfNotNull(
                inputNifti?.toSlim(),
                gt_inputnifti?.toSlim()
            )

            val outputs = listOfNotNull(
                outputNifti?.toSlim()
            )

            val mapping = FileMappingFull(
                title = title,
                inputs = inputs,
                outputs = outputs
            )

            niftiRepo.jsonMapper.addMappingAndSave(mapping)
        }

    }
}