package org.thesis.project.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.nio.file.Paths
import java.util.*
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j


enum class NiftiView(val displayName: String) {
    AXIAL("Axial"),
    CORONAL("Coronal"),
    SAGITTAL("Sagittal");

    override fun toString(): String = displayName
}

enum class Settings(val settingName: String) {
    PIXEL("Pixel Value"),
    MEASUREMENT("Measurement");

    override fun toString(): String = settingName
}

enum class PathStrings(val path: String) {
    OUTPUT_PATH_GZ("src/desktopMain/resources/output_gz"),
    OUTPUT_PATH_NPY("src/desktopMain/resources/output_npy"),
    INPUT_PATH_GZ("src/desktopMain/resources/input_gz"),
    //PREV_VIEWED_PATH("src/desktopMain/resources/prev_view.xml"),
    SAVED_MAPPING("src/desktopMain/resources/saved_mapping.txt"),
    SERVER_IP("http://localhost:8000");

    override fun toString(): String = path
}
@Serializable
data class UploadFileMetadata(
    var filePath: String,
    var title: String,
    var modality: String,
    var region: String,
    var model: AIModel? = null,
    val groundTruthFilePath: String,
)

@Serializable
data class AIModel(val id: Int, val title: String, val description: String, val inputModality: String, val outputModality: String, val region: String)

data class NiftiData(
    val id: String = UUID.randomUUID().toString(),
    val width: Int,
    val height: Int,
    val depth: Int,
    var voxelSpacing: List<Float>,
    val modality: String = "",
    var region: String = "",
    var voxelVolume_ind: INDArray,
//    var voxelVolume: Array<Array<Array<Float>>> = emptyArray(),
//    var coronalVoxelSlices: Array<Array<Array<Float>>> = emptyArray(),
//    var sagittalVoxelSlices: Array<Array<Array<Float>>> = emptyArray(),
    var npy_path: String = "",
    var gz_path: String = "",
    var name: String = "",
) {
    fun toSlim(): NiftiDataSlim {
        return NiftiDataSlim(
            id = this.id,
            width = this.width,
            height = this.height,
            depth = this.depth,
            voxelSpacing = this.voxelSpacing,
            modality = this.modality,
            region = this.region,
            npy_path = this.npy_path,
            gz_path = this.gz_path,
            name = this.name,

        )
    }

    fun clearData() {
        try {
            voxelVolume_ind.close()
            println("data cleared")
        }
        catch (e: Exception) {
            println(e)
        }

        voxelVolume_ind = Nd4j.empty()

    }
}

@Serializable
data class NiftiMeta(
    val width: Int,
    val height: Int,
    val depth: Int,
    val voxel_spacing: List<Float>,
    val npy_path: String
)

@Serializable
data class NiftiDataSlim(
    val id: String = "",
    val width: Int,
    val height: Int,
    val depth: Int,
    val voxelSpacing: List<Float>,
    val modality: String = "",
    val region: String = "",
    val npy_path: String = "",
    val gz_path: String = "",
    var name: String = "",

)

@Serializable
data class FileMappingFull(
    val title: String,
    val inputs: List<NiftiDataSlim>,
    val outputs: List<NiftiDataSlim>
)


class InterfaceModel : ViewModel() {
    val imageController = ImageController(viewModelScope)
    val niftiRepo = NiftiRepo(imageController)
    val fileUploader = FileUploadController(niftiRepo)
    val panelLayout = PanelLayoutController()

    val modelRunner = ModelRunner(niftiRepo, fileUploader)



    private val _organs = MutableStateFlow(listOf("Liver", "Heart", "Lung", "Kidney", "Brain"))
    val organs: StateFlow<List<String>> = _organs

//    private val _selectedDistricts = MutableStateFlow<Set<String>>(setOf())
//    val selectedDistricts: StateFlow<Set<String>> = _selectedDistricts

    private val _regions = MutableStateFlow<List<String>>(listOf())
    val regions: StateFlow<List<String>> = _regions

    private val _modalities = MutableStateFlow<List<String>>(listOf())
    val modalities: StateFlow<List<String>> = _modalities

    fun resetRegionsAndModalities() {
        _regions.value = listOf()
        _modalities.value = listOf()
    }

    suspend fun fetchRegions() = coroutineScope{
        val regions = fetchAvailableRegions(PathStrings.SERVER_IP.toString())
        println("regions: $regions")
        _regions.value = regions ?: listOf("Head", "Lung", "Total Body")
    }

    suspend fun fetchModalities() = coroutineScope{
        val modalities = fetchAvailableModalities(PathStrings.SERVER_IP.toString())
        _modalities.value = modalities ?: listOf("CT", "PET", "MRI")
    }

//    fun updateSelectedDistrict(label: String, isSelected: Boolean) {
//        _selectedDistricts.update { currentSet ->
//            if (isSelected) currentSet + label else currentSet - label
//        }
//    }

    private val _selectedSettings = MutableStateFlow<Set<Settings>>(setOf())
    val selectedSettings: StateFlow<Set<Settings>> = _selectedSettings

    fun updateSelectedSettings(label: Settings, isSelected: Boolean) {
        _selectedSettings.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }

    private fun establishFolderIntegrity() {
        PathStrings.entries.forEach { pathString ->
            val path = pathString.path

            if (path.startsWith("http")) {
                // Skip server URLs
                return@forEach
            }

            val file = Paths.get(path).toFile()

            if (path.endsWith(".txt")) {
                // It's a text file: ensure parent folder exists, then create empty file if missing
                file.parentFile?.mkdirs()
                if (!file.exists()) {
                    println("Creating missing file: ${file.absolutePath}")
                    file.createNewFile()
                }
            } else {
                // It's a directory: create directory if missing
                if (!file.exists()) {
                    println("Creating missing directory: ${file.absolutePath}")
                    file.mkdirs()
                }
            }
        }
    }

    private val _shouldRunModel = MutableStateFlow(false)

    fun triggerModelRun() {
        _shouldRunModel.value = true
    }

    fun runModelIfTriggered() {
        if (_shouldRunModel.value) {
            _shouldRunModel.value = false
            viewModelScope.launch {
                establishFolderIntegrity()
                withContext(Dispatchers.IO) {
                    modelRunner.runModel()
                }
            }
        }
    }

    private val _infoMode = MutableStateFlow(false)
    val infoMode: StateFlow<Boolean> = _infoMode

    fun toggleInfoMode() {
        _infoMode.update { !it }
    }

    fun setInfoMode(state: Boolean) {
        _infoMode.update { state }
    }



}