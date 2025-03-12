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
    val modality: String = "CT",

    @Transient
    var imageSlicesAxial: List<BufferedImage> = emptyList(),

    @Transient
    var imageSlicesCoronal: List<BufferedImage> = emptyList(),

    @Transient
    var imageSlicesSagittal: List<BufferedImage> = emptyList(),

    @Transient var coronalVoxelSlices: List<List<List<Float>>> = emptyList(),
    @Transient var sagittalVoxelSlices: List<List<List<Float>>> = emptyList()

)




fun parseNiftiImages(jsonData: String, modality: String): NiftiData {
    val json = Json { ignoreUnknownKeys = true }
    val baseData = json.decodeFromString<NiftiData>(jsonData)
    return baseData.copy(modality = modality)
}


fun removeNiiExtension(filename: String): String {
    return filename.removeSuffix(".nii").removeSuffix(".nii.gz") // Handles both .nii and .nii.gz
}