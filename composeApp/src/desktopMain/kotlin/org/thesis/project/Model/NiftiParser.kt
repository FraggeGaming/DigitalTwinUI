import kotlinx.serialization.json.Json
import org.nd4j.linalg.factory.Nd4j
import org.thesis.project.Model.NiftiData
import org.thesis.project.Model.NiftiMeta
import org.thesis.project.Model.UploadFileMetadata
import java.io.File
import java.nio.file.Paths

fun runNiftiParser(niftiPath: String, outputDir: String): String {
    val path = Paths.get("src/desktopMain/resources/executables/nifti_visualize.exe")

    println("running process")
    val process = ProcessBuilder(path.toAbsolutePath().toString(), niftiPath, outputDir)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0) throw RuntimeException("NIfTI parser failed")

    return output
}




//fun parseNiftiImages(jsonData: String, modality: String): NiftiData {
//    val json = Json { ignoreUnknownKeys = true }
//    val baseData = json.decodeFromString<NiftiData>(jsonData)
//    return baseData.copy(modality = modality)
//}

//private val json = Json { ignoreUnknownKeys = true }

fun parseNiftiImages(meta: NiftiMeta, metaData: UploadFileMetadata): NiftiData {

    val volume = loadNpyVoxelVolume(meta.npy_path)


    val coronalVoxel = transformToCoronalSlices(volume)
    val sagittalVoxel = transformToSagittalSlices(volume)

    return NiftiData(
        width = meta.width,
        height = meta.height,
        depth = meta.depth,
        voxelSpacing = meta.voxel_spacing,
        modality = metaData.modality,
        region = metaData.region,
        voxelVolume = volume,
        coronalVoxelSlices = coronalVoxel,
        sagittalVoxelSlices = sagittalVoxel,
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
    val shape = ndArray.shape()

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

    //Delete the file after loading
//    val deleted = file.delete()
//    if (deleted) {
//        println("File deleted successfully.")
//    } else {
//        println("Failed to delete the file.")
//    }

    return result
}


fun removeNiiExtension(filename: String): String {
    return filename.removeSuffix(".nii").removeSuffix(".nii.gz")
}