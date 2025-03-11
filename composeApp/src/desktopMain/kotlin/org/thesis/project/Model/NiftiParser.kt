import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

fun runNiftiParser(niftiPath: String): String {
    val exePath = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\executables\\nifti_visualize.exe"
    //val exePath = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\executables\\nifti_visualize.exe"

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
    val axial: List<String>, // Axial Base64 JSON Data
    val axialVoxels: List<List<List<Float>>>,
    val coronal: List<String>, // Coronal Base64 JSON Data
    val coronalVoxels: List<List<List<Float>>>,
    val sagittal: List<String>, // Sagittal Base64 JSON Data
    val sagittalVoxels: List<List<List<Float>>>,
    @kotlinx.serialization.Transient
    val axialImages: List<BufferedImage> = emptyList(),
    @kotlinx.serialization.Transient
    val coronalImages: List<BufferedImage> = emptyList(),
    @kotlinx.serialization.Transient
    val sagittalImages: List<BufferedImage> = emptyList(),
    val modality: String
)


fun parseNiftiImages(jsonData: String, modality: String): NiftiData {
    val json = Json { ignoreUnknownKeys = true }
    val jsonElement = json.parseToJsonElement(jsonData).jsonObject

    val width = jsonElement["width"]?.jsonPrimitive?.intOrNull ?: 0
    val height = jsonElement["height"]?.jsonPrimitive?.intOrNull ?: 0
    val depth = jsonElement["depth"]?.jsonPrimitive?.intOrNull ?: 0

    fun extractVoxelData(key: String): List<List<List<Float>>> {
        return jsonElement[key]?.jsonArray?.map { slice ->  // Iterate over slices (depth)
            slice.jsonArray.map { row ->  // Iterate over rows
                row.jsonArray.map { it.jsonPrimitive.floatOrNull ?: 0f } // Extract voxel values
            }
        } ?: emptyList()
    }


    val axialVoxels = extractVoxelData("axial_voxels")
    val coronalVoxels = extractVoxelData("coronal_voxels")
    val sagittalVoxels = extractVoxelData("sagittal_voxels")

    val axialEncoded = jsonElement["axial"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
    val coronalEncoded = jsonElement["coronal"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
    val sagittalEncoded = jsonElement["sagittal"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

    val axialImages = axialEncoded.mapNotNull { base64ToBufferedImage(it) }
    val coronalImages = coronalEncoded.mapNotNull { base64ToBufferedImage(it) }
    val sagittalImages = sagittalEncoded.mapNotNull { base64ToBufferedImage(it) }
    println("test : ${axialImages.size}")


    return NiftiData(
        width = width,
        height = height,
        depth = depth,
        axial = axialEncoded,
        coronal = coronalEncoded,
        sagittal = sagittalEncoded,
        axialVoxels = axialVoxels,
        coronalVoxels = coronalVoxels,
        sagittalVoxels = sagittalVoxels,
        axialImages = axialImages,
        coronalImages = coronalImages,
        sagittalImages = sagittalImages,
        modality = modality
    )
}


fun removeNiiExtension(filename: String): String {
    return filename.removeSuffix(".nii").removeSuffix(".nii.gz") // Handles both .nii and .nii.gz
}