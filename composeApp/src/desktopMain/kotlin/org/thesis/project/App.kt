package org.thesis.project

import CardMenu
import CardWithCheckboxes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import buttonWithCheckboxSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import menuCard
import navigationButtons
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.thesis.project.Components.voxelImageDisplay
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.NiftiView
import androidx.compose.ui.platform.LocalDensity
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


@Composable
@Preview
fun App() {
    MaterialTheme {
        val interfaceModel: InterfaceModel = viewModel()

        val navController = rememberNavController()
        NavHost(navController, startDestination = "main") {

            composable("upload") {
                uploadData(interfaceModel = interfaceModel, navMenu = { navigationButtons(navController, "upload") })
            }

            composable("modelSelect") {
                modelSelect(
                    interfaceModel = interfaceModel,
                    navMenu = { navigationButtons(navController, "modelSelect") })
            }

            composable("main") {


                imageViewer(interfaceModel = interfaceModel, navMenu = { navigationButtons(navController, "main") })
            }
        }
    }
}

@Composable
fun FileUploadComponent(
    filePathFlow: MutableStateFlow<String?> // StateFlow to store the file path
) {
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .border(2.dp, if (isDragging) Color.Blue else Color.Gray, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragCancel = { isDragging = false },
                    onDragEnd = { isDragging = false },
                    onDrag = { _, _ -> }
                )
            }
            .clickable { selectFile(filePathFlow) }, // Handle click to open file picker
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Upload,
                contentDescription = "Upload",
                tint = Color.DarkGray,
                modifier = Modifier.size(48.dp)
            )
            Text("Drag & Drop or Click to Upload", textAlign = TextAlign.Center)
        }
    }

    // Display selected file path
    val filePath by filePathFlow.collectAsState()
    filePath?.let {
        Text("Selected File: $it", modifier = Modifier.padding(top = 8.dp), color = Color.Black)
    }
}

// Function to open file picker
fun selectFile(filePathFlow: MutableStateFlow<String?>) {
    val fileChooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.FILES_ONLY
        fileFilter = FileNameExtensionFilter("NIfTI Files", "nii", "nii.gz")
    }
    val result = fileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        filePathFlow.value = fileChooser.selectedFile.absolutePath
    }
}

@Composable
fun uploadData(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit
) {
    val selectedFilePath = remember { MutableStateFlow<String?>(null) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        navMenu()
        Text(text = "Upload Data")
        FileUploadComponent(selectedFilePath)
    }
}

@Composable
fun modelSelect(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        navMenu()
        Text(text = "Select model")
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun imageViewer(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit
) {
    val selectedData by interfaceModel.selectedData.collectAsState()
    val selectedDistricts by interfaceModel.selectedDistricts.collectAsState()
    val organs by interfaceModel.organs.collectAsState()

    val leftPanelWidth by interfaceModel.leftPanelWidth.collectAsState()
    val rightPanelWidth by interfaceModel.rightPanelWidth.collectAsState()
    val leftPanelExpanded by interfaceModel.leftPanelExpanded.collectAsState()
    val rightPanelExpanded by interfaceModel.rightPanelExpanded.collectAsState()


    val selectedViews by interfaceModel.selectedViews.collectAsState()
    val selectedSettings by interfaceModel.selectedSettings.collectAsState()

    val fileMapping by interfaceModel.fileMapping.collectAsState()
    //Takes the file path and parses the nifti
    //val niftiFile = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"


    //val niftiFile1 = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_PET\\liver_PET.nii.gz"

    val file1 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"

    val file2 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\PET.nii.gz"
    //TODO change this to a dynamic path

    val title = "Patient_1"
    val inputFiles = listOf(file1) // Example input NIfTI files
    val outputFiles = listOf(file2) // Example output NIfTI files

    interfaceModel.updateSelectedViews(NiftiView.AXIAL, true)

    MainPanelLayout(
        leftPanelWidth = leftPanelWidth,
        leftPanelExpanded = leftPanelExpanded,
        rightPanelWidth = rightPanelWidth,
        rightPanelExpanded = rightPanelExpanded,
        toggleLeftPanel = { interfaceModel.toggleLeftPanelExpanded() },
        toggleRightPanel = { interfaceModel.toggleRightPanelExpanded() },


        leftContent = {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter

            ) {
                Column(
                    modifier = Modifier
                        .width(400.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    navMenu()

                    CardMenu(
                        fileKeys = fileMapping.keys.toList(),
                        selectedData = selectedData,
                        getFileMapping = interfaceModel::getFileMapping,
                        onCheckboxChanged = { label, isChecked ->
                            interfaceModel.updateSelectedData(label, isChecked)
                        }
                    )

                    CardWithCheckboxes(
                        selectedDistricts,
                        items = organs,
                        onCheckboxChanged = { organ, isChecked ->
                            interfaceModel.updateSelectedDistrict(organ, isChecked)
                        }
                    )
                }
            }

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
                                            interfaceModel.decrementScrollPosition()
                                        } else if (scrollDelta.y < 0f) {
                                            interfaceModel.incrementScrollPosition()
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        imageGrid(selectedData, selectedViews, interfaceModel)
                    }


                }

                Row(
                    modifier = Modifier
                        .width(800.dp)
                        .height(100.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                    )
                    {
                        menuCard(
                            content = listOf(
                                {
                                    Row {
                                        buttonWithCheckboxSet(
                                            selectedViews,
                                            NiftiView.AXIAL,
                                            interfaceModel::updateSelectedViews
                                        )
                                    }
                                },
                                {
                                    Row {
                                        buttonWithCheckboxSet(
                                            selectedViews,
                                            NiftiView.CORONAL,
                                            interfaceModel::updateSelectedViews
                                        )
                                    }
                                },

                                {
                                    Row {
                                        buttonWithCheckboxSet(
                                            selectedViews,
                                            NiftiView.SAGITTAL,
                                            interfaceModel::updateSelectedViews
                                        )
                                    }
                                },

                                {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(50.dp)
                                            .background(Color.Gray)
                                    )
                                },

                                {

                                    //TODO change to enum
                                    TextButton(
                                        onClick = {
                                            val isCurrentlyChecked = selectedSettings.contains("measure")
                                            interfaceModel.updateSelectedSettings("measure", !isCurrentlyChecked)
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            backgroundColor = if (selectedSettings.contains("measure")) Color.Gray else Color.Transparent, // Change background
                                            contentColor = Color.Unspecified
                                        ),
                                        elevation = null, // Removes elevation
                                        modifier = Modifier.padding(0.dp) // Removes extra padding
                                    ) {
                                        Icon(Icons.Default.Straighten, contentDescription = "Measure")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Measure")
                                    }
                                },

                                {
                                    TextButton(
                                        onClick = {
                                            val isCurrentlyChecked = selectedSettings.contains("pixel")
                                            interfaceModel.updateSelectedSettings("pixel", !isCurrentlyChecked)
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            backgroundColor = if (selectedSettings.contains("pixel")) Color.Gray else Color.Transparent, // Change background
                                            contentColor = Color.Unspecified
                                        ),
                                        elevation = null, // Removes elevation
                                        modifier = Modifier.padding(0.dp) // Removes extra padding
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Pixel intensity")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Pixel intensity")
                                    }
                                }
                            )
                        )
                    }
                }
            }
        },
        rightContent = {
            Box(
                modifier = Modifier.fillMaxSize(),

            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val coroutineScope = rememberCoroutineScope()

                    Button(onClick = {
                        coroutineScope.launch {
                            interfaceModel.parseNiftiData(title, inputFiles, outputFiles)
                        }
                    }) {
                        Text("Parse NIfTI")
                    }

                    ScrollSlider(
                        selectedData = interfaceModel.selectedData,
                        scrollStep = interfaceModel.scrollStep,
                        maxIndexMap = interfaceModel.maxSelectedImageIndex,
                        onUpdate = { value -> interfaceModel.setScrollPosition(value) } // Convert Int -> Float
                    )

                    windowControls(interfaceModel)
                }
            }
        }
    )
}

@Composable
fun imageGrid(
    selectedData: Set<String>,
    selectedViews: Set<NiftiView>,
    interfaceModel: InterfaceModel,
) {
    val spacing = 12.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val rowCount = selectedData.size
        val rowHeight = containerHeight / rowCount

        Column(
            //verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedData.forEach { filename ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imageCount = selectedViews.size.coerceAtLeast(1)
                        val imageWidth = if (imageCount == 1) containerWidth else (containerWidth - spacing * (imageCount - 1)) / imageCount

                        selectedViews.forEach { selectedView ->
                            val imageIndices by interfaceModel.getImageIndices(filename).collectAsState()
                            val currentIndex = when (selectedView) {
                                NiftiView.AXIAL -> imageIndices.first
                                NiftiView.CORONAL -> imageIndices.second
                                NiftiView.SAGITTAL -> imageIndices.third
                            }

                            val (slices, spacingPx) = interfaceModel.getSlicesFromVolume(selectedView, filename)
                            val modality = interfaceModel.extractModality(filename)

                            Box(
                                modifier = Modifier
                                    .width(imageWidth)
                                    .height(rowHeight),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.wrapContentSize()
                                ) {
                                    Text(
                                        text = "${selectedView.displayName} - Slice $currentIndex",
                                        style = MaterialTheme.typography.h6
                                    )

                                    Card(
                                        modifier = Modifier.size(imageWidth)
                                            .padding(8.dp) .background(Color.LightGray),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation =  8.dp
                                    ) {

                                            if (modality != null) {
                                                voxelImageDisplay(
                                                    voxelSlice = slices[currentIndex],
                                                    interfaceModel = interfaceModel,
                                                    modality = modality,
                                                    pixelSpacing = spacingPx
                                                )
                                            }

                                    }
                                }
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
fun windowControls(interfaceModel: InterfaceModel) {
    val windowing by interfaceModel.windowing.collectAsState()
    var selectedPresetLabel by remember { mutableStateOf("CT - Brain") }

    Column{
        Text("Windowing Presets", style = MaterialTheme.typography.h5)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            interfaceModel.windowPresets.forEach { (label, preset) ->
                Button(
                    onClick = {
                        selectedPresetLabel = label
                        interfaceModel.setPreset(preset)
                    },
                    colors = if (label == selectedPresetLabel)
                        ButtonDefaults.buttonColors()
                    else
                        ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(label)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Window Center: ${windowing.center.toInt()}")
        Slider(
            value = windowing.center,
            onValueChange = { interfaceModel.setWindowing(it, windowing.width) },
            valueRange = -1000f..1000f,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Window Width: ${windowing.width.toInt()}")
        Slider(
            value = windowing.width,
            onValueChange = { interfaceModel.setWindowing(windowing.center, it) },
            valueRange = 1f..2500f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun ScrollSlider(
    selectedData: StateFlow<Set<String>>,
    scrollStep: StateFlow<Float>,
    maxIndexMap: StateFlow<Map<String, Float>>,
    onUpdate: (Float) -> Unit
) {
    val currentScrollStep by scrollStep.collectAsState()
    val selectedFilenames by selectedData.collectAsState()
    val maxSizeMap by maxIndexMap.collectAsState()

    val maxValue = selectedFilenames.mapNotNull { maxSizeMap[it] }.maxOrNull() ?: 1f

    var sliderPosition by remember { mutableStateOf(currentScrollStep) }

    Column {
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                sliderPosition = newValue
                onUpdate(newValue)
            },
            valueRange = 0f..maxValue,
            steps = 0
        )
        Text(text = "Step: ${currentScrollStep.toInt()} / ${maxValue.toInt()}")
    }

    LaunchedEffect(currentScrollStep) {
        if (currentScrollStep != sliderPosition) {
            sliderPosition = currentScrollStep
        }
    }
}







