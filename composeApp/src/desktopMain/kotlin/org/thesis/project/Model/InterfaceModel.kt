package org.thesis.project.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

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

data class UploadFileMetadata(val filePath: String, var title: String, var modality: String, var region: String, var model: AIModel? = null)
data class AIModel(val title: String, val description: String, val inputModality: String, val outputModality: String)
data class NiftiData(val width: Int, val height: Int, val depth: Int, val voxelSpacing: List<Float>, val modality: String = "", var region: String = "", val voxelVolume: Array<Array<Array<Float>>>, var coronalVoxelSlices: Array<Array<Array<Float>>> = emptyArray(), var sagittalVoxelSlices: Array<Array<Array<Float>>> = emptyArray())

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

}