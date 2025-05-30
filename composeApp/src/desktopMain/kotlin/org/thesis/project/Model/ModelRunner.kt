package org.thesis.project.Model


import androidx.compose.runtime.mutableStateMapOf
import getNpy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import removeNiiExtension
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit

/**
 * Class to run the nifti parsing
 * */
class ModelRunner(
    private val niftiRepo: NiftiRepo,
    private val fileUploader: FileUploadController,
    private val output_path_gz: File,
    private val input_path_gz: File,
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

    //Stateflows for the polling process
    val progressFlows = mutableStateMapOf<String, MutableStateFlow<Progress>>()
    val progressKillFlows = mutableStateMapOf<String, MutableStateFlow<Progress>>()

    suspend fun fetchMLModels(metadata: UploadFileMetadata) = coroutineScope {
        _hasFetchedModels.value = false
        val models = fetchAvailableModels(Config.serverIp, metadata)
        if (models != null) {
            _mLModels.value = models
            _hasFetchedModels.value = true
        }
    }

    //Load data from the jsonMappings if they are selected by the user
    fun loadMapping(mapping: List<NiftiDataSlim>): MutableList<String>{
        val data = mutableListOf<String>()

        mapping.forEach { output ->
            println(output)
            println(niftiRepo.get(output.id))
            if (niftiRepo.get(output.id) == null) {
                //Get nifti volume
                val v = getNpy(output.npy_path)

                val niftiData = NiftiData(
                    id = output.id,
                    width = output.width,
                    height = output.height,
                    depth = output.depth,
                    voxelSpacing = output.voxelSpacing,
                    modality = output.modality,
                    region = output.region,
                    voxelVolume_ind = v,
                    intensity_max = v.maxNumber().toFloat(),
                    intensity_min = v.minNumber().toFloat(),
                    npy_path = output.npy_path,
                    gz_path = output.gz_path,
                    name = output.name,
                )

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


        val mappings = niftiRepo.jsonMapper.selectedMappings.value.toList()

        //Load the files from the mappings, i.e already parsed data
        mappings.forEach { mapping ->
            val title = mapping.title
            if (niftiRepo.hasFileMapping(title)) {
                println("Mapping for $title already exists")
                return@forEach //Skip
            }

            val inputList = loadMapping(mapping.inputs)
            val outputList = loadMapping(mapping.outputs)
            niftiRepo.addFileMapping(title, inputList, outputList)
            println(niftiRepo.getFileMapping(title))
        }


        val uploadedFiles = fileUploader.uploadedFileMetadata.value.toList()

        fileUploader.clear()

        //Load the new files the user has uploaded in a separate thread
        uploadedFiles.forEach { file ->
            async(Dispatchers.IO) {
                println("Uploading $file")
                val input = mutableListOf<String>()
                val output = mutableListOf<String>()
                val inputNifti: NiftiData?
                var gt_inputnifti: NiftiData? = null
                var outputNifti: NiftiData? = null
                val title = file.title
                println("Model: ${file.model}")

                //Changing the name by adding the title for a unique name
                val newPath = copyAndChangeName(file.title, file.filePath)
                if (newPath != null) {
                    file.filePath = newPath

                    //Fetching/parsing the input nifti
                    inputNifti = async(Dispatchers.IO) { fileUploader.loadNifti(file) }.await()
                    inputNifti.gz_path = file.filePath

                    val fileName = removeNiiExtension(File(file.filePath).nameWithoutExtension)
                    inputNifti.name = fileName

                    niftiRepo.store(inputNifti.id, inputNifti)
                    println("stored nifti: ${inputNifti.id}")
                    input.add(inputNifti.id)
                } else {

                    println("File does not exist or failed to copy.")
                    return@async
                }


                //If ground truth file was uploaded. Parse that as well
                if (file.groundTruthFilePath.isNotBlank()){
                    //Create new UploadFileMetadata for the ground truth
                    val newGTFile = UploadFileMetadata(
                        filePath = file.groundTruthFilePath,
                        title = File(file.groundTruthFilePath).nameWithoutExtension + "_GT",
                        modality = file.model?.outputModality ?: "",
                        region = file.region,
                        model = file.model,
                        groundTruthFilePath = ""
                    )


                    val newPathGT = copyAndChangeName(newGTFile.title, newGTFile.filePath)
                    if (newPathGT != null) {
                        file.filePath = newPathGT

                        gt_inputnifti = async(Dispatchers.IO) { fileUploader.loadNifti(newGTFile) }.await()
                        gt_inputnifti.gz_path = newGTFile.filePath
                        gt_inputnifti.name = removeNiiExtension(File(file.groundTruthFilePath).nameWithoutExtension)

                        niftiRepo.store(gt_inputnifti.id, gt_inputnifti)
                        println("stored nifti: ${gt_inputnifti.id}")
                        input.add(gt_inputnifti.id)
                    } else {
                        println("File does not exist or failed to copy.")
                    }
                }

                niftiRepo.updateFileMappingInput(title, input)

                //If user has selected a AI model, send it to the server and update polling progress untill finished
                if (file.model != null){
                    val newProgressFlow = MutableStateFlow(
                        Progress(step = 0, total = 1, jobId = title, finished = false, status = "Sending job", error = false)
                    )
                    progressFlows[title] = newProgressFlow

                    val returnedNifti = sendNiftiToServer(file, Config.serverIp, newProgressFlow, client, progressKillFlows, output_path_gz )
                    println(progressFlows[title])
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

    fun removeJob(jobId: String){
        if (progressFlows.containsKey(jobId)) {
            //progressKillFlows[jobId] = progressFlows[jobId]!!
            progressFlows.remove(jobId)
        }
    }

    fun cancelJob(jobId: String) {
        if (progressFlows.containsKey(jobId)) {
            cancelRunningInference(jobId, client)
            progressKillFlows[jobId] = progressFlows[jobId]!!
            progressFlows.remove(jobId)
        }

    }

    //Copies the appended file and puts in the input_gz folder, and changes the name
    fun copyAndChangeName(name: String, filePath: String): String? {
        val path = Paths.get(filePath)
        val fileName = path.fileName.toString()

        val dotIndex = fileName.indexOf(".")
        val newFileName = if (dotIndex != -1) {
            fileName.substring(0, dotIndex) + "_$name" + fileName.substring(dotIndex)
        } else {
            fileName + "_$name"
        }

        Files.createDirectories(input_path_gz.toPath())
        val newPath = input_path_gz.toPath().resolve(newFileName)

        return try {
            Files.copy(path, newPath, StandardCopyOption.REPLACE_EXISTING)
            newPath.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}