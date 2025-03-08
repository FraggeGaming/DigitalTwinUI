package org.thesis.project.Model

import NiftiData
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import parseNiftiImages
import removeNiiExtension
import runNiftiParser
import java.io.File

enum class NiftiView(val displayName: String) {
    AXIAL("Axial"),
    CORONAL("Coronal"),
    SAGITTAL("Sagittal");

    override fun toString(): String = displayName
}

class InterfaceModel : ViewModel() {

    // Define initial values as constants or properties
    private val initialLeftPanelWidth: Dp = 400.dp
    private val initialRightPanelWidth: Dp = 300.dp

    //Stores the niftiData by Filename
    private val _niftiImages = MutableStateFlow<Map<String, NiftiData>>(emptyMap())
    val niftiImages: StateFlow<Map<String, NiftiData>> = _niftiImages

    //Currently selected niftiData by name
    private val _selectedData = MutableStateFlow<Set<String>>(setOf())
    val selectedData: StateFlow<Set<String>> = _selectedData

    //Connects multiple filenames together, ex: Patient_1 : List("CT_img1", "PET_real"), List("Syntet_PET")
    private val _fileMapping = MutableStateFlow<Map<String, Pair<List<String>, List<String>>>>(emptyMap())
    val fileMapping: StateFlow<Map<String, Pair<List<String>, List<String>>>> = _fileMapping


    fun parseNiftiData(title: String, inputNiftiFile: List<String>, outputNiftiFile: List<String>) {

        val inputFilenames = mutableListOf<String>()
        val outputFilenames = mutableListOf<String>()

        inputNiftiFile.forEach { file ->


            println("Running NIfTI Parser for: $file")

            val output = runNiftiParser(file)

            output.let {
                val niftiData = parseNiftiImages(it)
                println("Parsing: $file")
                val fileName = removeNiiExtension(File(file).nameWithoutExtension)
                inputFilenames.add(fileName)
                storeNiftiImages(fileName, niftiData)
                println("Stored NIfTI images for: $fileName")
            }
        }

        outputNiftiFile.forEach { file ->

            println("Running NIfTI Parser for: $file")

            val output = runNiftiParser(file)

            output.let {
                val niftiData = parseNiftiImages(it)
                val fileName = removeNiiExtension(File(file).nameWithoutExtension)
                outputFilenames.add(fileName)

                storeNiftiImages(fileName, niftiData)
                println("Stored NIfTI images for: $fileName")
            }
        }

        println("Added mapping for $title : $inputFilenames, $outputFilenames")
        addFileMapping(title, inputFilenames, outputFilenames)
    }


    //Add or update an entry in the mapping
    fun addFileMapping(key: String, firstList: List<String>, secondList: List<String>) {
        _fileMapping.update { currentMap ->
            currentMap + (key to Pair(firstList, secondList))
        }
    }

    //Retrieve a specific mapping by key
    fun getFileMapping(key: String): Pair<List<String>, List<String>>? {
        return _fileMapping.value[key]
    }

    //Get all keys (filenames)
    fun getFileMappingKeys(): List<String> {
        return _fileMapping.value.keys.toList()
    }

    //Remove an entry from the mapping
    fun removeFileMapping(key: String) {
        _fileMapping.update { currentMap ->
            currentMap - key
        }
    }

    //Check if a key exists in the mapping
    fun hasFileMapping(key: String): Boolean {
        return _fileMapping.value.containsKey(key)
    }

    private val _organs = MutableStateFlow(listOf("Liver", "Heart", "Lung", "Kidney", "Brain"))
    val organs: StateFlow<List<String>> = _organs

    private val _selectedDistricts = MutableStateFlow<Set<String>>(setOf())
    val selectedDistricts: StateFlow<Set<String>> = _selectedDistricts


    private val _selectedSettings = MutableStateFlow<Set<String>>(setOf())
    val selectedSettings: StateFlow<Set<String>> = _selectedSettings

    fun updateSelectedData(label: String, isSelected: Boolean) {
        _selectedData.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }

        if (!isSelected) {
            _maxSelectedImageIndex.update { currentMap ->
                currentMap.toMutableMap().apply { remove(label) } // Apply returns the modified map
            }
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


    fun storeNiftiImages(filename: String, data: NiftiData) {
        _niftiImages.update { currentMap ->
            currentMap + (filename to data)
            //filename to NiftiData
        }
    }

    val niftiFilenames: StateFlow<List<String>> = _niftiImages.map { it.keys.toList() }.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    fun getNiftiImages(filename: String): NiftiData? {
        val niftiData = _niftiImages.value[filename] ?: return null
        return niftiData
    }

    fun getNiftiJson(filename: String): Triple<List<String>, List<String>, List<String>>? {
        val niftiData = _niftiImages.value[filename] ?: return null
        return Triple(niftiData.axial, niftiData.coronal, niftiData.sagittal)
    }

    private val _maxSelectedImageIndex = MutableStateFlow<Map<String, Float>>(emptyMap())
    val maxSelectedImageIndex: StateFlow<Map<String, Float>> = _maxSelectedImageIndex


    private val _scrollStep = MutableStateFlow(0) // Holds the global scroll step
    val scrollStep: StateFlow<Int> = _scrollStep

    fun getImageIndices(filename: String): StateFlow<Triple<Int, Int, Int>> {
        return scrollStep.map { step ->
            val images = _niftiImages.value[filename] ?: return@map Triple(0, 0, 0)

            val maxLength = listOf(images.axial.size, images.coronal.size, images.sagittal.size).maxOrNull() ?: 1
            _maxSelectedImageIndex.update { currentMap ->
                currentMap.toMutableMap().apply { put(filename, maxLength.toFloat()) }
            }

            val axialIndex = ((step * images.axial.size) / maxLength).coerceIn(0, images.axial.lastIndex)
            val coronalIndex = ((step * images.coronal.size) / maxLength).coerceIn(0, images.coronal.lastIndex)
            val sagittalIndex = ((step * images.sagittal.size) / maxLength).coerceIn(0, images.sagittal.lastIndex)

            Triple(axialIndex, coronalIndex, sagittalIndex)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(0, 0, 0))
    }

    fun incrementScrollPosition() {
        _scrollStep.update { (it + 1) }

    }

    fun setScrollPosition(value: Float) {
        _scrollStep.update { current ->
            val newValue = value.toInt()
            if (current != newValue) newValue else current // Avoid redundant updates
        }
    }


    fun decrementScrollPosition() {
        _scrollStep.update { (it - 1).coerceAtLeast(0) }
    }

    private val _selectedViews = MutableStateFlow<Set<String>>(setOf())
    val selectedViews: StateFlow<Set<String>> = _selectedViews

    fun updateSelectedViews(label: String, isSelected: Boolean) {
        _selectedViews.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }
}