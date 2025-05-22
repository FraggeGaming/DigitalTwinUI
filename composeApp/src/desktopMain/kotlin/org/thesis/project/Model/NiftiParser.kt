import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.thesis.project.Model.NiftiData
import org.thesis.project.Model.NiftiMeta
import org.thesis.project.Model.UploadFileMetadata
import java.io.File
import java.nio.file.Paths

fun runNiftiParser(niftiPath: String, outputDir: String, exePath: String): String {
    //val path = Paths.get("src/desktopMain/resources/executables/nifti_visualize.exe")


    println("Running external process:")
    println("Executable: $exePath")
    println("Input NIfTI: $niftiPath")
    println("Output Dir: $outputDir")

    if (!System.getProperty("os.name").startsWith("Windows")) {
        val chmodProcess = ProcessBuilder("chmod", "+x", exePath)
            .inheritIO()
            .start()
        val chmodExit = chmodProcess.waitFor()
        if (chmodExit != 0) {
            throw RuntimeException("chmod +x failed on $exePath")
        }
    }
    val process = ProcessBuilder(exePath.toString(), niftiPath, outputDir)
        .redirectErrorStream(true)
        .start()




    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0) throw RuntimeException("NIfTI parser failed")

    return output
}

fun fastPercentileEstimate(data: FloatArray, lowPercentile: Double = 0.02, highPercentile: Double = 0.98, bins: Int = 100): Pair<Float, Float> {
    if (data.isEmpty()) return 0f to 1f

    val min = data.minOrNull() ?: return 0f to 1f
    val max = data.maxOrNull() ?: return 0f to 1f
    if (min == max) return min to max

    val histogram = IntArray(bins)
    val binSize = (max - min) / bins

    // Build histogram
    for (v in data) {
        val bin = ((v - min) / (max - min) * (bins - 1)).toInt().coerceIn(0, bins - 1)
        histogram[bin]++
    }

    // Compute cumulative histogram
    val cumulative = IntArray(bins)
    histogram.foldIndexed(0) { i, acc, count ->
        cumulative[i] = acc + count
        acc + count
    }

    val total = cumulative.last()
    val lowerCount = (lowPercentile * total).toInt()
    val upperCount = (highPercentile * total).toInt()

    // Find bins that correspond to 2nd and 98th percentiles
    val pLowBin = cumulative.indexOfFirst { it >= lowerCount }
    val pHighBin = cumulative.indexOfFirst { it >= upperCount }

    val pLow = min + pLowBin * binSize
    val pHigh = min + pHighBin * binSize

    return pLow to pHigh
}

fun parseNiftiImages(meta: NiftiMeta, metaData: UploadFileMetadata): NiftiData {

    val v = getNpy(meta.npy_path)

    val data = v.data().asFloat()
    val (p2, p98) = fastPercentileEstimate(data)
    return NiftiData(
        width = meta.width,
        height = meta.height,
        depth = meta.depth,
        voxelSpacing = meta.voxel_spacing,
        modality = metaData.modality,
        region = metaData.region,
        voxelVolume_ind = v,
        intensity_max = p98,
        intensity_min = p2,
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

fun transformToAxialSlices(voxelVolume: Array<Array<Array<Float>>>): Array<Array<Array<Float>>> {
    val depth = voxelVolume.size
    val height = voxelVolume[0].size
    val width = voxelVolume[0][0].size

    return Array(depth) { z ->
        Array(height) { y ->
            Array(width) { x ->
                voxelVolume[z][y][x]
            }
        }
    }
}

//fun loadNpyVoxelVolume(npyPath: String): Array<Array<Array<Float>>> {
//    val file = File(npyPath)
//    val ndArray = Nd4j.createFromNpyFile(file)
//    val shape = ndArray.shape()
//
//    val width = shape[0].toInt()
//    val height = shape[1].toInt()
//    val depth = shape[2].toInt()
//
//    val result = Array(width) { x ->
//        Array(height) { y ->
//            Array(depth) { z ->
//                ndArray.getFloat(x, y, z)
//            }
//        }
//    }
//    ndArray.close()
//    return result
//}

fun getNpy(npyPath: String): INDArray {
    val file = File(npyPath)
    val ndArray = Nd4j.createFromNpyFile(file)

    return ndArray
}


fun removeNiiExtension(filename: String): String {
    return filename.removeSuffix(".nii").removeSuffix(".nii.gz")
}