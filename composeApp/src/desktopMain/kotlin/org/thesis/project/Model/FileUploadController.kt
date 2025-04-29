package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import parseNiftiImages
import runNiftiParser
import java.nio.file.Paths



class FileUploadController(private val niftiRepo: NiftiRepo) {

    private val _uploadedFileMetadata = MutableStateFlow<List<UploadFileMetadata>>(emptyList())
    val uploadedFileMetadata: StateFlow<List<UploadFileMetadata>> = _uploadedFileMetadata.asStateFlow()

    fun addFile(filePath: String) {
        val newFile = UploadFileMetadata(filePath, title = "", modality = "", region = "", groundTruthFilePath = "")
        _uploadedFileMetadata.update { it + newFile }
    }

    fun updateMetadata(index: Int, newData: UploadFileMetadata) {
        _uploadedFileMetadata.update {
            it.toMutableList().apply {
                if (index in indices) this[index] = newData
            }
        }
    }

    fun removeFile(index: Int) {
        _uploadedFileMetadata.update {
            it.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
        }
    }

    fun clear() {
        _uploadedFileMetadata.update { emptyList() }
    }
    private val json = Json { ignoreUnknownKeys = true }
    suspend fun loadNifti(niftiStorage: UploadFileMetadata): NiftiData = withContext(Dispatchers.IO) {

        val outPath_npy = Paths.get(PathStrings.OUTPUT_PATH_NPY.toString())
        val outputJson = runNiftiParser(niftiStorage.filePath, outPath_npy.toAbsolutePath().toString())

        val meta = json.decodeFromString<NiftiMeta>(outputJson)
        val niftiData = parseNiftiImages(meta, niftiStorage)
        niftiData.npy_path = meta.npy_path
        niftiData.gz_path = niftiStorage.filePath

        niftiData
    }

    fun uploadRunCheck(
        uploadedFiles: List<UploadFileMetadata>,
        mappings: List<FileMappingFull>
    ): Pair<Boolean, String>{
        var errorMsg = "Accepted"
        var canContinue = true



        val titles = uploadedFiles.map { it.title }
        val duplicateTitles = titles.groupingBy { it }.eachCount().filter { it.value > 1 }
        val existingTitles = mappings.map { it.title }.toSet()
        val overlappingTitles = titles.filter { it in existingTitles }
        val containsNonAscii = titles.any { title -> title.any { it.code > 127 } }
        if (containsNonAscii){
            errorMsg = "Only Ascii characters are allowed"
            canContinue = false
        }

        if (duplicateTitles.isNotEmpty()) {
            errorMsg = "Duplicate titles found: ${duplicateTitles.keys}"
            canContinue = false

        }

        else if (overlappingTitles.isNotEmpty()) {
            errorMsg = "These titles already exist: ${overlappingTitles.joinToString(", ")}"
            canContinue = false

        }

        else{
            for (file in uploadedFiles) {


                val missingField = when {
                    file.title.isBlank() -> "Title"
                    file.modality.isBlank() -> "Modality"
                    file.region.isBlank() -> "Region"
                    else -> null
                }

                if (missingField != null) {
                    val path = Paths.get(file.filePath)
                    val fileName = path.fileName.toString()

                    errorMsg = "Please select a $missingField for ${fileName}."
                    canContinue = false
                    break
                }
            }
        }
        return Pair(canContinue, errorMsg)
    }
}