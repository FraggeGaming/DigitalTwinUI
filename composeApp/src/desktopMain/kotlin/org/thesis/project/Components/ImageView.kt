package org.thesis.project.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.thesis.project.Model.InterfaceModel
import java.awt.Point
import java.awt.image.BufferedImage

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun voxelImageDisplay(
    modifier: Modifier = Modifier,
    voxelSlice: Array<Array<Float>>,
    interfaceModel: InterfaceModel,
    modality: String,
    pixelSpacing: Float = 1f,
) {
    val uiState = remember { mutableStateOf(VoxelImageUIState()) }
    val selectedSettings by interfaceModel.selectedSettings.collectAsState()
    val windowing by interfaceModel.windowing.collectAsState()
    val bitmap = voxelSliceToBitmap(voxelSlice, windowing.center, windowing.width)

    var imageLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var boxCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var renderedImageSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { boxCoordinates = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { localPos ->
                        if (selectedSettings.contains("measure")) {
                            val correctedPos = mapToImageCoordinatesAspectAware(
                                rawPointerPos = localPos,
                                boxSize = renderedImageSize,
                                bitmap = bitmap
                            )
                            interfaceModel.calculateDistance(
                                uiState,
                                correctedPos,
                                1f,
                                bitmap,
                                pixelSpacing,
                                voxelSlice
                            )
                        }
                    },
                    onLongPress = {
                        uiState.value = uiState.value.copy(point1 = null, point2 = null, distance = null)
                    }
                )
            }
            .onPointerEvent(PointerEventType.Move) { event ->
                if (uiState.value.isHovering) {
                    val localPos = event.changes.first().position
                    uiState.value = uiState.value.copy(cursorPosition = localPos)

                    val correctedPos = mapToImageCoordinatesAspectAware(
                        rawPointerPos = localPos,
                        boxSize = renderedImageSize,
                        bitmap = bitmap
                    )

                    val voxelData = interfaceModel.getVoxelInfo(
                        position = correctedPos,
                        scaleFactor = 1f,
                        imageWidth = bitmap.width,
                        imageHeight = bitmap.height,
                        voxelSlice = voxelSlice
                    )

                    uiState.value = uiState.value.copy(
                        hoverVoxelValue = voxelData?.voxelValue,
                        hoverVoxelPosition = voxelData?.let { Point(it.x, it.y) }
                    )
                }
            }
            .onPointerEvent(PointerEventType.Enter) {
                uiState.value = uiState.value.copy(isHovering = true)
            }
            .onPointerEvent(PointerEventType.Exit) {
                uiState.value = uiState.value.copy(
                    isHovering = false,
                    hoverVoxelValue = null,
                    hoverVoxelPosition = null
                )
            }
    ) {

        // Compute actual image rendering area
        val imageAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        val boxAspect = renderedImageSize.width.toFloat() / renderedImageSize.height.toFloat()

        val renderWidth: Int
        val renderHeight: Int
        val offsetX: Float
        val offsetY: Float

        if (boxAspect > imageAspect) {
            renderHeight = renderedImageSize.height
            renderWidth = (renderHeight * imageAspect).toInt()
            offsetX = ((renderedImageSize.width - renderWidth) / 2f)
            offsetY = 0f
        } else {
            renderWidth = renderedImageSize.width
            renderHeight = (renderWidth / imageAspect).toInt()
            offsetX = 0f
            offsetY = ((renderedImageSize.height - renderHeight) / 2f)
        }

//        val cardModifier = if (imageSize.width > 0 && imageSize.height > 0) {
//            Modifier.size(
//                width = imageSize.width.dp,
//                height = imageSize.height.dp
//            )
//        } else {
//            Modifier.fillMaxSize() // allow first card to size itself
//        }
//
//        standardCard(
//            modifier = cardModifier,
//
//            contentAlignment = Alignment.CenterHorizontally,
//            content = {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Voxel image",
                    modifier = Modifier.fillMaxSize() //wrapcontent
                        .onSizeChanged {
                            renderedImageSize = it
//                            interfaceModel.setImageCardSize(IntSize(renderWidth, renderHeight))
//                            println("Voxel image: ${renderWidth} ${renderHeight}")
                        }
                        .onGloballyPositioned { imageLayoutCoordinates = it }
                )

//            }
//        )



        // Canvas overlay drawn exactly over rendered image
        Canvas(
            modifier = Modifier
                .size(with(LocalDensity.current) { renderWidth.toDp() }, with(LocalDensity.current) { renderHeight.toDp() })
                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
        ) {
            val scaleX = size.width / bitmap.width
            val scaleY = size.height / bitmap.height

            uiState.value.point1?.let { p1 ->
                drawCircle(
                    color = Color.Green,
                    radius = 5.dp.toPx(),
                    center = Offset(p1.x * scaleX, p1.y * scaleY)
                )
            }

            uiState.value.point2?.let { p2 ->
                drawCircle(
                    color = Color.Green,
                    radius = 5.dp.toPx(),
                    center = Offset(p2.x * scaleX, p2.y * scaleY)
                )
                uiState.value.point1?.let { p1 ->
                    drawLine(
                        color = Color.Yellow,
                        start = Offset(p1.x * scaleX, p1.y * scaleY),
                        end = Offset(p2.x * scaleX, p2.y * scaleY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        if (
            selectedSettings.contains("pixel") &&
            uiState.value.hoverVoxelValue != null &&
            uiState.value.hoverVoxelPosition != null &&
            uiState.value.isHovering
        ) {
            HoverPopup(
                cursorPosition = uiState.value.cursorPosition,
                hoverPosition = uiState.value.hoverVoxelPosition!!,
                string = formatVoxelValue(uiState.value.hoverVoxelValue!!, modality)
            )

        }

        if (selectedSettings.contains("measure") && uiState.value.distance != null) {
            Text(
                text = "Distance: ${"%.2f".format(uiState.value.distance)} mm",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                color = Color.White
            )
        }
    }
}

data class VoxelImageUIState(
    var layoutCoordinates: LayoutCoordinates? = null,
    var hoverVoxelValue: Float? = null,
    var hoverVoxelPosition: Point? = null,
    var cursorPosition: Offset = Offset.Zero,
    var isHovering: Boolean = false,
    var point1: Point? = null,
    var point2: Point? = null,
    var distance: Double? = null
)

fun voxelSliceToBitmap(
    slice: Array<Array<Float>>,
    windowCenter: Float,
    windowWidth: Float
): ImageBitmap {
    val height = slice.size
    val width = slice[0].size

    val windowMin = windowCenter - windowWidth / 2f
    val windowMax = windowCenter + windowWidth / 2f
    val windowRange = windowMax - windowMin

    // Flatten and find min/max
    val allValues = slice.flatten()
    val min = allValues.minOrNull() ?: 0f
    val max = allValues.maxOrNull() ?: 1f
    val range = (max - min).takeIf { it != 0f } ?: 1f

    // Create BufferedImage (grayscale)
    val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val raster = image.raster

    for (y in 0 until height) {
        for (x in 0 until width) {
            val value = slice[y][x]
            val pixel = when {
                value <= windowMin -> 0
                value >= windowMax -> 255
                else -> ((value - windowMin) / windowRange * 255f).toInt()
            }
            raster.setSample(x, y, 0, pixel)
        }
    }

    return image.toComposeImageBitmap()
}



fun mapToImageCoordinatesAspectAware(
    rawPointerPos: Offset,
    boxSize: IntSize,
    bitmap: ImageBitmap
): Offset {
    val imageAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
    val boxAspect = boxSize.width.toFloat() / boxSize.height.toFloat()

    val renderWidth: Int
    val renderHeight: Int
    val imageOffsetX: Float
    val imageOffsetY: Float

    if (boxAspect > imageAspect) {
        renderHeight = boxSize.height
        renderWidth = (renderHeight * imageAspect).toInt()
        imageOffsetX = ((boxSize.width - renderWidth) / 2f)
        imageOffsetY = 0f
    } else {
        renderWidth = boxSize.width
        renderHeight = (renderWidth / imageAspect).toInt()
        imageOffsetX = 0f
        imageOffsetY = ((boxSize.height - renderHeight) / 2f)
    }

    val relativeX = rawPointerPos.x - imageOffsetX
    val relativeY = rawPointerPos.y - imageOffsetY

    val mappedX = (relativeX / renderWidth) * bitmap.width
    val mappedY = (relativeY / renderHeight) * bitmap.height

    return Offset(mappedX, mappedY)
}

/**
 * Small popup card showing hover pixel values.
 */
@Composable
fun HoverPopup(cursorPosition: Offset, hoverPosition: Point, string: String) {
    Popup(
        offset = IntOffset(
            x = cursorPosition.x.toInt() + 10,
            y = cursorPosition.y.toInt() + 10
        ),
        properties = PopupProperties(
            focusable = false
        ), onPreviewKeyEvent = { false }, onKeyEvent = { false }) {
        Card(
            elevation = 8.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text("X: ${hoverPosition.x}, Y: ${hoverPosition.y}", fontSize = 10.sp, fontWeight = FontWeight.Normal)
                Text("Value: $string", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


fun formatVoxelValue(value: Float, modality: String): String {
    return when (modality.uppercase()) {
        "CT" -> "HU: ${value.toInt()}"
        "MRI" -> "Signal Intensity: ${"%.3f".format(value)}"
        "PET" -> "PET Intensity: ${"%.1f".format(value)}" // not SUV if unscaled
        else -> "Value: ${"%.3f".format(value)}"
    }
}
