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
import java.io.File


//Definitions of the different views, used in the UI and for logic
enum class NiftiView(val displayName: String) {
    AXIAL("Axial"),
    CORONAL("Coronal"),
    SAGITTAL("Sagittal");

    override fun toString(): String = displayName
}

//Definition of the user settings, used in UI and in logic
enum class Settings(val settingName: String) {
    PIXEL("Pixel Value"),
    MEASUREMENT("Measurement");

    override fun toString(): String = settingName
}

//Definitions of paths
enum class PathStrings(val path: String) {
    OUTPUT_PATH_GZ("external/output_gz"),
    OUTPUT_PATH_NPY("external/output_npy"),
    INPUT_PATH_GZ("external/input_gz"),
    SAVED_MAPPING("external/saved_mapping.txt"),
    CONFIG_FILE("external/config.properties");

    override fun toString(): String = path
}

//Dataclass for the upload of files, used for parsing and is sent to the server
@Serializable
data class UploadFileMetadata(
    var filePath: String,
    var title: String,
    var modality: String,
    var region: String,
    var model: AIModel? = null,
    val groundTruthFilePath: String,
)

//Dataclass for the AI model object that gets returned from the server
@Serializable
data class AIModel(val id: Int, val title: String, val description: String, val inputModality: String, val outputModality: String, val region: String)

//NIfTI data main class. Used across the application. This is class that has the path to the file, and also contains metadata
data class NiftiData(
    val id: String = UUID.randomUUID().toString(),
    val width: Int,
    val height: Int,
    val depth: Int,
    var voxelSpacing: List<Float>,
    val modality: String = "",
    var region: String = "",
    var voxelVolume_ind: INDArray,
    var intensity_max: Float,
    var intensity_min: Float,
    var npy_path: String = "",
    var gz_path: String = "",
    var name: String = "",
) {
    //Slim nifti for storage in json
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

    //Manually clear the voxelvolume on destruction
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

//Metadata class for nifti
@Serializable
data class NiftiMeta(
    val width: Int,
    val height: Int,
    val depth: Int,
    val voxel_spacing: List<Float>,
    val npy_path: String
)

//Definition of the slim nifti dataclass for storage
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


//Contains the template to store the loaded images, and to load, and remove
@Serializable
data class FileMappingFull(
    val title: String,
    val inputs: List<NiftiDataSlim>,
    val outputs: List<NiftiDataSlim>
)

//Object to configure the config.properties file, and set a default value
object Config {
    var serverIp: String = "http://localhost:8000" // default

    fun load(baseDir: File) {
        val configFile = File(baseDir, PathStrings.CONFIG_FILE.path)
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            configFile.writeText(
                """
                # App Configuration
                server_ip=http://localhost:8000
                """.trimIndent()
            )
            println("Created default config at: ${configFile.absolutePath}")
        }

        val props = Properties()
        props.load(configFile.inputStream())
        serverIp = props.getProperty("server_ip") ?: serverIp
        println("Loaded server IP: $serverIp")
    }
}

/**
 * The main model class, inherited from viewmodel. Holds all the other models
 * */
class InterfaceModel : ViewModel() {

    val baseDir: File get() = File(System.getProperty("user.dir"))

    //Make sure directories and folders exist
    init {
        resolvePath(PathStrings.OUTPUT_PATH_GZ)
        resolvePath(PathStrings.OUTPUT_PATH_NPY)
        resolvePath(PathStrings.INPUT_PATH_GZ)
        resolvePath(PathStrings.SAVED_MAPPING)

        Config.load(baseDir)
    }

    fun resolvePath(path: PathStrings): File {
        val file = File(baseDir, path.path)

        val shouldCreateDir = !file.exists() && !path.path.endsWith(".txt")

        if (shouldCreateDir) {
            file.mkdirs()
            println("Created directory: ${file.absolutePath}")
        } else if (!file.exists() && file.parentFile != null) {
            file.parentFile.mkdirs()
            println("Created parent directory: ${file.parentFile.absolutePath}")
        }

        return file
    }

    //Decleration of the models
    val imageController = ImageController(viewModelScope)
    val niftiRepo = NiftiRepo(imageController, resolvePath(PathStrings.SAVED_MAPPING))
    val fileUploader = FileUploadController(niftiRepo, resolvePath(PathStrings.OUTPUT_PATH_NPY), baseDir)
    val panelLayout = PanelLayoutController()
    val modelRunner = ModelRunner(niftiRepo, fileUploader, resolvePath(PathStrings.OUTPUT_PATH_GZ), resolvePath(PathStrings.INPUT_PATH_GZ))


    private val _regions = MutableStateFlow<List<String>>(listOf())
    val regions: StateFlow<List<String>> = _regions

    private val _modalities = MutableStateFlow<List<String>>(listOf())
    val modalities: StateFlow<List<String>> = _modalities

    fun resetRegionsAndModalities() {
        Config.load(baseDir)
        _regions.value = listOf()
        _modalities.value = listOf()
    }

    //Get regions from server
    suspend fun fetchRegions() = coroutineScope{
        val regions = fetchAvailableRegions(Config.serverIp)
        println("regions: $regions")
        _regions.value = regions ?: listOf("Head", "Lung", "Brain", "Liver", "Total Body")
    }

    //Get modalities from server
    suspend fun fetchModalities() = coroutineScope{
        val modalities = fetchAvailableModalities(Config.serverIp)
        _modalities.value = modalities ?: listOf("CT", "PET", "MRI")
    }

    private val _selectedSettings = MutableStateFlow<Set<Settings>>(setOf())
    val selectedSettings: StateFlow<Set<Settings>> = _selectedSettings

    fun updateSelectedSettings(label: Settings, isSelected: Boolean) {
        _selectedSettings.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }

    private val _shouldRunModel = MutableStateFlow(false)

    //Trigger the modelrunner to run if new data has been appended
    fun triggerModelRun() {
        _shouldRunModel.value = true
    }

    fun runModelIfTriggered(): Boolean {
        if (_shouldRunModel.value) {
            _shouldRunModel.value = false
            viewModelScope.launch {
                //establishFolderIntegrity()
                withContext(Dispatchers.IO) {
                    modelRunner.runModel()
                }
            }

            return true
        }

        return false
    }

    //Toggle if the info buttons should show in the UI
    private val _infoMode = MutableStateFlow(false)
    val infoMode: StateFlow<Boolean> = _infoMode

    fun toggleInfoMode() {
        _infoMode.update { !it }
    }

    fun setInfoMode(state: Boolean) {
        _infoMode.update { state }
    }
}