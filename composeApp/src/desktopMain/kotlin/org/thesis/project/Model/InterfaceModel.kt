package org.thesis.project.Model
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import parseNifti
import parseNiftiImages
import removeNiiExtension
import runNiftiParser
import java.awt.image.BufferedImage
import java.io.File

enum class NiftiView(val displayName: String) {
    AXIAL("Axial"),
    CORONAL("Coronal"),
    SAGITTAL("Sagittal");

    override fun toString(): String = displayName
}

data class NiftiImageData(
    val axial: List<BufferedImage> = emptyList(),
    val coronal: List<BufferedImage> = emptyList(),
    val sagittal: List<BufferedImage> = emptyList()
)

data class NiftiImageJson(
    val axial: List<BufferedImage> = emptyList(),
    val coronal: List<BufferedImage> = emptyList(),
    val sagittal: List<BufferedImage> = emptyList()
)

class InterfaceModel : ViewModel() {

    // Define initial values as constants or properties
    private val initialLeftPanelWidth: Dp = 400.dp
    private val initialRightPanelWidth: Dp = 300.dp


    fun parseNiftiData(title: String, inputNiftiFile: List<String>, outputNiftiFile: List<String>){

        var inputFilename by remember { mutableStateOf<List<String>>(emptyList()) }
        var outputFilename by remember { mutableStateOf<List<String>>(emptyList()) }



        inputNiftiFile.forEachIndexed { index, file ->
            var output by remember { mutableStateOf<String?>(null) }
            var isProcessing by remember { mutableStateOf(false) }

            LaunchedEffect(file) {
                if (!isProcessing) {
                    isProcessing = true
                    println("Running NIfTI Parser for: $file")

                    output = runNiftiParser(file)

                    output?.let {
                        val niftiData = parseNiftiImages(it)
                        inputFilename = inputFilename + removeNiiExtension(File(file).nameWithoutExtension)
                        storeNiftiImages(file, niftiData.axialImages, niftiData.coronalImages, niftiData.sagittalImages)
                        println("Stored NIfTI images for: $file")
                    }
                }
            }

        }


        outputFilename.forEachIndexed { index, file ->
            var output by remember { mutableStateOf<String?>(null) }
            var isProcessing by remember { mutableStateOf(false) }

            LaunchedEffect(file) {
                if (!isProcessing) {
                    isProcessing = true
                    println("Running NIfTI Parser for: $file")

                    output = runNiftiParser(file)

                    output?.let {
                        val niftiData = parseNiftiImages(it)
                        val fileName = removeNiiExtension(File(file).nameWithoutExtension)
                        outputFilename = outputFilename + fileName

                        storeNiftiImages(fileName, niftiData.axialImages, niftiData.coronalImages, niftiData.sagittalImages)
                        println("Stored NIfTI images for: $fileName")
                    }
                }
            }

        }

        addFileMapping(title, inputNiftiFile, outputNiftiFile)
    }


    private val _fileMapping = MutableStateFlow<Map<String, Pair<List<String>, List<String>>>>(emptyMap())
    val fileMapping: StateFlow<Map<String, Pair<List<String>, List<String>>>> = _fileMapping

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

    private val _selectedData = MutableStateFlow<Set<String>>(setOf())
    val selectedData: StateFlow<Set<String>> = _selectedData

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

    private val _niftiImages = MutableStateFlow<Map<String, Triple<List<BufferedImage>, List<BufferedImage>, List<BufferedImage>>>>(emptyMap())
    val niftiImages: StateFlow<Map<String, Triple<List<BufferedImage>, List<BufferedImage>, List<BufferedImage>>>> = _niftiImages

    fun storeNiftiImages(filename: String, axial: List<BufferedImage>, coronal: List<BufferedImage>, sagittal: List<BufferedImage>) {
        _niftiImages.update { currentMap ->
            currentMap + (filename to Triple(axial, coronal, sagittal))
            //filename to NiftiData
        }
    }

    val niftiFilenames: StateFlow<List<String>> = _niftiImages.map { it.keys.toList() }.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    fun getNiftiImages(filename: String): Triple<List<BufferedImage>, List<BufferedImage>, List<BufferedImage>>? {
        return _niftiImages.value[filename]
    }

    fun getMaxIndexSizeForSelectedData(): Int {
        val valuesList: List<String> = _selectedData.value.toList()
        var maxIndex = 0

        valuesList.forEachIndexed { index, value ->
            val images = getNiftiImages(value)
            if (images != null){
                val (axial, coronal, sagittal) = images
                val size = listOf(axial.size, coronal.size, sagittal.size).maxOrNull() ?: 1


                println("Size: $size")
                if (size < maxIndex){
                    maxIndex = size
                }
            }
        }
        println("Max index: $maxIndex")

        return maxIndex
    }

    private val _maxSelectedImageIndex = MutableStateFlow<Map<String, Float>>(emptyMap())
    val maxSelectedImageIndex: StateFlow<Map<String, Float>> = _maxSelectedImageIndex


    private val _scrollStep = MutableStateFlow(0) // Holds the global scroll step
    val scrollStep: StateFlow<Int> = _scrollStep

    fun getImageIndices(filename: String): StateFlow<Triple<Int, Int, Int>> {
        return scrollStep.map { step ->
            val images = _niftiImages.value[filename] ?: return@map Triple(0, 0, 0)
            val (axial, coronal, sagittal) = images

            val maxLength = listOf(axial.size, coronal.size, sagittal.size).maxOrNull() ?: 1
            _maxSelectedImageIndex.update { currentMap ->
                currentMap.toMutableMap().apply { put(filename, maxLength.toFloat()) }
            }

            val axialIndex = ((step * axial.size) / maxLength).coerceIn(0, axial.lastIndex)
            val coronalIndex = ((step * coronal.size) / maxLength).coerceIn(0, coronal.lastIndex)
            val sagittalIndex = ((step * sagittal.size) / maxLength).coerceIn(0, sagittal.lastIndex)

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