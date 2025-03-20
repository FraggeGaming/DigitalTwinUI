package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Paths
import runNiftiParser
import parseNiftiImages
import removeNiiExtension

class FileUploadController(private val niftiRepo: NiftiRepo,) {

    private val _uploadedFileMetadata = MutableStateFlow<UploadFileMetadata?>(null)
    val uploadedFileMetadata: StateFlow<UploadFileMetadata?> = _uploadedFileMetadata.asStateFlow()

    fun addFile(filePath: String) {
        _uploadedFileMetadata.value = UploadFileMetadata(filePath, title = "", modality = "", region = "")
    }

    fun updateMetadata(newData: UploadFileMetadata) {
        _uploadedFileMetadata.value = newData
    }

    fun removeFile() {
        _uploadedFileMetadata.value = null
    }
    suspend fun loadNifti(): String = withContext(Dispatchers.IO) {
        try {
            val niftiStorage = _uploadedFileMetadata.value ?: return@withContext ""

            println("Running NIfTI Parser for: ${niftiStorage.filePath}")
            System.out.flush()

            val outPath = Paths.get("src/desktopMain/resources/output")
            println("After output path val")
            System.out.flush()

            val outputJson = runNiftiParser(niftiStorage.filePath, outPath.toAbsolutePath().toString())
            val niftiData = parseNiftiImages(outputJson, niftiStorage)

            println("NIFTI: $niftiData")
            System.out.flush()

            val fileName = removeNiiExtension(File(niftiStorage.filePath).nameWithoutExtension)
            println(fileName)

            niftiRepo.store(fileName, niftiData)
            println("Stored NIfTI images for: $fileName")
            System.out.flush()

            println("Returning filename: $fileName")
            fileName
        } catch (e: Exception) {
            println("ERROR in NIfTI load: ${e.message}")
            e.printStackTrace()
            System.out.flush()
            ""
        }
    }
}