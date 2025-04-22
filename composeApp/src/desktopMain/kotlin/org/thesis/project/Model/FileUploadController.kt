package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import parseNiftiImages
import removeNiiExtension
import runNiftiParser
import java.io.File
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
    private val json = Json { ignoreUnknownKeys = true }
    suspend fun loadNifti(niftiStorage: UploadFileMetadata): String = withContext(Dispatchers.IO) {
        try {

            val outPath_npy = Paths.get(PathStrings.OUTPUT_PATH_NPY.toString())

            val outputJson = runNiftiParser(niftiStorage.filePath, outPath_npy.toAbsolutePath().toString())
            val meta = json.decodeFromString<NiftiMeta>(outputJson)
            val niftiData = parseNiftiImages(meta, niftiStorage)
            niftiData.npy_path = meta.npy_path
            niftiData.gz_path = niftiStorage.filePath


            val fileName = removeNiiExtension(File(niftiStorage.filePath).nameWithoutExtension)
            println(fileName)

            niftiRepo.store(fileName, niftiData)

            fileName
        } catch (e: Exception) {
            println("ERROR in NIfTI load: ${e.message}")
            e.printStackTrace()
            System.out.flush()
            ""
        }
    }
}