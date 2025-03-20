package org.thesis.project.Screens

import CardMenu
import CardWithCheckboxes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import org.thesis.project.*
import org.thesis.project.Components.*
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

    //Takes the file path and parses the nifti
    //val niftiFile = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"


    //val niftiFile1 = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_PET\\liver_PET.nii.gz"

//    val file1 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"
//
//    val file2 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\PET.nii.gz"
//    //TODO change this to a dynamic path
//
//    val title = "Patient_1"
//    val inputFiles = listOf(file1) //Example input NIfTI files
//    val outputFiles = listOf(file2) //Example output NIfTI files

    interfaceModel.imageController.updateSelectedViews(NiftiView.AXIAL, true)

    Column(modifier = Modifier.fillMaxSize()) {


        topAppBar(title = "App Name" , modelName = "Model Name"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                IconButton(onClick = { /* show info */ }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }

                IconButton(
                    onClick = { interfaceModel.panelLayout.toggleRightPanelExpanded() }
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
                }
            }
        }

        MainPanelLayout(
            leftPanelWidth = leftPanelWidth,
            leftPanelExpanded = leftPanelExpanded,
            rightPanelWidth = rightPanelWidth,
            rightPanelExpanded = rightPanelExpanded,
            toggleLeftPanel = { interfaceModel.panelLayout.toggleLeftPanelExpanded() },
            toggleRightPanel = { interfaceModel.panelLayout.toggleRightPanelExpanded() },

            leftContent = {


                navMenu()

                CardMenu(
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

//                val coroutineScope = rememberCoroutineScope()
//
//                Button(onClick = {
//                    coroutineScope.launch {
//                        interfaceModel.parseNiftiData(title, inputFiles, outputFiles)
//                    }
//                }) {
//                    Text("Parse NIfTI")
//                }
                standardCard (
                    content ={
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
                            val imageIndices by interfaceModel.imageController.getImageIndices(filename).collectAsState()
                            val currentIndex = when (selectedView) {
                                NiftiView.AXIAL -> imageIndices.first
                                NiftiView.CORONAL -> imageIndices.second
                                NiftiView.SAGITTAL -> imageIndices.third
                            }

                            val (slices, spacingPx, modality) = interfaceModel.niftiRepo.getSlicesFromVolume(selectedView, filename)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(containerHeight/selectedData.size)
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
    var buttonWidth = 160.dp

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
    var expanded by remember { mutableStateOf(false) }


    standardCard (
        content ={
            Text("Windowing Presets")
            val icon = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown


            Box {
                Button(onClick = { expanded = true }) {
                    Text(selectedPresetLabel ?: "Select Preset")
                    Spacer(Modifier.width(8.dp))
                    Icon(icon, contentDescription = if (expanded) "Collapse" else "Expand")
                }

                if (expanded) { // 🔥 Ensure this check, Compose sometimes bugs otherwise
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        interfaceModel.imageController.windowPresets.forEach { (label, preset) ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedPresetLabel = label
                                    interfaceModel.imageController.setPreset(preset)
                                    expanded = false
                                }

                            ){
                                Text(label)
                            }
                        }
                    }
                }
            }

            scrollWithTitle(
                title = "Window Center: ${windowing.center.toInt()}",
                value = windowing.center,
                onValueChange = { interfaceModel.imageController.setWindowing(it, windowing.width) },
                valueRange = -1000f..1000f,
                modifier = Modifier.fillMaxWidth()
            )

            scrollWithTitle(
                title = "Window Width: ${windowing.width.toInt()}",
                value = windowing.width,
                onValueChange = { interfaceModel.imageController.setWindowing(windowing.center, it) },
                valueRange = 1f..2500f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )

}