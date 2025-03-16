import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.nd4j.linalg.factory.Nd4j
import java.io.File

fun runNiftiParser(niftiPath: String, outputDir: String): String {
    //val exePath = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\executables\\nifti_visualize.exe"
    val exePath = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\executables\\nifti_visualize.exe"
    val process = ProcessBuilder(exePath, niftiPath, outputDir)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0) throw RuntimeException("NIfTI parser failed")

    return output //width/height/depth/npy_path
}

@Serializable
data class NiftiMeta(
    val width: Int,
    val height: Int,
    val depth: Int,
    val voxel_spacing: List<Float>,
    val npy_path: String
)

data class NiftiData(
    val width: Int,
    val height: Int,
    val depth: Int,
    val voxelSpacing: List<Float>,
    val voxelVolume: Array<Array<Array<Float>>>,
    val modality: String = "",
    var coronalVoxelSlices: Array<Array<Array<Float>>> = emptyArray(),
    var sagittalVoxelSlices: Array<Array<Array<Float>>> = emptyArray()
)

//fun parseNiftiImages(jsonData: String, modality: String): NiftiData {
//    val json = Json { ignoreUnknownKeys = true }
//    val baseData = json.decodeFromString<NiftiData>(jsonData)
//    return baseData.copy(modality = modality)
//}

fun parseNiftiImages(jsonMeta: String, modality: String): NiftiData {
    val meta = Json { ignoreUnknownKeys = true }.decodeFromString<NiftiMeta>(jsonMeta)
    val volume = loadNpyVoxelVolume(meta.npy_path)


    val coronalVoxel =  transformToCoronalSlices(volume)
    val sagittalVoxel =  transformToSagittalSlices(volume)

    return NiftiData(
        width = meta.width,
        height = meta.height,
        depth = meta.depth,
        voxelSpacing = meta.voxel_spacing, // <-- ADDED
        voxelVolume = volume,
        modality = modality,
        coronalVoxelSlices = coronalVoxel,
        sagittalVoxelSlices = sagittalVoxel
    )
}

fun transformToCoronalSlices(voxelVolume: Array<Array<Array<Float>>>): Array<Array<Array<Float>>> {
    val depth = voxelVolume.size
    val height = voxelVolume[0].size
    val width = voxelVolume[0][0].size

    return Array(height) { y ->
        Array(depth) { z ->
            Array(width) { x ->
                voxelVolume[z][y][x]
            }
        }
    }
}

fun transformToSagittalSlices(voxelVolume: Array<Array<Array<Float>>>): Array<Array<Array<Float>>> {
    val depth = voxelVolume.size
    val height = voxelVolume[0].size
    val width = voxelVolume[0][0].size

    return Array(width) { x ->
        Array(depth) { z ->
            Array(height) { y ->
                voxelVolume[z][y][x]
            }
        }
    }
}

fun loadNpyVoxelVolume(npyPath: String): Array<Array<Array<Float>>> {
    val file = File(npyPath)
    val ndArray = Nd4j.createFromNpyFile(file)
    val shape = ndArray.shape()  // LongArray

    val width = shape[0].toInt()
    val height = shape[1].toInt()
    val depth = shape[2].toInt()

    val result = Array(width) { x ->
        Array(height) { y ->
            Array(depth) { z ->
                ndArray.getFloat(x, y, z)
            }
        }
    }
    return result
}


fun removeNiiExtension(filename: String): String {
    return filename.removeSuffix(".nii").removeSuffix(".nii.gz") // Handles both .nii and .nii.gz
}