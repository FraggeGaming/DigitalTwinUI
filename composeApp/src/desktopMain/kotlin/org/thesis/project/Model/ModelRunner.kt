package org.thesis.project.Model


import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import loadNpyVoxelVolume
import okhttp3.OkHttpClient
import removeNiiExtension
import transformToCoronalSlices
import transformToSagittalSlices
import java.io.File
import java.util.concurrent.TimeUnit

class ModelRunner(
    private val niftiRepo: NiftiRepo,
    private val fileUploader: FileUploadController
) {

    val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .build()

    private val _mLModels = MutableStateFlow<List<AIModel>>(emptyList())
    val mLModels: StateFlow<List<AIModel>> = _mLModels.asStateFlow()

    private val _hasFetchedModels = MutableStateFlow(false)
    val hasFetchedModels: StateFlow<Boolean> = _hasFetchedModels

    val progressFlows = mutableStateMapOf<String, MutableStateFlow<Progress>>()
    val progressKillFlows = mutableStateMapOf<String, MutableStateFlow<Progress>>()


    suspend fun fetchMLModels(metadata: UploadFileMetadata) = coroutineScope {
        _hasFetchedModels.value = false
        val models = fetchAvailableModels(PathStrings.SERVER_IP.toString(), metadata)
        if (models != null) {
            _mLModels.value = models
            _hasFetchedModels.value = true
        }
    }

    fun loadMapping(mapping: List<NiftiDataSlim>, isInput: Boolean = true, title:String): MutableList<String>{
        val data = mutableListOf<String>()

        mapping.forEach { output ->
            if (niftiRepo.get(output.id) != null) {
                val volume = loadNpyVoxelVolume(output.npy_path)


                val coronalVoxel = transformToCoronalSlices(volume)
                val sagittalVoxel = transformToSagittalSlices(volume)

                val niftiData = NiftiData(
                    id = output.id,
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
                    name = output.name,
                )

                val fileName = removeNiiExtension(File(output.gz_path).nameWithoutExtension)
                niftiRepo.store(niftiData.id, niftiData)
                println("stored nifti: ${niftiData.id}")

                data.add(niftiData.id)
            }
            else {
                println("Nifti already stored in cache")
                data.add(output.id)
            }
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

            val inputList = loadMapping(mapping.inputs, true, title)
            val outputList = loadMapping(mapping.outputs, false, title)
            niftiRepo.addFileMapping(title, inputList, outputList)
            println(niftiRepo.getFileMapping(title))
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

            niftiRepo.store(inputNifti.id, inputNifti)
            println("stored nifti: ${inputNifti.id}")
            input.add(inputNifti.id)

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

                niftiRepo.store(gt_inputnifti.id, gt_inputnifti)
                println("stored nifti: ${gt_inputnifti.id}")
                input.add(gt_inputnifti.id)

            }

            niftiRepo.updateFileMappingInput(title, input)

            if (file.model != null){
                val newProgressFlow = MutableStateFlow(
                    Progress(step = 0, total = 1, percent = 0.0, jobId = title, finished = false)
                )
                progressFlows[title] = newProgressFlow

                val returnedNifti = sendNiftiToServer(file, PathStrings.SERVER_IP.toString(), newProgressFlow, client, progressKillFlows)

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

                    niftiRepo.store(outputNifti!!.id, outputNifti!!)
                    println("stored nifti: ${outputNifti!!.id}")
                    output.add(outputNifti!!.id)

                }

                //VoxelSpacing transfer
                if (outputNifti != null){
                    outputNifti!!.voxelSpacing = inputNifti.voxelSpacing
                    niftiRepo.updateFileMappingOutput(title, output)
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

    fun cancelJob(jobId: String) {
        cancelRunningInference(jobId, client)
        progressKillFlows[jobId] = progressFlows[jobId]!!
        progressFlows.remove(jobId)
    }
}