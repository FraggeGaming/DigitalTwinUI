package org.thesis.project

import androidx.compose.foundation.*
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
    minPanelWidth: Dp = 300.dp, // Minimum width in dp for left/right panels
    maxPanelWidth: Dp = 600.dp, // Maximum width in dp for left/right panels
    leftPanelWidth: Dp,                // Passed in state for left panel width
    rightPanelWidth: Dp,               // Passed in state for right panel width
    leftPanelExpanded: Boolean,        // Passed in state for left panel expansion
    rightPanelExpanded: Boolean,       // Passed in state for right panel expansion
    toggleLeftPanel: () -> Unit,  // Callback to update left panel expansion
    toggleRightPanel: () -> Unit  // Callback to update right panel expansion
) {

    var leftWidth by remember { mutableStateOf(leftPanelWidth) }
    var rightWidth by remember { mutableStateOf(rightPanelWidth) }

    Row(modifier = Modifier.fillMaxSize()) {
        val leftScrollState = rememberScrollState()
        val rightScrollState = rememberScrollState()

        // Left Panel
        if (leftPanelExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(leftWidth.coerceIn(minPanelWidth, maxPanelWidth)) // Constrain width
                    .verticalScroll(leftScrollState)
                    .background(Color.LightGray),
                contentAlignment = Alignment.TopCenter
            ) {
                leftContent()
            }

            // Left Resizer
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                HorizontalResizer { delta ->
                    leftWidth = ((leftWidth + delta).coerceIn(minPanelWidth, maxPanelWidth))
                }
            }
        }
        else{
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = {toggleLeftPanel()}) {
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
                contentAlignment = Alignment.TopCenter
            ) {
                HorizontalResizer { delta ->
                    rightWidth = (rightWidth - delta).coerceIn(minPanelWidth, maxPanelWidth)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(rightWidth.coerceIn(minPanelWidth, maxPanelWidth)) // Constrain width
                    .verticalScroll(rightScrollState)
                    .background(Color.LightGray),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(modifier = Modifier.fillMaxSize().align(Alignment.TopStart)) {
                    TextButton(
                        modifier = Modifier
                            .width(48.dp)
                          ,
                        onClick = {
                            toggleRightPanel()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Collapse Right")
                    }
                    rightContent()
                }

            }
        }
        else {

            TextButton(
                modifier = Modifier
                    .width(48.dp)
                    .background(Color.DarkGray),
                onClick = {
                    toggleRightPanel()
                }
            ) {

                Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)

            }

        }
    }
}


@Composable
fun HorizontalResizer(onResize: (Dp) -> Unit) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .width(5.dp)
            .fillMaxHeight()
            .background(Color.DarkGray, RoundedCornerShape(2.dp))
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    // Convert dragAmount (in pixels) to dp manually:
                    val dragDp = Dp(dragAmount / density.density)
                    onResize(dragDp)
                }
            }
    )
}




