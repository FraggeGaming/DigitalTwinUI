package org.thesis.project

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.awt.Cursor
import androidx.compose.ui.input.pointer.PointerIcon
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
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(leftWidth.coerceIn(minPanelWidth, maxPanelWidth))
                    .verticalScroll(leftScrollState)
                    .background(Color.LightGray)
                    .padding(12.dp), // move padding here
                verticalArrangement = Arrangement.spacedBy(16.dp), // combine spacing
                horizontalAlignment = Alignment.Start
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
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f) // Center panel takes the remaining space
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
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

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(rightWidth.coerceIn(minPanelWidth, maxPanelWidth))
                    .verticalScroll(rightScrollState)
                    .background(Color.LightGray),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0050A0))
                        .clickable { toggleRightPanel() }
                        .drawBehind {
                            // Draw top border
                            drawLine(
                                color = Color.LightGray,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Collapse Right",
                        tint = Color.White
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Settings and Controls",
                            color = Color.White
                        )
                    }
                }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp), // ✅ Restore spacing between items
                        horizontalAlignment = Alignment.Start
                    ) {
                        rightContent()
                    }

            }


        }

//        else {
//
//            TextButton(
//                modifier = Modifier
//                    .width(48.dp)
//                    .background(Color(0xFF0050A0)),
//                onClick = {
//                    toggleRightPanel()
//                }
//            ) {
//
//                Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
//
//            }
//
//        }
    }
}


@Composable
fun HorizontalResizer(onResize: (Dp) -> Unit) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .width(5.dp)
            .fillMaxHeight()
            .background(Color.DarkGray)
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




