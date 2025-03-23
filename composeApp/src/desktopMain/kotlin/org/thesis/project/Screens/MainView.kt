package org.thesis.project.Screens

import CardWithCheckboxes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import buttonWithCheckboxSet
import cardMenu
import org.thesis.project.Components.scrollSlider
import org.thesis.project.Components.scrollWithTitle
import org.thesis.project.Components.standardCard
import org.thesis.project.Components.voxelImageDisplay
import org.thesis.project.MainPanelLayout
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.NiftiView
import org.thesis.project.Model.Settings

@Composable
fun imageViewer(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit,
    navController: NavHostController
) {
    val selectedData by interfaceModel.imageController.selectedData.collectAsState()
    val selectedDistricts by interfaceModel.selectedDistricts.collectAsState()
    val organs by interfaceModel.organs.collectAsState()

    val leftPanelWidth by interfaceModel.panelLayout.leftPanelWidth.collectAsState()
    val rightPanelWidth by interfaceModel.panelLayout.rightPanelWidth.collectAsState()
    val leftPanelExpanded by interfaceModel.panelLayout.leftPanelExpanded.collectAsState()
    val rightPanelExpanded by interfaceModel.panelLayout.rightPanelExpanded.collectAsState()


    val selectedViews by interfaceModel.imageController.selectedViews.collectAsState()
    val selectedSettings by interfaceModel.selectedSettings.collectAsState()

    val fileMapping by interfaceModel.niftiRepo.fileMapping.collectAsState()

    interfaceModel.imageController.updateSelectedViews(NiftiView.AXIAL, true)

    MainPanelLayout(
        leftPanelWidth = leftPanelWidth,
        leftPanelExpanded = leftPanelExpanded,
        rightPanelWidth = rightPanelWidth,
        rightPanelExpanded = rightPanelExpanded,
        toggleLeftPanel = { interfaceModel.panelLayout.toggleLeftPanelExpanded() },
        toggleRightPanel = { interfaceModel.panelLayout.toggleRightPanelExpanded() },

        leftContent = {


            navMenu()

            cardMenu(
                fileKeys = fileMapping.keys.toList(),
                selectedData = selectedData,
                getFileMapping = interfaceModel.niftiRepo::getFileMapping,
                onCheckboxChanged = { label, isChecked ->
                    interfaceModel.imageController.updateSelectedData(label, isChecked)
                }
            )

            CardWithCheckboxes(
                selectedDistricts,
                items = organs,
                onCheckboxChanged = { organ, isChecked ->
                    interfaceModel.updateSelectedDistrict(organ, isChecked)
                }
            )

        },
        centerContent = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.type == PointerEventType.Scroll) {
                                        val scrollDelta = event.changes.firstOrNull()?.scrollDelta ?: Offset.Zero
                                        if (scrollDelta.y > 0f) {
                                            interfaceModel.imageController.decrementScrollPosition()
                                        } else if (scrollDelta.y < 0f) {
                                            interfaceModel.imageController.incrementScrollPosition()
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    imageGrid(selectedData, selectedViews, interfaceModel)
                }

                menuCard(
                    selectedViews = selectedViews,
                    interfaceModel = interfaceModel,
                    selectedSettings = selectedSettings,
                )
            }
        },
        rightContent = {
            standardCard(
                content = {
                    scrollSlider(
                        selectedData = interfaceModel.imageController.selectedData,
                        scrollStep = interfaceModel.imageController.scrollStep,
                        maxIndexMap = interfaceModel.imageController.maxSelectedImageIndex,
                        onUpdate = { value -> interfaceModel.imageController.setScrollPosition(value) } // Convert Int -> Float
                    )
                }
            )
            windowControls(interfaceModel)
        }
    )
}

@Composable
fun imageGrid(
    selectedData: Set<String>,
    selectedViews: Set<NiftiView>,
    interfaceModel: InterfaceModel,
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val containerHeight = maxHeight

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedData.forEach { filename ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        selectedViews.forEach { selectedView ->
                            val imageIndices by interfaceModel.imageController.getImageIndices(filename)
                                .collectAsState()
                            val currentIndex = when (selectedView) {
                                NiftiView.AXIAL -> imageIndices.first
                                NiftiView.CORONAL -> imageIndices.second
                                NiftiView.SAGITTAL -> imageIndices.third
                            }

                            val (slices, spacingPx, modality) = interfaceModel.niftiRepo.getSlicesFromVolume(
                                selectedView,
                                filename
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(containerHeight / selectedData.size)
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {

                                standardCard(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .aspectRatio(1f),

                                    contentAlignment = Alignment.CenterHorizontally,
                                    content = {
                                        Text(
                                            text = "${selectedView.displayName} - Slice $currentIndex",
                                            style = MaterialTheme.typography.h6
                                        )
                                        voxelImageDisplay(
                                            voxelSlice = slices[currentIndex],
                                            interfaceModel = interfaceModel,
                                            modality = modality,
                                            pixelSpacing = spacingPx
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }


    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun menuCard(
    modifier: Modifier = Modifier,
    selectedViews: Set<NiftiView>,
    interfaceModel: InterfaceModel,
    selectedSettings: Set<Settings>,
    //content: List<@Composable () -> Unit>
) {
    val buttonHeight = 60.dp
    val buttonWidth = 160.dp

    Card(
        modifier = modifier.padding(16.dp),
        elevation = 8.dp,
        contentColor = Color.White
    ) {
        FlowRow(
            modifier = Modifier
                .wrapContentSize()
                .background(Color(0xFF0050A0), shape = RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            listOf(
                NiftiView.AXIAL,
                NiftiView.CORONAL,
                NiftiView.SAGITTAL
            ).forEach { view ->
                buttonWithCheckboxSet(
                    selectedData = selectedViews,
                    label = view,
                    onCheckboxChanged = interfaceModel.imageController::updateSelectedViews,
                    modifier = Modifier
                        .width(buttonWidth)
                        .height(buttonHeight)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(buttonHeight)
                    .background(Color.Gray)
            )

            TextButton(
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                onClick = {
                    val isChecked = selectedSettings.contains(Settings.MEASUREMENT)
                    interfaceModel.updateSelectedSettings(Settings.MEASUREMENT, !isChecked)
                },
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = if (selectedSettings.contains(Settings.MEASUREMENT)) Color(0xFF80A7D0) else Color.Transparent,
                    contentColor = if (selectedSettings.contains(Settings.MEASUREMENT)) Color.Black else Color.White
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Straighten, contentDescription = Settings.MEASUREMENT.settingName)
                    Spacer(Modifier.width(4.dp))
                    Text(Settings.MEASUREMENT.settingName)
                }
            }

            TextButton(
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                onClick = {
                    val isChecked = selectedSettings.contains(Settings.PIXEL)
                    interfaceModel.updateSelectedSettings(Settings.PIXEL, !isChecked)
                },
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = if (selectedSettings.contains(Settings.PIXEL)) Color(0xFF80A7D0) else Color.Transparent,
                    contentColor = if (selectedSettings.contains(Settings.PIXEL)) Color.Black else Color.White
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = Settings.PIXEL.settingName)
                    Spacer(Modifier.width(4.dp))
                    Text(Settings.PIXEL.settingName)
                }
            }
        }
    }
}

@Composable
fun windowControls(interfaceModel: InterfaceModel) {
    val windowing by interfaceModel.imageController.windowing.collectAsState()
    var selectedPresetLabel by remember { mutableStateOf<String?>(null) }

    standardCard(
        content = {
            Text("Windowing Presets")
            //val icon = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown


            dropDownMenuCustom(
                label = "Select Preset",
                selected = selectedPresetLabel ?: "",
                options = interfaceModel.imageController.windowPresets.keys.toList(),
                onSelected = { label ->
                    selectedPresetLabel = label
                    interfaceModel.imageController.setPreset(interfaceModel.imageController.windowPresets[label]!!)
                }
            )

            scrollWithTitle(
                title = "Window Center",
                value = windowing.center,
                onValueChange = { interfaceModel.imageController.setWindowing(it, windowing.width) },
                valueRange = -1000f..1000f,
                modifier = Modifier.fillMaxWidth()
            )

            scrollWithTitle(
                title = "Window Width",
                value = windowing.width,
                onValueChange = { interfaceModel.imageController.setWindowing(windowing.center, it) },
                valueRange = 1f..2500f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )

}