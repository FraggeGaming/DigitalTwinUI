package org.thesis.project

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerHoverIcon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp



@Composable
fun MainPanelLayout(
    leftContent: @Composable () -> Unit,
    centerContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    minPanelWidth: Dp = 200.dp, // Minimum width in dp for left/right panels
    maxPanelWidth: Dp = 600.dp, // Maximum width in dp for left/right panels
    initialLeftPanelWidth: Dp = 400.dp, // Initial left panel width
    initialRightPanelWidth: Dp = 300.dp // Initial right panel width
) {
    // Track panel widths (in dp) and expansion states
    var leftPanelWidth by remember { mutableStateOf(initialLeftPanelWidth) }
    var rightPanelWidth by remember { mutableStateOf(initialRightPanelWidth) }
    var leftPanelExpanded by remember { mutableStateOf(true) }
    var rightPanelExpanded by remember { mutableStateOf(true) }

    Row(modifier = Modifier.fillMaxSize()) {
        val leftScrollState = rememberScrollState()
        val rightScrollState = rememberScrollState()

        // Left Panel
        if (leftPanelExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(leftPanelWidth.coerceIn(minPanelWidth, maxPanelWidth)) // Constrain width
                    .verticalScroll(leftScrollState)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                leftContent()

                // Collapse Button
                Box(
                    modifier = Modifier.align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { leftPanelExpanded = false }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Collapse Left")
                    }
                }
            }

            // Left Resizer
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                HorizontalResizer { delta ->
                    leftPanelWidth = (leftPanelWidth + delta).coerceIn(minPanelWidth, maxPanelWidth)
                }
            }
        } else {
            // Expand Button for Left Panel
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = { leftPanelExpanded = true }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Expand Left", tint = Color.White)
                }
            }
        }

        // Center Panel
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f) // Center panel takes the remaining space
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            centerContent()
        }

        // Right Panel
        if (rightPanelExpanded) {
            // Right Resizer
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                HorizontalResizer { delta ->
                    rightPanelWidth = (rightPanelWidth - delta).coerceIn(minPanelWidth, maxPanelWidth)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(rightPanelWidth.coerceIn(minPanelWidth, maxPanelWidth)) // Constrain width
                    .verticalScroll(rightScrollState)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                rightContent()

                // Collapse Button
                Box(
                    modifier = Modifier.align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { rightPanelExpanded = false }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Collapse Right")
                    }
                }
            }
        } else {
            // Expand Button for Right Panel
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = { rightPanelExpanded = true }) {
                    Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun HorizontalResizer(onResize: (Dp) -> Unit) {
    val density = LocalDensity.current // Obtain within a proper @Composable function
    Box(
        modifier = Modifier
            .width(5.dp)
            .fillMaxHeight()
            .background(Color.DarkGray, RoundedCornerShape(2.dp))
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val dragDp = with(density) { dragAmount.toDp() } // Convert dragAmount to Dp within the gesture block
                    onResize(dragDp) // Pass the detected drag delta as Dp
                }
            }
    )
}


@Composable
fun calculateIntrinsicWidth(content: @Composable () -> Unit): Dp {
    var widthPx by remember { mutableStateOf(0f) }

    // Measure content width
    Box(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            widthPx = coordinates.size.width.toFloat()
        }
    ) {
        content()
    }

    // Convert pixel width to Dp using current screen density
    val density = LocalDensity.current
    return with(density) { widthPx.toDp() }
}




