package org.thesis.project
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InterfaceModel : ViewModel() {

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
}