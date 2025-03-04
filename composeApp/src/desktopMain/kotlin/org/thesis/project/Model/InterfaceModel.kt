package org.thesis.project.Model
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InterfaceModel : ViewModel() {

    // Define initial values as constants or properties
    private val initialLeftPanelWidth: Dp = 400.dp
    private val initialRightPanelWidth: Dp = 300.dp

    // Existing data flows
    private val _nestedData = MutableStateFlow(
        listOf(
            "CT_Patient1" to listOf("PET_Patient1", "MRI_Patient1"),
            "CT_Patient2" to listOf("PET_Patient2", "MRI_Patient2")
        )
    )
    val nestedData: StateFlow<List<Pair<String, List<String>>>> = _nestedData

    private val _organs = MutableStateFlow(listOf("Liver", "Heart", "Lung", "Kidney", "Brain"))
    val organs: StateFlow<List<String>> = _organs

    private val _selectedDistricts = MutableStateFlow<Set<String>>(setOf())
    val selectedDistricts: StateFlow<Set<String>> = _selectedDistricts

    private val _selectedData = MutableStateFlow<Set<String>>(setOf())
    val selectedData: StateFlow<Set<String>> = _selectedData

    private val _selectedSettings = MutableStateFlow<Set<String>>(setOf())
    val selectedSettings: StateFlow<Set<String>> = _selectedSettings

    fun updateSelectedData(label: String, isSelected: Boolean) {
        _selectedData.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }

    fun updateSelectedSettings(label: String, isSelected: Boolean) {
        _selectedSettings.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }

    fun updateSelectedDistrict(label: String, isSelected: Boolean) {
        _selectedDistricts.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }

    // Panel layout state using Dp
    private val _leftPanelWidth = MutableStateFlow(initialLeftPanelWidth)
    val leftPanelWidth: StateFlow<Dp> = _leftPanelWidth

    private val _rightPanelWidth = MutableStateFlow(initialRightPanelWidth)
    val rightPanelWidth: StateFlow<Dp> = _rightPanelWidth

    private val _leftPanelExpanded = MutableStateFlow(true)
    val leftPanelExpanded: StateFlow<Boolean> = _leftPanelExpanded

    private val _rightPanelExpanded = MutableStateFlow(true)
    val rightPanelExpanded: StateFlow<Boolean> = _rightPanelExpanded

    fun updateLeftPanelWidth(newWidth: Dp) {
        _leftPanelWidth.value = newWidth
    }

    fun updateRightPanelWidth(newWidth: Dp) {
        _rightPanelWidth.value = newWidth
    }

    fun toggleLeftPanelExpanded() {
        _leftPanelExpanded.value = !_leftPanelExpanded.value
    }

    fun toggleRightPanelExpanded() {
        _rightPanelExpanded.value = !_rightPanelExpanded.value
    }


    private val _selectedImageIndex = MutableStateFlow(0)
    val selectedImageIndex: StateFlow<Int> = _selectedImageIndex

    fun updateSelectedImageIndex(newIndex: Int) {
        _selectedImageIndex.value = newIndex
    }

    fun incrementSelectedImageIndex(max: Int) {
        _selectedImageIndex.update { (it + 1).coerceAtMost(max) }
    }

    fun decrementSelectedImageIndex() {
        _selectedImageIndex.update { (it - 1).coerceAtLeast(0) }
    }

}