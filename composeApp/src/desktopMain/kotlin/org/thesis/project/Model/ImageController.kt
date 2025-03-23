package org.thesis.project.Model

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.thesis.project.Components.VoxelImageUIState
import java.awt.Point
import kotlin.math.floor
import kotlin.math.sqrt

class ImageController(private val niftiRepo: NiftiRepo, private val scope: CoroutineScope) {

    private val _scrollStep = MutableStateFlow(0f)
    val scrollStep: StateFlow<Float> = _scrollStep

    private val _maxSelectedImageIndex = MutableStateFlow<Map<String, Float>>(emptyMap())
    val maxSelectedImageIndex: StateFlow<Map<String, Float>> = _maxSelectedImageIndex


    fun getImageIndices(filename: String): StateFlow<Triple<Int, Int, Int>> {
        return combine(scrollStep, niftiRepo.niftiImages) { step, imagesMap ->
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
        }.stateIn(scope, SharingStarted.Lazily, Triple(0, 0, 0))
    }


    fun incrementScrollPosition() {
        _scrollStep.update { it + 1f }
    }

    fun setScrollPosition(value: Float) {
        _scrollStep.update { current ->
            if (current != value) value else current
        }
    }

    fun decrementScrollPosition() {
        _scrollStep.update { (it - 1f).coerceAtLeast(0f) } // **Finer decrement**
    }


    private val _selectedViews = MutableStateFlow<Set<NiftiView>>(setOf())
    val selectedViews: StateFlow<Set<NiftiView>> = _selectedViews

    fun updateSelectedViews(label: NiftiView, isSelected: Boolean) {
        _selectedViews.update { currentSet ->
            if (isSelected) currentSet + label else currentSet - label
        }
    }

    //Currently selected niftiData by name
    private val _selectedData = MutableStateFlow<Set<String>>(setOf())
    val selectedData: StateFlow<Set<String>> = _selectedData


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


    //Windowing

    val windowPresets = mapOf(
        "CT - Brain" to WindowingParams(40f, 80f),
        "CT - Lung" to WindowingParams(-600f, 1500f),
        "CT - Bone" to WindowingParams(400f, 2000f),
        "PET SUV" to WindowingParams(5f, 10f)
    )

    data class WindowingParams(
        val center: Float,
        val width: Float
    )

    private val _windowing = MutableStateFlow(WindowingParams(center = 40f, width = 80f))
    val windowing: StateFlow<WindowingParams> = _windowing.asStateFlow()

    fun setWindowing(center: Float, width: Float) {
        _windowing.value = WindowingParams(center, width)
    }

    fun setPreset(preset: WindowingParams) {
        _windowing.value = preset
    }
}