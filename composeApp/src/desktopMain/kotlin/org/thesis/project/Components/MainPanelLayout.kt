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
    minPanelWidth: Float = 0.1f, // Minimum 10% width
    maxPanelWidth: Float = 0.4f // Maximum 40% width
) {
    var leftPanelWidth by remember { mutableStateOf(0.3f) } // 20% width initially
    var rightPanelWidth by remember { mutableStateOf(0.3f) } // 20% width initially

    var leftPanelExpanded by remember { mutableStateOf(true) }
    var rightPanelExpanded by remember { mutableStateOf(true) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Panel
        val leftScrollState = rememberScrollState()
        val rightScrollState = rememberScrollState()

        if (leftPanelExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(leftPanelWidth)
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

            //Left Resizer
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
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Expand Right", tint = Color.White)
                }
            }
        }

        // Center Panel
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f - (if (leftPanelExpanded) leftPanelWidth else 0f) - (if (rightPanelExpanded) rightPanelWidth else 0f))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            centerContent()
        }

        if (rightPanelExpanded) {
            // Right Resizer (Overlayed)
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                HorizontalResizer { delta ->
                    rightPanelWidth = (rightPanelWidth - delta).coerceIn(minPanelWidth, maxPanelWidth)
                }
            }

            // Right Panel
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(rightPanelWidth)
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
fun HorizontalResizer(onResize: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .width(5.dp)
            .fillMaxHeight()
            .background(Color.DarkGray, RoundedCornerShape(2.dp))
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    onResize(dragAmount / 1000f)
                }
            }
    )
}

@Composable
fun calculateIntrinsicWidth(content: @Composable () -> Unit): Dp {
    var intrinsicWidth by remember { mutableStateOf(0.dp) } // Store result in Dp
    val density = LocalDensity.current // Access system density for pixel-to-Dp conversion

    BoxWithConstraints {
        Box(
            modifier = Modifier
                .onGloballyPositioned { layoutCoordinates ->
                    // Convert pixel width to Dp value
                    intrinsicWidth = with(density) { layoutCoordinates.size.width.toDp() }
                }
        ) {
            content()
        }
    }

    return intrinsicWidth
}



