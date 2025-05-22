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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import org.nd4j.linalg.api.ndarray.INDArray
import kotlin.math.roundToInt


class ImageController(private val scope: CoroutineScope) {

    private val _scrollStep = MutableStateFlow(0f)
    val scrollStep: StateFlow<Float> = _scrollStep

    private val _maxSelectedImageIndex = MutableStateFlow<Map<String, Float>>(emptyMap())
    val maxSelectedImageIndex: StateFlow<Map<String, Float>> = _maxSelectedImageIndex


//    fun getImageIndices(data: NiftiData): StateFlow<Triple<Int, Int, Int>> {
//        return scrollStep.map { step ->
//
//            val axialSize = data.voxelVolume.size
//            val coronalSize = data.coronalVoxelSlices.size
//            val sagittalSize = data.sagittalVoxelSlices.size
//
//            val maxLength = listOf(axialSize, coronalSize, sagittalSize).maxOrNull() ?: 1
//
//            _maxSelectedImageIndex.update { currentMap ->
//                currentMap.toMutableMap().apply { put(data.id, maxLength.toFloat()) }
//            }
//
//            val axialIndex = ((step * axialSize) / maxLength).toInt().coerceIn(0, axialSize - 1)
//            val coronalIndex = ((step * coronalSize) / maxLength).toInt().coerceIn(0, coronalSize - 1)
//            val sagittalIndex = ((step * sagittalSize) / maxLength).toInt().coerceIn(0, sagittalSize - 1)
//
//            Triple(axialIndex, coronalIndex, sagittalIndex)
//
//        }.stateIn(scope, SharingStarted.Lazily, Triple(0, 0, 0))
//    }

    fun getImageIndicesInd(data: NiftiData): StateFlow<Triple<Int, Int, Int>> {
        return scrollStep.map { step ->

            val shape = data.voxelVolume_ind.shape()

            val axialMax = shape[0].toInt() - 1
            val coronalMax = shape[1].toInt() - 1
            val sagittalMax = shape[2].toInt() - 1

            _maxSelectedImageIndex.update { currentMap ->
                currentMap.toMutableMap().apply {
                    put(data.id, listOf(axialMax, coronalMax, sagittalMax).maxOrNull()?.toFloat() ?: 1f)
                }
            }

            val axialIndex = step.toInt().coerceIn(0, axialMax)
            val coronalIndex = step.toInt().coerceIn(0, coronalMax)
            val sagittalIndex = step.toInt().coerceIn(0, sagittalMax)

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
        _scrollStep.update { (it - 1f).coerceAtLeast(0f) }
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


    fun updateSelectedData(id: String, isSelected: Boolean) {
        _selectedData.update { currentSet ->
            if (isSelected) currentSet + id else currentSet - id
        }
        if (!isSelected) {
            _maxSelectedImageIndex.update { currentMap ->
                currentMap.toMutableMap().apply { remove(id) } //Apply returns the modified map
            }
        }
    }

    fun removeSelectedData(id: String) {
        _selectedData.update { currentSet ->
            currentSet - id
        }
        _maxSelectedImageIndex.update { currentMap ->
            currentMap.toMutableMap().apply { remove(id) }
        }
    }

    fun calculateDistanceInd(
        uiState: MutableState<VoxelImageUIState>,
        position: Offset,
        scaleFactor: Float,
        bitmap: ImageBitmap,
        pixelSpacing: Float,
        voxelSlice: INDArray // <-- CHANGED
    ) {
        val voxelData = getVoxelInfoInd(
            position = position,
            scaleFactor = scaleFactor,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height,
            voxelSlice = voxelSlice
        )

        voxelData?.let { data ->
            val newPoint = Point(data.x, data.y)

            val updatedState = when {
                uiState.value.point1 == null -> uiState.value.copy(point1 = newPoint)
                uiState.value.point2 == null -> uiState.value.copy(point2 = newPoint)
                else -> uiState.value.copy(point1 = newPoint, point2 = null)
            }

            val distance = if (updatedState.point1 != null && updatedState.point2 != null) {
                calculateVoxelDistance(
                    updatedState.point1,
                    updatedState.point2,
                    pixelSpacing,
                    pixelSpacing
                )
            } else null

            uiState.value = updatedState.copy(distance = distance)
        }
    }

    fun getVoxelInfoInd(
        position: Offset,
        scaleFactor: Float,
        imageWidth: Int,
        imageHeight: Int,
        voxelSlice: INDArray
    ): VoxelData? {
        val clickX = floor(position.x / scaleFactor).toInt()
        val clickY = floor(position.y / scaleFactor).toInt()

        val outOfBounds = clickX < 0 || clickX >= imageWidth || clickY < 0 || clickY >= imageHeight
        if (outOfBounds) return null

        // 🔥 Undo the 90° counterclockwise rotation:
        val originalX = imageHeight - clickY - 1
        val originalY = clickX

        if (originalX !in 0 until voxelSlice.shape()[0] || originalY !in 0 until voxelSlice.shape()[1]) {
            return null  // prevent crash if out of bounds
        }

        val voxelValue = voxelSlice.getFloat(originalX.toLong(), originalY.toLong())

        return VoxelData(originalX, originalY, position, voxelValue)
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


    val windowPresets = mapOf(
        "CT - Brain" to WindowingParams(40f, 80f),
        "CT - Lung" to WindowingParams(-600f, 1500f),
        "CT - Bone" to WindowingParams(300f, 1500f),
        "PET SUV" to WindowingParams(2f, 5f)
    )

    data class WindowingParams(
        val center: Float,
        val width: Float
    )

    val windowingMap = mutableMapOf<String, MutableState<WindowingParams>>()

    fun getWindowingState(data: String): State<WindowingParams> {
        return windowingMap.getOrPut(data) {
            mutableStateOf(WindowingParams(40f, 1000f))
        }
    }

    private fun getWindowingMutableState(data: String): MutableState<WindowingParams> {
        return windowingMap.getOrPut(data) {
            mutableStateOf(WindowingParams(40f, 1000f))
        }
    }


    fun setWindowingCenter(center: Float, data: String) {
        val state = getWindowingMutableState(data)
        state.value = state.value.copy(center = center)
    }

    fun setWindowingWidth(width: Float, data: String) {
        val state = getWindowingMutableState(data)
        state.value = state.value.copy(width = width)
    }

    fun setPreset(preset: WindowingParams, data: String) {
        val state = getWindowingMutableState(data)
        state.value = preset
    }
}