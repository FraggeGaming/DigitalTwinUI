package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

suspend fun sendNiftiToServer(
    metadata: UploadFileMetadata,
    serverUrl: String
): File? = withContext(Dispatchers.IO) {
    try {
        val niftiFile = File(metadata.filePath)
        val jsonMetadata = Json.encodeToString(metadata)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                niftiFile.name,
                niftiFile.asRequestBody("application/octet-stream".toMediaType())
            )
            .addFormDataPart(
                "metadata",
                null,
                jsonMetadata.toRequestBody("application/json".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("$serverUrl/process")
            .post(requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.MINUTES)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return@withContext null
        }

        val outputDir = Paths.get(PathStrings.OUTPUT_PATH_GZ.toString()).toFile()
        outputDir.mkdirs() // Ensure the folder exists

        val returnedFileName = "returned_${UUID.randomUUID().toString().substring(0, 8)}.nii.gz"
        val returnedFile = File(outputDir, returnedFileName)

        // Save the response body to the file
        returnedFile.outputStream().use { output ->
            response.body?.byteStream()?.copyTo(output)
        }

        println("Saved returned file to: ${returnedFile.absolutePath}")

        return@withContext returnedFile

    } catch (e: Exception) {
        println("Error sending NIfTI to server: ${e.localizedMessage}")
        e.printStackTrace()
        return@withContext null
    }
}

suspend fun fetchAvailableModels(serverUrl: String, metadata: UploadFileMetadata): List<AIModel>? = withContext(Dispatchers.IO) {
    try {
        val json = Json.encodeToString(mapOf("modality" to metadata.modality, "region" to metadata.region))
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$serverUrl/getmodels")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return@withContext null
        }

        val bodyString = response.body?.string()
        if (bodyString == null) {
            println("Empty response body.")
            return@withContext null
        }

        return@withContext Json.decodeFromString<List<AIModel>>(bodyString)

    } catch (e: Exception) {
        println("Error fetching models: ${e.localizedMessage}")
        e.printStackTrace()
        return@withContext null
    }

}



