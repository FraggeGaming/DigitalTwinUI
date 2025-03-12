package org.thesis.project.Model

import NiftiData
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import parseNiftiImages
import removeNiiExtension
import runNiftiParser
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.floor

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
                val niftiData = extractModality(file)?.let { it1 -> parseNiftiImages(it, it1) }
                println("Parsing: $file")
                val fileName = removeNiiExtension(File(file).nameWithoutExtension)

                if (niftiData != null) {
                    inputFilenames.add(fileName)
                    storeNiftiImages(fileName, niftiData)
                }
                println("Stored NIfTI images for: $fileName")
            }
        }

        outputNiftiFile.forEach { file ->

            println("Running NIfTI Parser for: $file")

            val output = runNiftiParser(file)

            output.let {
                val niftiData = extractModality(file)?.let { it1 -> parseNiftiImages(it, it1) }
                val fileName = removeNiiExtension(File(file).nameWithoutExtension)


                if (niftiData != null) {
                    outputFilenames.add(fileName)
                    storeNiftiImages(fileName, niftiData)
                    preloadSlicesInBackground(niftiData)
                }
                println("Stored NIfTI images for: $fileName")
            }
        }

        println("Added mapping for $title : $inputFilenames, $outputFilenames")
        addFileMapping(title, inputFilenames, outputFilenames)


    }

    fun extractModality(filename: String): String? {
        // First try to extract known modalities
        val knownModalities = listOf("CT", "PET", "MR", "MRI", "T1", "T2", "FLAIR")
        val regexKnown = Regex("_(\\w+)(?=\\.nii\\.gz$)", RegexOption.IGNORE_CASE)

        val match = regexKnown.find(filename)
        val modality = match?.groupValues?.get(1)

        // Return if it's a known modality
        if (modality != null && knownModalities.any { it.equals(modality, ignoreCase = true) }) {
            return modality.uppercase()
        }

        // Fallback: extract whatever is between last `_` and `.nii`
        return ""
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

    fun getNiftiImages(filename: String): NiftiData? {
        val niftiData = _niftiImages.value[filename] ?: return null
        return niftiData
    }

    private val _maxSelectedImageIndex = MutableStateFlow<Map<String, Float>>(emptyMap())
    val maxSelectedImageIndex: StateFlow<Map<String, Float>> = _maxSelectedImageIndex

    fun applyAutoWindowing(voxelSlice: List<List<Float>>): BufferedImage {
        val allValues = voxelSlice.flatten()
        val min = allValues.minOrNull() ?: 0f
        val max = allValues.maxOrNull() ?: 1f
        val range = max - min

        val height = voxelSlice.size
        val width = voxelSlice.firstOrNull()?.size ?: 0
        val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
        val raster = image.raster

        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = voxelSlice[y][x].coerceIn(min, max)
                val scaled = ((value - min) / range * 255f).toInt().coerceIn(0, 255)
                raster.setSample(x, y, 0, scaled)
            }
        }

        return image
    }


    fun transformToCoronalSlices(voxelVolume: List<List<List<Float>>>): List<List<List<Float>>> {
        val depth = voxelVolume.size
        val height = voxelVolume[0].size
        val width = voxelVolume[0][0].size

        return List(height) { y ->
            List(depth) { z ->
                List(width) { x ->
                    voxelVolume[z][y][x]
                }
            }
        }
    }

    fun transformToSagittalSlices(voxelVolume: List<List<List<Float>>>): List<List<List<Float>>> {
        val depth = voxelVolume.size
        val height = voxelVolume[0].size
        val width = voxelVolume[0][0].size

        return List(width) { x ->
            List(depth) { z ->
                List(height) { y ->
                    voxelVolume[z][y][x]
                }
            }
        }
    }



    fun preloadSlicesInBackground(images: NiftiData) {
        CoroutineScope(Dispatchers.Default).launch {
            if (images.coronalVoxelSlices.isEmpty()) {
                images.coronalVoxelSlices = transformToCoronalSlices(images.voxel_volume)
            }
            if (images.imageSlicesCoronal.isEmpty()) {
                images.imageSlicesCoronal = images.coronalVoxelSlices.map { applyAutoWindowing(it) }
            }

            if (images.sagittalVoxelSlices.isEmpty()) {
                images.sagittalVoxelSlices = transformToSagittalSlices(images.voxel_volume)
            }
            if (images.imageSlicesSagittal.isEmpty()) {
                images.imageSlicesSagittal = images.sagittalVoxelSlices.map { applyAutoWindowing(it) }
            }
        }
    }




//    fun getSlicesFromVolume(view: NiftiView, filename: String): Pair<List<List<List<Float>>>, List<BufferedImage>> {
//        val images = getNiftiImages(filename) ?: return Pair(emptyList(), emptyList())
//        val voxelVolume = images.voxel_volume
//
//        return when (view) {
//            NiftiView.AXIAL -> {
//                val axialSlices = voxelVolume
//                if (images.imageSlicesAxial.isEmpty()) {
//                    images.imageSlicesAxial = axialSlices.map { applyAutoWindowing(it) }
//                }
//                Pair(axialSlices, images.imageSlicesAxial)
//            }
//
//            NiftiView.CORONAL -> {
//                val depth = voxelVolume.size
//                val height = voxelVolume[0].size
//                val width = voxelVolume[0][0].size
//
//                val coronalSlices = List(height) { y ->
//                    List(depth) { z ->
//                        List(width) { x ->
//                            voxelVolume[z][y][x]
//                        }
//                    }
//                }
//
//                if (images.coronalVoxelSlices.isEmpty()) {
//                    images.coronalVoxelSlices = transformToCoronalSlices(voxelVolume)
//                }
//
//
//                Pair(coronalSlices, images.imageSlicesCoronal)
//            }
//
//            NiftiView.SAGITTAL -> {
//                val depth = voxelVolume.size
//                val height = voxelVolume[0].size
//                val width = voxelVolume[0][0].size
//
//                val sagittalSlices = List(width) { x ->
//                    List(depth) { z ->
//                        List(height) { y ->
//                            voxelVolume[z][y][x]
//                        }
//                    }
//                }
//
//                if (images.imageSlicesSagittal.isEmpty()) {
//                    images.imageSlicesSagittal = sagittalSlices.map { applyAutoWindowing(it) }
//                }
//
//                Pair(sagittalSlices, images.imageSlicesSagittal)
//            }
//        }
//    }

    fun getSlicesFromVolume(view: NiftiView, filename: String): Pair<List<List<List<Float>>>, List<BufferedImage>> {
        val images = getNiftiImages(filename) ?: return Pair(emptyList(), emptyList())
        val voxelVolume = images.voxel_volume

        return when (view) {
            NiftiView.AXIAL -> {
                val axialSlices = voxelVolume
                if (images.imageSlicesAxial.isEmpty()) {
                    images.imageSlicesAxial = axialSlices.map { applyAutoWindowing(it) }
                }
                Pair(axialSlices, images.imageSlicesAxial)
            }

            NiftiView.CORONAL -> {
                if (images.coronalVoxelSlices.isEmpty()) {
                    images.coronalVoxelSlices = transformToCoronalSlices(voxelVolume)
                }
                if (images.imageSlicesCoronal.isEmpty()) {
                    images.imageSlicesCoronal = images.coronalVoxelSlices.map { applyAutoWindowing(it) }
                }
                Pair(images.coronalVoxelSlices, images.imageSlicesCoronal)
            }

            NiftiView.SAGITTAL -> {
                if (images.sagittalVoxelSlices.isEmpty()) {
                    images.sagittalVoxelSlices = transformToSagittalSlices(voxelVolume)
                }
                if (images.imageSlicesSagittal.isEmpty()) {
                    images.imageSlicesSagittal = images.sagittalVoxelSlices.map { applyAutoWindowing(it) }
                }
                Pair(images.sagittalVoxelSlices, images.imageSlicesSagittal)
            }
        }
    }








    private val _scrollStep = MutableStateFlow(0f) // Holds the global scroll step as Float
    val scrollStep: StateFlow<Float> = _scrollStep

    fun getImageIndices(filename: String): StateFlow<Triple<Int, Int, Int>> {
        return combine(scrollStep, _niftiImages) { step, imagesMap ->
            val images = imagesMap[filename]
            if (images == null) {
                return@combine Triple(0, 0, 0)
            }

            val axialSize = images.voxel_volume.size
            val coronalSize = images.voxel_volume[0].size
            val sagittalSize = images.voxel_volume[0][0].size

            val maxLength = listOf(axialSize, coronalSize, sagittalSize).maxOrNull() ?: 1

            _maxSelectedImageIndex.update { currentMap ->
                currentMap.toMutableMap().apply { put(filename, maxLength.toFloat()) }
            }

            val axialIndex = ((step * axialSize) / maxLength).toInt().coerceIn(0, axialSize - 1)
            val coronalIndex = ((step * coronalSize) / maxLength).toInt().coerceIn(0, coronalSize - 1)
            val sagittalIndex = ((step * sagittalSize) / maxLength).toInt().coerceIn(0, sagittalSize - 1)

            Triple(axialIndex, coronalIndex, sagittalIndex)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(0, 0, 0))
    }



    fun incrementScrollPosition() {
        _scrollStep.update { it + 0.5f } // **Finer step increments**
    }

    fun setScrollPosition(value: Float) {
        _scrollStep.update { current ->
            if (current != value) value else current // **Avoid redundant updates**
        }
    }

    fun decrementScrollPosition() {
        _scrollStep.update { (it - 0.5f).coerceAtLeast(0f) } // **Finer decrement**
    }


    private val _selectedViews = MutableStateFlow<Set<NiftiView>>(setOf())
    val selectedViews: StateFlow<Set<NiftiView>> = _selectedViews

    fun updateSelectedViews(label: NiftiView, isSelected: Boolean) {
        _selectedViews.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }


    fun getVoxelInfo(
        position: Offset,
        scaleFactor: Float,
        imageWidth: Int,
        imageHeight: Int,
        voxelSlice: List<List<Float>>
    ): VoxelData? {
        val x = floor(position.x / scaleFactor).toInt()
        val y = floor(position.y / scaleFactor).toInt()

        // Out-of-bounds check
        val outOfBounds = x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
        if (outOfBounds) return null

        // Prevent crash if voxelSlice is misaligned
        val voxelValue = voxelSlice.getOrNull(y)?.getOrNull(x) ?: 0f

        return VoxelData(x, y, position, voxelValue)
    }

    // Data class to store voxel information
    data class VoxelData(val x: Int, val y: Int, val position: Offset, val voxelValue: Float)

    var hoverPosition = mutableStateOf<Point?>(null)
        private set

    var voxelValue = mutableStateOf<Float?>(null)
        private set

    var cursorPosition = mutableStateOf(Offset.Zero)
        private set

    var isHovering = mutableStateOf(false)
        private set

    var lastX = mutableStateOf<Int?>(null)
        private set

    var lastY = mutableStateOf<Int?>(null)
        private set


    private fun setHoverData(data: VoxelData, localPosition: Offset) {

        lastX.value = data.x
        lastY.value = data.y

        hoverPosition.value = Point(data.x, data.y)
        voxelValue.value = data.voxelValue
        cursorPosition.value = localPosition
        isHovering.value = true

    }

    fun updatePointerPosition(
        position: Offset,
        scaleFactor: Float,
        width: Int,
        height: Int,
        currentVoxelSlice: List<List<Float>> // ← make sure this is passed in!
    ) {
        val voxelData = getVoxelInfo(position, scaleFactor, width, height, currentVoxelSlice)
        if (voxelData != null) {
            setHoverData(voxelData, position)
        }
    }
}




