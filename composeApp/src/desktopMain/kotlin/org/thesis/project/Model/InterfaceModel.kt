package org.thesis.project.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.nio.file.Paths

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
    //PREV_VIEWED_PATH("src/desktopMain/resources/prev_view.xml"),
    SAVED_MAPPING("src/desktopMain/resources/saved_mapping.txt"),
    SERVER_IP("http://localhost:8000");

    override fun toString(): String = path
}

@Serializable
data class UploadFileMetadata(
    val filePath: String,
    var title: String,
    var modality: String,
    var region: String,
    var model: AIModel? = null,
    val groundTruthFilePath: String
)

@Serializable
data class AIModel(val id: Int, val title: String, val description: String, val inputModality: String, val outputModality: String, val region: String)

data class NiftiData(
    val width: Int,
    val height: Int,
    val depth: Int,
    var voxelSpacing: List<Float>,
    val modality: String = "",
    var region: String = "",
    val voxelVolume: Array<Array<Array<Float>>>,
    var coronalVoxelSlices: Array<Array<Array<Float>>> = emptyArray(),
    var sagittalVoxelSlices: Array<Array<Array<Float>>> = emptyArray(),
    var npy_path: String = "",
    var gz_path: String = "",
    var name: String = "",
) {
    fun toSlim(): NiftiDataSlim {
        return NiftiDataSlim(
            width = this.width,
            height = this.height,
            depth = this.depth,
            voxelSpacing = this.voxelSpacing,
            modality = this.modality,
            region = this.region,
            npy_path = this.npy_path,
            gz_path = this.gz_path,
            name = this.name
        )
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
    val width: Int,
    val height: Int,
    val depth: Int,
    val voxelSpacing: List<Float>,
    val modality: String = "",
    val region: String = "",
    val npy_path: String = "",
    val gz_path: String = "",
    var name: String = ""
)

@Serializable
data class FileMappingFull(
    val title: String,
    val inputs: List<NiftiDataSlim>,
    val outputs: List<NiftiDataSlim>
)


class InterfaceModel : ViewModel() {
    val niftiRepo = NiftiRepo()
    val fileUploader = FileUploadController(niftiRepo)
    val panelLayout = PanelLayoutController()

    val modelRunner = ModelRunner(niftiRepo, fileUploader)
    val imageController = ImageController(niftiRepo, viewModelScope)


    private val _organs = MutableStateFlow(listOf("Liver", "Heart", "Lung", "Kidney", "Brain"))
    val organs: StateFlow<List<String>> = _organs

    private val _selectedDistricts = MutableStateFlow<Set<String>>(setOf())
    val selectedDistricts: StateFlow<Set<String>> = _selectedDistricts

    fun updateSelectedDistrict(label: String, isSelected: Boolean) {
        _selectedDistricts.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }

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







}