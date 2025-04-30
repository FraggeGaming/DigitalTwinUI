package org.thesis.project.Model


import androidx.compose.runtime.mutableStateMapOf
import getNpy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
//import loadNpyVoxelVolume
import okhttp3.OkHttpClient
import removeNiiExtension
import transformToAxialSlices
import transformToCoronalSlices
import transformToSagittalSlices
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
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

//    init {
//        val dummyProgress = Progress(
//            step = 0,
//            total = 10,
//            percent = 0.0,
//            jobId = "dummy_job",
//            finished = false
//        )
//
//        val dummyProgressFlow = MutableStateFlow(dummyProgress)
//
//        progressFlows["dummy_job"] = dummyProgressFlow
//    }

    suspend fun fetchMLModels(metadata: UploadFileMetadata) = coroutineScope {
        _hasFetchedModels.value = false
        val models = fetchAvailableModels(PathStrings.SERVER_IP.toString(), metadata)
        if (models != null) {
            _mLModels.value = models
            _hasFetchedModels.value = true
        }
    }



    fun loadMapping(mapping: List<NiftiDataSlim>): MutableList<String>{
        val data = mutableListOf<String>()

        mapping.forEach { output ->
            println(output)
            println(niftiRepo.get(output.id))
            if (niftiRepo.get(output.id) == null) {
//                val volume = loadNpyVoxelVolume(output.npy_path)
                val v = getNpy(output.npy_path)
//                val axialVoxel = transformToAxialSlices(volume)
//                val coronalVoxel = transformToCoronalSlices(volume)
//                val sagittalVoxel = transformToSagittalSlices(volume)


                val niftiData = NiftiData(
                    id = output.id,
                    width = output.width,
                    height = output.height,
                    depth = output.depth,
                    voxelSpacing = output.voxelSpacing,
                    modality = output.modality,
                    region = output.region,
                    voxelVolume_ind = v,
//                    voxelVolume = axialVoxel,
//                    coronalVoxelSlices = coronalVoxel,
//                    sagittalVoxelSlices = sagittalVoxel,
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
                file.filePath = copyAndChangeName(file.title, file.filePath)

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

                    newGTFile.filePath = copyAndChangeName(newGTFile.title, newGTFile.filePath)
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
                        Progress(step = 0, total = 1, jobId = title, finished = false)
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

                if (progressFlows.containsKey(title)) {
                    progressFlows.remove(title)
                }
            }

        }




    }

    fun cancelJob(jobId: String) {
        if (progressFlows.containsKey(jobId)) {
            cancelRunningInference(jobId, client)
            progressKillFlows[jobId] = progressFlows[jobId]!!
            progressFlows.remove(jobId)
        }

    }

    fun copyAndChangeName(name: String, filePath: String): String {
        val path = Paths.get(filePath)
        val fileName = path.fileName.toString()

        val dotIndex = fileName.indexOf(".")
        val newFileName = if (dotIndex != -1) {
            fileName.substring(0, dotIndex) + "_$name" + fileName.substring(dotIndex)
        } else {
            fileName + "_$name"
        }

        val targetDir = Paths.get(PathStrings.INPUT_PATH_GZ.path)
        Files.createDirectories(targetDir)

        val newPath = targetDir.resolve(newFileName)

        Files.copy(path, newPath, StandardCopyOption.REPLACE_EXISTING)

        return newPath.toString()
    }
}