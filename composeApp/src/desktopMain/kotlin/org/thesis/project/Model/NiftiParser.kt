import androidx.compose.runtime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.thesis.project.Model.InterfaceModel
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

fun runNiftiParser(niftiPath: String): String {
    val exePath = "G:/Coding/Imaging/composeApp/src/desktopMain/resources/executables/nifti_visualize.exe"

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

@Serializable
data class NiftiImageData(
    val axial: List<String>,
    val coronal: List<String>,
    val sagittal: List<String>
)

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

fun parseNiftiImages(jsonData: String): Triple<List<BufferedImage>, List<BufferedImage>, List<BufferedImage>> {
    val json = Json { ignoreUnknownKeys = true }
    val niftiImages = json.decodeFromString<NiftiImageData>(jsonData)

    val axialImages = niftiImages.axial.mapNotNull { base64ToBufferedImage(it) }
    val coronalImages = niftiImages.coronal.mapNotNull { base64ToBufferedImage(it) }
    val sagittalImages = niftiImages.sagittal.mapNotNull { base64ToBufferedImage(it) }

    return Triple(axialImages, coronalImages, sagittalImages)
}

fun removeNiiExtension(filename: String): String {
    return filename.removeSuffix(".nii").removeSuffix(".nii.gz") // Handles both .nii and .nii.gz
}

@Composable
fun parseNifti(niftiPath: String, interfaceModel: InterfaceModel): String? {
    var isProcessing by remember { mutableStateOf(false) }
    var output by remember { mutableStateOf<String?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(niftiPath) {
        if (!isProcessing) {
            isProcessing = true
            println("Running NIfTI Parser for: $niftiPath")

            output = runNiftiParser(niftiPath) // Run the Python script

            output?.let {
                val (axial, coronal, sagittal) = parseNiftiImages(it)
                fileName = removeNiiExtension(File(niftiPath).nameWithoutExtension)  // Extract filename
                interfaceModel.storeNiftiImages(fileName!!, axial, coronal, sagittal) // ✅ Store images in ViewModel
                println("Stored NIfTI images for: $fileName")
            }
        }
    }

    return fileName
}