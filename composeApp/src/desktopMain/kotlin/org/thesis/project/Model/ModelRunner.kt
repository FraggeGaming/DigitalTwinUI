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

    fun loadMapping(mapping: List<NiftiDataSlim>): MutableList<String>{
        val data = mutableListOf<String>()

        mapping.forEach { output ->
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
            data.add(fileName)
        }

        return data
    }

    suspend fun runModel() = coroutineScope {

        //loadFromJson()
        niftiRepo.jsonMapper.selectedMappings.value.forEach { mapping ->
            val title = mapping.title
            if (niftiRepo.hasFileMapping(title)) {
                println("Mapping for $title already exists")
                return@forEach //Skip
            }

            val inputList = loadMapping(mapping.inputs)
            val outputList = loadMapping(mapping.outputs)
            niftiRepo.addFileMapping(title, inputList, outputList)
        }


        fileUploader.uploadedFileMetadata.value.forEach { file ->
            val input = mutableListOf<String>()
            val output = mutableListOf<String>()
            val inputNifti: NiftiData?
            var gt_inputnifti: NiftiData? = null
            var outputNifti: NiftiData? = null
            val title = file.title
            println("Model: ${file.model}")

            //Fetching/parsing the input nifti
            inputNifti = async(Dispatchers.IO) { fileUploader.loadNifti(file) }.await()
            inputNifti.gz_path = file.filePath

            val fileName = removeNiiExtension(File(file.filePath).nameWithoutExtension)
            inputNifti.name = fileName

            niftiRepo.store(inputNifti.name, inputNifti)
            input.add(inputNifti.name)

            //If ground truth file was uploaded. Parse that as well
            if (file.groundTruthFilePath.isNotBlank()){
                //Create new UploadFileMetadata for the ground truth
                val newGTFile = UploadFileMetadata(
                    filePath = file.groundTruthFilePath,
                    title = "${file.groundTruthFilePath.substringAfterLast("/")}_GT",
                    modality = file.model?.outputModality ?: "",
                    region = file.region,
                    model = file.model,
                    groundTruthFilePath = ""
                )

                gt_inputnifti = async(Dispatchers.IO) { fileUploader.loadNifti(newGTFile) }.await()
                gt_inputnifti.gz_path = newGTFile.filePath
                gt_inputnifti.name = removeNiiExtension(File(file.groundTruthFilePath).nameWithoutExtension)

                niftiRepo.store(gt_inputnifti.name, gt_inputnifti)
                input.add(gt_inputnifti.name)

            }

            niftiRepo.updateFileMappingInput(title, input)

            if (file.model != null){
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
                    outputNifti = async(Dispatchers.IO) { fileUploader.loadNifti(predictedMetadata) }.await()
                    outputNifti!!.gz_path = predictedMetadata.filePath
                    outputNifti!!.name = "Gen_${inputNifti.name}"

                    niftiRepo.store(outputNifti!!.name, outputNifti!!)
                    output.add(outputNifti!!.name)
                }

                //VoxelSpacing transfer
                if (outputNifti != null){
                    outputNifti!!.voxelSpacing = inputNifti.voxelSpacing
                    niftiRepo.updateFileMappingInput(title, output)
                }
            }


            val inputs = listOfNotNull(
                inputNifti.toSlim(),
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