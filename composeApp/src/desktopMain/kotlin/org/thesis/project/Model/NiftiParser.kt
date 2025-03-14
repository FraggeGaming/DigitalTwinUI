import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

fun runNiftiParser(niftiPath: String): String {
    val exePath = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\executables\\nifti_visualize.exe"
    //val exePath = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\executables\\nifti_visualize.exe"    val exeFile = File(exePath)
    val exeFile = File(exePath)

    if (!exeFile.exists()) throw RuntimeException("Executable not found at: $exePath")

    val process = ProcessBuilder(exePath, niftiPath)
        .redirectErrorStream(false) // Better to read stdout and stderr separately
        .start()

    val stdout = StringBuilder()
    val stderr = StringBuilder()

    val stdoutThread = Thread {
        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                stdout.appendLine(line)
            }
        }
    }

    val stderrThread = Thread {
        process.errorStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                stderr.appendLine(line)
            }
        }
    }

    stdoutThread.start()
    stderrThread.start()

    stdoutThread.join()
    stderrThread.join()

    val exitCode = process.waitFor()
    if (stderr.isNotEmpty()) {
        println("NIfTI parser stderr:\n${stderr.trim()}")
    }

    if (exitCode != 0) {
        throw RuntimeException("Parser failed with exit code $exitCode")
    }

    return stdout.toString()
}


@Serializable
data class NiftiData(
    val width: Int,
    val height: Int,
    val depth: Int,
    val voxel_volume: List<List<List<Float>>>,

    @Transient
    val modality: String = "",

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