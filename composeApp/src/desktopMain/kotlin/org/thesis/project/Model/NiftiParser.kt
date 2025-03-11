import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

fun runNiftiParser(niftiPath: String): String {
    //val exePath = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\executables\\nifti_visualize.exe"
    val exePath = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\executables\\nifti_visualize.exe"

    val exeFile = File(exePath)
    if (!exeFile.exists()) {
        throw RuntimeException("Executable not found at: $exePath")
    }

    val process = ProcessBuilder(exePath, niftiPath)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().readText()
    val errorOutput = process.errorStream.bufferedReader().readText()
    process.waitFor()

    if (errorOutput.isNotEmpty()) {
        println("Error running NIfTI parser:\n$errorOutput")
    }

    return output
}

fun base64ToBufferedImage(base64: String): BufferedImage? {
    return try {
        val imageBytes = Base64.getDecoder().decode(base64)
        val inputStream = ByteArrayInputStream(imageBytes)
        ImageIO.read(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Serializable
data class NiftiData(
    val width: Int,
    val height: Int,
    val depth: Int,
    val voxel_volume: List<List<List<Float>>>,

    @Transient
    val modality: String = "CT"
)




fun parseNiftiImages(jsonData: String, modality: String): NiftiData {
    val json = Json { ignoreUnknownKeys = true }
    val baseData = json.decodeFromString<NiftiData>(jsonData)
    return baseData.copy(modality = modality)


//    val axialVoxels = extractVoxelData("axial_voxels")
//    val coronalVoxels = extractVoxelData("coronal_voxels")
//    val sagittalVoxels = extractVoxelData("sagittal_voxels")


//    val axialEncoded = jsonElement["axial"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
//    val coronalEncoded = jsonElement["coronal"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
//    val sagittalEncoded = jsonElement["sagittal"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

//    val axialImages = axialEncoded.mapNotNull { base64ToBufferedImage(it) }
//    val coronalImages = coronalEncoded.mapNotNull { base64ToBufferedImage(it) }
//    val sagittalImages = sagittalEncoded.mapNotNull { base64ToBufferedImage(it) }
//    println("test : ${axialImages.size}")
}


fun removeNiiExtension(filename: String): String {
    return filename.removeSuffix(".nii").removeSuffix(".nii.gz") // Handles both .nii and .nii.gz
}