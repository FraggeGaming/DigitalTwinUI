package org.thesis.project

import CardMenu
import CardWithCheckboxes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import buttonWithCheckboxSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import menuCard
import navigationButtons
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.NiftiView
import java.awt.Point
import java.awt.image.BufferedImage
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

    //Takes the file path and parses the nifti
    //val niftiFile = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"


    //val niftiFile1 = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_PET\\liver_PET.nii.gz"

    val file1 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"

    val file2 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\PET.nii.gz"
    //TODO change this to a dynamic path

    val title = "Patient_1"
    val inputFiles = listOf(file1) // Example input NIfTI files
    val outputFiles = listOf(file2) // Example output NIfTI files

    interfaceModel.parseNiftiData(title, inputFiles, outputFiles)
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
                        fileKeys = interfaceModel.getFileMappingKeys(),
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
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        selectedData.forEach { filename ->
                            Text(filename)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                selectedViews.forEach { selectedView ->
                                    val imageIndices by interfaceModel.getImageIndices(filename).collectAsState()
                                    val currentIndex = when (selectedView) {
                                        NiftiView.AXIAL -> imageIndices.first
                                        NiftiView.CORONAL -> imageIndices.second
                                        NiftiView.SAGITTAL -> imageIndices.third
                                    }

                                    val slices = interfaceModel.getSlicesFromVolume(selectedView, filename)

                                    val currentVoxelSlice = voxelSliceToBitmap(slices[currentIndex])
                                    val mod = interfaceModel.extractModality(filename)

                                    if (mod != null) {
                                        VoxelImageDisplay(
                                            currentVoxelSlice,
                                            slices[currentIndex],
                                            scaleFactor = 1f,
                                            interfaceModel,
                                            mod,
                                        )
                                    }
                                }
                            }
                        }
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
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter

            ) {
                Column {
                    // Collapse Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { interfaceModel.toggleRightPanelExpanded() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Collapse Right")
                        }
                    }

                    Text("Right Panel")

                    ScrollSlider(
                        selectedData = interfaceModel.selectedData,
                        scrollStep = interfaceModel.scrollStep,
                        maxIndexMap = interfaceModel.maxSelectedImageIndex,
                        onUpdate = { value -> interfaceModel.setScrollPosition(value) } // Convert Int -> Float
                    )
                }
            }
        }
    )
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
            steps = 0,
            colors = SliderDefaults.colors(
                thumbColor = Color.Gray,
                activeTrackColor = Color.Blue,
                inactiveTrackColor = Color.Green,
            )
        )
        Text(text = "Step: ${currentScrollStep.toInt()} / ${maxValue.toInt()}")
    }

    LaunchedEffect(currentScrollStep) {
        if (currentScrollStep != sliderPosition) {
            sliderPosition = currentScrollStep
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VoxelImageDisplay(
    bitmap: ImageBitmap,
    voxelSlice: List<List<Float>>,
    scaleFactor: Float = 1f,
    interfaceModel: InterfaceModel,
    modality: String,

    modifier: Modifier = Modifier
) {
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var hoverVoxelValue by remember { mutableStateOf<Float?>(null) }
    var hoverVoxelPosition by remember { mutableStateOf<Point?>(null) }
    var cursorPosition by remember { mutableStateOf(Offset.Zero) }
    var isHovering by remember { mutableStateOf(false) }

    var point1 by remember { mutableStateOf<Point?>(null) }
    var point2 by remember { mutableStateOf<Point?>(null) }
    val selectedSettings by interfaceModel.selectedSettings.collectAsState()

    var distance by remember { mutableStateOf<Double?>(null) }

    Box(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates = it }
            .background(Color.Red.copy(alpha = 0.2f)) // TEMP!
            // CLICK HANDLER (separate gesture)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { localPos ->
                        layoutCoordinates?.let { layout ->
                            if (selectedSettings.contains("measure")) {
                                print("TEST11111")
                                val imageWidth = bitmap.width
                                val imageHeight = bitmap.height

                                val voxelData = interfaceModel.getVoxelInfo(
                                    position = localPos,
                                    scaleFactor = scaleFactor,
                                    imageWidth = imageWidth,
                                    imageHeight = imageHeight,
                                    voxelSlice = voxelSlice
                                )

                                voxelData?.let {
                                    val newPoint = Point(it.x, it.y)

                                    if (point1 == null) {
                                        point1 = newPoint
                                    } else if (point2 == null) {
                                        point2 = newPoint
                                    } else {

                                        point1 = newPoint
                                        point2 = null
                                    }

                                    distance = interfaceModel.calculateVoxelDistance(point1, point2, 1f, 1f)

                                }

                            } else {
                                println(selectedSettings.toList().toString())
                            }
                        }


                    },
                    onLongPress = {
                        point1 = null
                        point2 = null
                        distance = null
                    }
                )
            }


            // HOVER TRACKING: MOVE EVENTS
            .onPointerEvent(PointerEventType.Move) { event ->
                if (isHovering) {
                    val localPos = event.changes.first().position
                    cursorPosition = localPos

                    layoutCoordinates?.let {
                        val imageWidth = bitmap.width
                        val imageHeight = bitmap.height

                        val voxelData = interfaceModel.getVoxelInfo(
                            position = localPos,
                            scaleFactor = scaleFactor,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            voxelSlice = voxelSlice
                        )

                        if (voxelData != null) {
                            hoverVoxelValue = voxelData.voxelValue
                            hoverVoxelPosition = Point(voxelData.x, voxelData.y)
                        } else {
                            hoverVoxelValue = null
                            hoverVoxelPosition = null
                        }
                    }
                }
            }

            .onPointerEvent(PointerEventType.Enter) {
                isHovering = true
            }

            .onPointerEvent(PointerEventType.Exit) {
                isHovering = false
                hoverVoxelValue = null
                hoverVoxelPosition = null
            }

            .width((bitmap.width * scaleFactor).dp)
            .height((bitmap.height * scaleFactor).dp)
    ) {
        Column {
            Box() {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Voxel image",
                    modifier = Modifier
                        .width((bitmap.width * scaleFactor).dp)
                        .height((bitmap.height * scaleFactor).dp)
                )

                // Debug info
//                println("selectedSettings.contains(pixel): ${selectedSettings.contains("pixel")}")
//                println("hoveringValue: $hoverVoxelValue")
//                println("hoveringVoxelPOS: $hoverVoxelPosition")
//                println("cursorPosition: $cursorPosition")
//                println("isHovering: $isHovering")

                if (selectedSettings.contains("pixel") && hoverVoxelValue != null && hoverVoxelPosition != null && isHovering) {
                    println("Hover voxel value: $hoverVoxelValue")
                    println("Hover voxel position: $hoverVoxelPosition")
                    println("modality: $modality")

                    HoverPopup(
                        cursorPosition = cursorPosition,
                        hoverPosition = hoverVoxelPosition!!,
                        string = formatVoxelValue(hoverVoxelValue!!, modality)
                    )
                }

                if (selectedSettings.contains("measure") && distance != null) {
                    Text(
                        text = "Distance: ${"%.2f".format(distance)} mm",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.5f)) // test visibility
                            .padding(8.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}


fun voxelSliceToBitmap(slice: List<List<Float>>): ImageBitmap {
    val height = slice.size
    val width = slice[0].size

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
            val normalized = ((value - min) / range * 255f).toInt().coerceIn(0, 255)
            raster.setSample(x, y, 0, normalized)
        }
    }

    return image.toComposeImageBitmap()
}


/**
 * Small popup card showing hover pixel values.
 */
@Composable
fun HoverPopup(cursorPosition: Offset, hoverPosition: Point, string: String) {
    Card(
        modifier = Modifier
            .offset(
                x = cursorPosition.x.dp + 10.dp,
                y = cursorPosition.y.dp + 10.dp
            ),
        elevation = 8.dp, // Adds elevation to make it float
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


fun formatVoxelValue(value: Float, modality: String): String {
    return when (modality.uppercase()) {
        "CT" -> "HU: ${value.toInt()}"
        "MRI" -> "Signal Intensity: ${"%.3f".format(value)}"
        "PET" -> "PET Intensity: ${"%.1f".format(value)}" // not SUV if unscaled
        else -> "Value: ${"%.3f".format(value)}"
    }
}





