package org.thesis.project.Model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

suspend fun sendNiftiToServer(
    metadata: UploadFileMetadata,
    serverUrl: String
): File? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
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
        .url(serverUrl)
        .post(requestBody)
        .build()

    val response = client.newCall(request).execute()
    if (!response.isSuccessful) {
        println("Request failed: ${response.code}")
        return@withContext null
    }

    val returnedFile = File.createTempFile("returned_", ".nii.gz")
    returnedFile.outputStream().use { output ->
        response.body?.byteStream()?.copyTo(output)
    }

    return@withContext returnedFile
}



