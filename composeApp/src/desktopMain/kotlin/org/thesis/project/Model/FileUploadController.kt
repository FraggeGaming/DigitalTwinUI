package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
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

    suspend fun loadNifti(niftiStorage: UploadFileMetadata): String = withContext(Dispatchers.IO) {
        try {

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