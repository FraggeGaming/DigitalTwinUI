package org.thesis.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.apache.commons.lang3.ClassUtils.Interfaces
import org.thesis.project.Components.LocalAppColors
import org.thesis.project.Components.TooltipArrowDirection
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Screens.ComponentInfoBox
import java.awt.Cursor


@Composable
fun MainPanelLayout(
    leftContent: @Composable () -> Unit,
    centerContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    minPanelWidth: Dp = 200.dp, // Minimum width in dp for left/right panels
    maxPanelWidth: Dp = 600.dp, // Maximum width in dp for left/right panels
    leftPanelWidth: Dp,                // Passed in state for left panel width
    rightPanelWidth: Dp,               // Passed in state for right panel width
    leftPanelExpanded: Boolean,        // Passed in state for left panel expansion
    rightPanelExpanded: Boolean,       // Passed in state for right panel expansion
    toggleLeftPanel: () -> Unit,  // Callback to update left panel expansion
    toggleRightPanel: () -> Unit,  // Callback to update right panel expansion
    interfaceModel: InterfaceModel
) {

    var leftWidth by remember { mutableStateOf(leftPanelWidth) }
    var rightWidth by remember { mutableStateOf(rightPanelWidth) }
    val infoMode = interfaceModel.infoMode.collectAsState()

    val bkg = LocalAppColors.current.secondaryBackgroundColor

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
                    .background(LocalAppColors.current.backgroundColor)
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

        } else {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .background(LocalAppColors.current.backgroundColor),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = { toggleLeftPanel() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Expand Left", tint = Color.White)
                }
            }
        }

        // Center Panel
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f) // Center panel takes the remaining space
                .background(bkg),
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
                    .background(LocalAppColors.current.backgroundColor),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalAppColors.current.backgroundColor))
                {
                    ComponentInfoBox(
                        id = "settings_tab",
                        infoMode,
                        infoText =
                            "This is the settings tab, where you can modify the contrast, and scroll through the image slices",
                        content = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0050A0))
                                    .clickable {
                                        toggleRightPanel()

                                    }
                                    .drawBehind {
                                        // Draw top border
                                        drawLine(
                                            color = bkg,
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
                        },
                        enabled = rightPanelExpanded,
                        arrowDirection = TooltipArrowDirection.Right
                    )
                }
                Row(modifier = Modifier
                    .fillMaxSize()
                    .background(LocalAppColors.current.backgroundColor))

                {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(rightWidth.coerceIn(minPanelWidth, maxPanelWidth))
                            .verticalScroll(rightScrollState)
                            .background(LocalAppColors.current.backgroundColor),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            rightContent()
                        }
                    }
                }
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




