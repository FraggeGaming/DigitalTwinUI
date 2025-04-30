package org.thesis.project.Model

import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

suspend fun sendNiftiToServer(
    metadata: UploadFileMetadata,
    serverUrl: String,
    progressFlow: MutableStateFlow<Progress>,
    client: OkHttpClient,
    progressKillFlows: SnapshotStateMap<String, MutableStateFlow<Progress>>
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


        // 1. Start the model process
        val processRequest = Request.Builder()
            .url("$serverUrl/process")
            .post(requestBody)
            .build()

        client.newCall(processRequest).execute().use { processResponse ->
            if (!processResponse.isSuccessful) {
                println("Request failed: ${processResponse.code} ${processResponse.message}")
                return@withContext null
            }

            println("Model started successfully")
        }


        var nifti: File? = null
        var finished = false

        pollProgress(
            jobId = metadata.title,
            serverUrl = serverUrl,
            client = client,
            progressKillFlows = progressKillFlows,
            onProgress = { updated ->
                progressFlow.value = updated
                println("Updated progress: $updated")
                if (updated.finished) {
                    println("Model finished, downloading output...")
                    nifti = downloadResult(metadata.title, serverUrl, client)
                    finished = true
                }
            },
            shouldStop = { finished }
        )


        return@withContext nifti

    } catch (e: Exception) {
        println("Error sending NIfTI to server: ${e.localizedMessage}")
        e.printStackTrace()
        return@withContext null
    }
}

fun downloadResult(jobId: String, serverUrl: String, client: OkHttpClient): File? {
    try {

        val downloadRequest = Request.Builder()
            .url("$serverUrl/download/$jobId")
            .build()

        val downloadResponse = client.newCall(downloadRequest).execute()

        if (!downloadResponse.isSuccessful) {
            println("Download failed: ${downloadResponse.code}")
            return null
        }

        val outputDir = Paths.get(PathStrings.OUTPUT_PATH_GZ.toString()).toFile()
        outputDir.mkdirs()

        val returnedFileName = "generated_${jobId}.nii.gz"
        val returnedFile = File(outputDir, returnedFileName)

        downloadResponse.body?.byteStream()?.use { input ->
            returnedFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        println("Saved returned file to: ${returnedFile.absolutePath}")

        return returnedFile

    } catch (e: Exception) {
        println("Error downloading result: ${e.localizedMessage}")
        e.printStackTrace()
        return null
    }
}

fun cancelRunningInference(jobId: String, client: OkHttpClient) {
    CoroutineScope(Dispatchers.IO).launch {
        val request = Request.Builder()
            .url("${PathStrings.SERVER_IP}/cancel/$jobId")
            .post("".toRequestBody()) // empty body
            .build()

        val response = client.newCall(request).execute()
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



suspend fun fetchAvaliableModalities(serverUrl: String): List<String>? = withContext(Dispatchers.IO) {
    try {
        val request = Request.Builder()
            .url("$serverUrl/modalities")
            .get()
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return@withContext null
        }

        val bodyString = response.body?.string() ?: return@withContext null
        return@withContext Json.decodeFromString<List<String>>(bodyString)

    } catch (e: Exception) {
        println("Error fetching modalities: ${e.localizedMessage}")
        e.printStackTrace()
        return@withContext null
    }
}

suspend fun fetchAvaliableRegions(serverUrl: String): List<String>? = withContext(Dispatchers.IO) {
    try {
        val request = Request.Builder()
            .url("$serverUrl/regions")
            .get()
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return@withContext null
        }

        val bodyString = response.body?.string() ?: return@withContext null
        return@withContext Json.decodeFromString<List<String>>(bodyString)

    } catch (e: Exception) {
        println("Error fetching regions: ${e.localizedMessage}")
        e.printStackTrace()
        return@withContext null
    }
}


@Serializable
data class Progress(val step: Int, val total: Int, @SerialName("job_id") val jobId: String, val finished: Boolean = false)

suspend fun pollProgress(
    jobId: String,
    serverUrl: String,
    client: OkHttpClient,
    progressKillFlows: SnapshotStateMap<String, MutableStateFlow<Progress>>,
    onProgress: (Progress) -> Unit,
    shouldStop: () -> Boolean
) {
    val json = Json { ignoreUnknownKeys = true }

    while (true) {
        try {
            if (progressKillFlows[jobId] != null || shouldStop()) {
                progressKillFlows.remove(jobId)
                print(progressKillFlows.values)
                println("stopping polling progress...")
                break
            }
            println("Polling for $jobId ...")
            val request = Request.Builder()
                .url("$serverUrl/progress/$jobId")
                .build()

            val response = client.newCall(request).execute()
            if (response.code == 204) {
                //Server tried to read and write to the progress at the same time, just continue and send another request
                delay(2000)
                continue
            }
            val body = response.body?.string()

            if (body != null) {

                if (body.contains("error")) {
                    println("Polling returned error: $body")
                    break
                }
                val progress = json.decodeFromString<Progress>(body)
                onProgress(progress)
                if (progress.step >= progress.total) break
            }
        } catch (e: Exception) {
            println("Polling error: ${e.localizedMessage} : ${e.message}")
            break
        }
        delay(2000)
    }
}




