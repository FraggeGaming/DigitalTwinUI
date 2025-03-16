package org.thesis.project.Model

import NiftiData
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.thesis.project.Components.VoxelImageUIState
import parseNiftiImages
import removeNiiExtension
import runNiftiParser
import java.awt.Point
import java.io.File
import kotlin.math.floor
import kotlin.math.sqrt

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


    suspend fun parseNiftiData(title: String, inputNiftiFile: List<String>, outputNiftiFile: List<String>) {

        val inputFilenames = loadNifti(inputNiftiFile)
        val outputFilenames = loadNifti(outputNiftiFile)

        println("Added mapping for $title : $inputFilenames, $outputFilenames")
        addFileMapping(title, inputFilenames, outputFilenames)
    }

    suspend fun loadNifti(niftiStorage: List<String>): MutableList<String> {
        val fileNames = mutableListOf<String>()
        coroutineScope {
            niftiStorage.map { file ->
                launch(Dispatchers.IO) {
                    println("Running NIfTI Parser for: $file")
                    //val output = runNiftiParser(file)

                    val outputJson = runNiftiParser(
                        file,
                        "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\ouput"
                    )
                    val niftiData = extractModality(file)?.let { parseNiftiImages(outputJson, it) }

                    val fileName = removeNiiExtension(File(file).nameWithoutExtension)

                    niftiData?.let {
                        synchronized(fileNames) { fileNames.add(fileName) }
                        storeNiftiImages(fileName, it)
                        println("Stored NIfTI images for: $fileName")
                    }
                }
            }.joinAll()
        }

        return fileNames
    }


    fun extractModality(filename: String): String? {
        val knownModalities = listOf("CT", "PET", "MR", "MRI", "T1", "T2", "FLAIR")
        val upperFilename = filename.uppercase()

        return knownModalities.firstOrNull { modality ->
            upperFilename.contains(modality)
        }
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


    fun getSlicesFromVolume(view: NiftiView, filename: String): Pair<Array<Array<Array<Float>>>, Float> {
        val images = getNiftiImages(filename) ?: return Pair(emptyArray(), 1f)
        val spacing = images.voxelSpacing

        //because of transpose when parsing nifti, (Z, Y, X) → (X, Y, Z), but we don't transpose spacing
        return when (view) {
            NiftiView.AXIAL -> {
                images.voxelVolume to spacing[2]
            }

            NiftiView.CORONAL -> {
                images.coronalVoxelSlices to spacing[1]
            }

            NiftiView.SAGITTAL -> {
                images.sagittalVoxelSlices to spacing[0]
            }
        }
    }


    private val _scrollStep = MutableStateFlow(0f)
    val scrollStep: StateFlow<Float> = _scrollStep

    fun getImageIndices(filename: String): StateFlow<Triple<Int, Int, Int>> {
        return combine(scrollStep, _niftiImages) { step, imagesMap ->
            val images = imagesMap[filename] ?: return@combine Triple(0, 0, 0)

            val axialSize = images.voxelVolume.size
            val coronalSize = images.voxelVolume[0].size
            val sagittalSize = images.voxelVolume[0][0].size

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
        _scrollStep.update { it + 0.5f }
    }

    fun setScrollPosition(value: Float) {
        _scrollStep.update { current ->
            if (current != value) value else current
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

    fun calculateDistance(
        uiState: MutableState<VoxelImageUIState>,
        position: Offset,
        scaleFactor: Float,
        bitmap: ImageBitmap,
        pixelSpacing: Float,
        voxelSlice: Array<Array<Float>>
    ) {
        val voxelData = getVoxelInfo(
            position = position,
            scaleFactor = scaleFactor,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height,
            voxelSlice = voxelSlice
        )


        voxelData?.let { data ->
            val newPoint = Point(data.x, data.y)

            // Update points based on current state
            val updatedState = when {
                uiState.value.point1 == null -> uiState.value.copy(point1 = newPoint)
                uiState.value.point2 == null -> uiState.value.copy(point2 = newPoint)
                else -> uiState.value.copy(point1 = newPoint, point2 = null)
            }

            // Recalculate distance if both points are set
            val distance = if (updatedState.point1 != null && updatedState.point2 != null) {
                calculateVoxelDistance(
                    updatedState.point1,
                    updatedState.point2,
                    pixelSpacing,
                    pixelSpacing
                )
            } else null

            // Update the final state with the distance
            uiState.value = updatedState.copy(distance = distance)
        }
    }

    //add to interface model


    fun getVoxelInfo(
        position: Offset,
        scaleFactor: Float,
        imageWidth: Int,
        imageHeight: Int,
        voxelSlice: Array<Array<Float>>
    ): VoxelData? {
        val x = floor(position.x / scaleFactor).toInt()
        val y = floor(position.y / scaleFactor).toInt()

        val outOfBounds = x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
        if (outOfBounds) return null

        val voxelValue = voxelSlice.getOrNull(y)?.getOrNull(x) ?: 0f
        //println("Voxel XY: ($x, $y) Value: $voxelValue")

        return VoxelData(x, y, position, voxelValue)
    }

    data class VoxelData(val x: Int, val y: Int, val position: Offset, val voxelValue: Float)

    /**
     * Calculates the voxel-space distance, optionally scaled with pixelSpacing (e.g., in mm).
     *
     * @param pixelSpacingX: distance between voxels in X direction (e.g. mm per pixel)
     * @param pixelSpacingY: distance between voxels in Y direction
     * @return Distance in scaled units (e.g., mm), or null if points are missing
     */
    fun calculateVoxelDistance(
        point1: Point?,
        point2: Point?,
        pixelSpacingX: Float = 1f,
        pixelSpacingY: Float = 1f
    ): Double? {
        if (point1 == null || point2 == null) return null

        val dx = (point1.x - point2.x) * pixelSpacingX
        val dy = (point1.y - point2.y) * pixelSpacingY

        return sqrt(dx * dx + dy * dy.toDouble())
    }
}