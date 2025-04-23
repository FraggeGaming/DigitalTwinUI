package org.thesis.project.Model


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import loadNpyVoxelVolume
import removeNiiExtension
import transformToCoronalSlices
import transformToSagittalSlices
import java.io.File

class ModelRunner(
    private val niftiRepo: NiftiRepo,
    private val fileUploader: FileUploadController
) {

    private val _mLModels = MutableStateFlow<List<AIModel>>(emptyList())
    val mLModels: StateFlow<List<AIModel>> = _mLModels.asStateFlow()

    private val _hasFetchedModels = MutableStateFlow(false)
    val hasFetchedModels: StateFlow<Boolean> = _hasFetchedModels


    suspend fun fetchMLModels(metadata: UploadFileMetadata) = coroutineScope {
        _hasFetchedModels.value = false
        val models = fetchAvailableModels(PathStrings.SERVER_IP.toString(), metadata)
        if (models != null) {
            _mLModels.value = models
            _hasFetchedModels.value = true
        }


    }

    private fun loadFromJson(){
        niftiRepo.jsonMapper.selectedMappings.value.forEach { mapping ->

            val title = mapping.title

            if (niftiRepo.hasFileMapping(title)) {
                println("Mapping for $title already exists")
                return@forEach //Skip
            }

            val inputList = mutableListOf<String>()
            val outputList = mutableListOf<String>()



            mapping.inputs.forEach { input ->
                val volume = loadNpyVoxelVolume(input.npy_path)

                val coronalVoxel = transformToCoronalSlices(volume)
                val sagittalVoxel = transformToSagittalSlices(volume)

                val niftiData = NiftiData(
                    width = input.width,
                    height = input.height,
                    depth = input.depth,
                    voxelSpacing = input.voxelSpacing,
                    modality = input.modality,
                    region = input.region,
                    voxelVolume = volume,
                    coronalVoxelSlices = coronalVoxel,
                    sagittalVoxelSlices = sagittalVoxel,
                    npy_path = input.npy_path,
                    gz_path = input.gz_path,
                )

                val fileName = removeNiiExtension(File(input.gz_path).nameWithoutExtension)
                niftiRepo.store(fileName, niftiData)
                inputList.add(fileName)
            }


            mapping.outputs.forEach { output ->
                val volume = loadNpyVoxelVolume(output.npy_path)


                val coronalVoxel = transformToCoronalSlices(volume)
                val sagittalVoxel = transformToSagittalSlices(volume)

                val niftiData = NiftiData(
                    width = output.width,
                    height = output.height,
                    depth = output.depth,
                    voxelSpacing = output.voxelSpacing,
                    modality = output.modality,
                    region = output.region,
                    voxelVolume = volume,
                    coronalVoxelSlices = coronalVoxel,
                    sagittalVoxelSlices = sagittalVoxel,
                    npy_path = output.npy_path,
                    gz_path = output.gz_path,
                )

                val fileName = removeNiiExtension(File(output.gz_path).nameWithoutExtension)
                niftiRepo.store(fileName, niftiData)
                outputList.add(fileName)
            }


            niftiRepo.addFileMapping(title, inputList, outputList)
        }
    }

    suspend fun runModel() = coroutineScope {


        loadFromJson()


//        fileUploader.uploadedFileMetadata.value.forEachIndexed { index, file ->
//            val originalFile = File(file.filePath)
//
//            val randomId = UUID.randomUUID().toString().substring(0, 8)
//            val newFilename = "${originalFile.nameWithoutExtension}_$randomId.nii.gz"
//            val newFile = File(outputDir, newFilename)
//
//            // Copy the file
//            originalFile.copyTo(newFile, overwrite = true)
//
//            // Use your provided updateMetadata method to update the entry
//            fileUploader.updateMetadata(
//                index,
//                file.copy(filePath = newFile.absolutePath)
//            )
//
//            if (file.groundTruthFilePath.isNotBlank()){
//
//            }
//        }


        fileUploader.uploadedFileMetadata.value.forEach { file ->
            val input = mutableListOf<String>()
            val output = mutableListOf<String>()
            val inputNifti: NiftiData?
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
                    title = "${fileName}_GT",
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