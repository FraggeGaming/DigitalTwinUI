import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.thesis.project.Model.NiftiData
import org.thesis.project.Model.NiftiMeta
import org.thesis.project.Model.UploadFileMetadata
import java.io.File

//Function to run the nifti executable parser in python using a subprocess
fun runNiftiParser(niftiPath: String, outputDir: String, exePath: String): String {
    println("Running external process:")
    println("Executable: $exePath")
    println("Input NIfTI: $niftiPath")
    println("Output Dir: $outputDir")

    //Set permission on unix systems
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

//Parses the nifti image into a volume
fun parseNiftiImages(meta: NiftiMeta, metaData: UploadFileMetadata): NiftiData {

    val v = getNpy(meta.npy_path)

    return NiftiData(
        width = meta.width,
        height = meta.height,
        depth = meta.depth,
        voxelSpacing = meta.voxel_spacing,
        modality = metaData.modality,
        region = metaData.region,
        voxelVolume_ind = v,
        intensity_max = v.maxNumber().toFloat(),
        intensity_min = v.minNumber().toFloat(),
    )
}


fun getNpy(npyPath: String): INDArray {
    val file = File(npyPath)
    val ndArray = Nd4j.createFromNpyFile(file)

    return ndArray
}


fun removeNiiExtension(filename: String): String {
    return filename.removeSuffix(".nii").removeSuffix(".nii.gz")
}