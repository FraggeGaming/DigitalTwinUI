package org.thesis.project

import CardMenu
import CardWithCheckboxes
import navigationButtons
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import bottomMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.NiftiView
import java.awt.image.BufferedImage
import parseNifti
import java.awt.Point
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


@Composable
@Preview
fun App() {
    MaterialTheme {
        val interfaceModel: InterfaceModel = viewModel()

        val navController = rememberNavController()
        NavHost(navController, startDestination = "main") {

            composable("upload"){
                uploadData(interfaceModel = interfaceModel, navMenu = { navigationButtons(navController, "upload") })
            }

            composable("modelSelect"){
                modelSelect(interfaceModel = interfaceModel, navMenu = { navigationButtons(navController, "modelSelect") })
            }

            composable("main"){


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
                    onDrag = {_, _ ->}
                )
            }
            .clickable { selectFile(filePathFlow) }, // Handle click to open file picker
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Upload, contentDescription = "Upload", tint = Color.DarkGray, modifier = Modifier.size(48.dp))
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
fun uploadData(interfaceModel: InterfaceModel,
               navMenu: @Composable () -> Unit){
    val selectedFilePath = remember { MutableStateFlow<String?>(null) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        navMenu()
        Text(text = "Upload Data")
        FileUploadComponent(selectedFilePath)
    }
}

@Composable
fun modelSelect(interfaceModel: InterfaceModel,
                navMenu: @Composable () -> Unit){
    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        navMenu()
        Text(text = "Select model")
    }
}


@Composable
fun imageViewer(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit
){
    val selectedData by interfaceModel.selectedData.collectAsState()
    val selectedDistricts by interfaceModel.selectedDistricts.collectAsState()
    val organs by interfaceModel.organs.collectAsState()

    val leftPanelWidth by interfaceModel.leftPanelWidth.collectAsState()
    val rightPanelWidth by interfaceModel.rightPanelWidth.collectAsState()
    val leftPanelExpanded by interfaceModel.leftPanelExpanded.collectAsState()
    val rightPanelExpanded by interfaceModel.rightPanelExpanded.collectAsState()


    val selectedViews by interfaceModel.selectedViews.collectAsState()


    //Takes the file path and parses the nifti
    val niftiFile = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_CT\\liver_CT.nii.gz"
    //val file = parseNifti(niftiFile, interfaceModel)

    //val niftiFile1 = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CT.nii.gz"
    //val file1 = parseNifti(niftiFile1, interfaceModel)

    val title = "Patient_1"
    val inputFiles = listOf("scan1.nii", "scan2.nii") // Example input NIfTI files
    val outputFiles = listOf("output1.nii", "output2.nii") // Example output NIfTI files



    interfaceModel.parseNiftiData(title, inputFiles, outputFiles)

    MainPanelLayout(
        leftPanelWidth = leftPanelWidth,
        leftPanelExpanded = leftPanelExpanded,
        rightPanelWidth = rightPanelWidth,
        rightPanelExpanded = rightPanelExpanded,
        toggleLeftPanel = {interfaceModel.toggleLeftPanelExpanded()},
        toggleRightPanel = {interfaceModel.toggleRightPanelExpanded()},


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
                            val images = interfaceModel.getNiftiImages(filename)
                            val imageIndices by interfaceModel.getImageIndices(filename).collectAsState()
                            images?.let { (axial, coronal, sagittal) ->
                                val (axialIndex, coronalIndex, sagittalIndex) = imageIndices

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    if (selectedViews.contains(NiftiView.AXIAL.toString())) imageDisplay(axial, axialIndex, NiftiView.AXIAL.toString(), 2f)
                                    if (selectedViews.contains(NiftiView.CORONAL.toString())) imageDisplay(coronal, coronalIndex, NiftiView.CORONAL.toString(),2f)
                                    if (selectedViews.contains(NiftiView.SAGITTAL.toString())) imageDisplay(sagittal, sagittalIndex, NiftiView.SAGITTAL.toString(),2f)
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
                    bottomMenu(
                        selectedViews,
                        onCheckboxChanged = interfaceModel::updateSelectedViews,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        ,
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
                        IconButton(onClick = { interfaceModel.toggleRightPanelExpanded()}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Collapse Right")
                        }
                    }

                    Text("Right Panel")

                    ScrollSlider(
                        selectedData = interfaceModel.selectedData,
                        scrollStep = interfaceModel.scrollStep,
                        maxIndexMap = interfaceModel.maxSelectedImageIndex,
                        onUpdate = { value -> interfaceModel.setScrollPosition(value.toFloat()) } // Convert Int -> Float
                    )


                }


            }

        }

    )
}

@Composable
fun ScrollSlider(
    selectedData: StateFlow<Set<String>>,
    scrollStep: StateFlow<Int>,
    maxIndexMap: StateFlow<Map<String, Float>>,
    onUpdate: (Int) -> Unit
) {
    val currentScrollStep by scrollStep.collectAsState()
    val selectedFilenames by selectedData.collectAsState()
    val maxSizeMap by maxIndexMap.collectAsState()

    val maxValue = selectedFilenames.mapNotNull { maxSizeMap[it]?.toInt() }.maxOrNull() ?: 1

    Column {
        Slider(
            value = currentScrollStep.toFloat(),
            onValueChange = { newValue ->
                onUpdate(newValue.toInt())
            },
            valueRange = 0f..maxValue.toFloat(),
            steps = maxValue - 1,
            colors = SliderDefaults.colors(
                thumbColor = Color.Gray,
                activeTrackColor = Color.Blue,
                inactiveTrackColor = Color.Green,
            )
        )
        Text(text = "Step: ${currentScrollStep.toFloat().toInt()} / $maxValue")
    }
}


@Composable
fun imageDisplay(images: List<BufferedImage>, index: Int, label: String, scaleFactor: Float = 2f) {
    var hoverPosition by remember { mutableStateOf<Point?>(null) }
    var hoverColor by remember { mutableStateOf<Color?>(null) }
    var intensity by remember { mutableStateOf<Int?>(null) }
    var cursorPosition by remember { mutableStateOf(Offset.Zero) }

    if (images.isEmpty()) {
        Text("No images")
        return
    }

    val image = images[index]
    val bitmap = image.toComposeImageBitmap()

    // Calculate scaled dimensions
    val scaledWidth = (image.width * scaleFactor).toInt()
    val scaledHeight = (image.height * scaleFactor).toInt()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$label - Slice $index")

        Box(
            modifier = Modifier
                .size(scaledWidth.dp, scaledHeight.dp)
                .pointerInput(Unit) {
                    detectPointerMovement(image, scaleFactor) { x, y, position ->
                        hoverPosition = Point(x, y)
                        val rgb = image.getRGB(x, y) // Get pixel color as int
                        hoverColor = Color(rgb)

                        intensity = extractIntensity(rgb) // Extract intensity from pixel
                        cursorPosition = position // Store cursor position for the popup
                    }
                }
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "$label Image",
                modifier = Modifier.size(scaledWidth.dp, scaledHeight.dp)
            )

            // Show popup next to cursor
            hoverPosition?.let { pos ->
                intensity?.let { value ->
                    HoverPopup(cursorPosition, pos, value)
                }
            }
        }
    }
}

/**
 * Detects mouse movement and maps scaled coordinates back to the original image pixel positions.
 */
suspend fun PointerInputScope.detectPointerMovement(
    image: BufferedImage,
    scaleFactor: Float,
    onPixelHover: (Int, Int, Offset) -> Unit
) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val position = event.changes.first().position

            // Convert screen position to image pixel coordinates
            val x = (position.x / scaleFactor).toInt().coerceIn(0, image.width - 1)
            val y = (position.y / scaleFactor).toInt().coerceIn(0, image.height - 1)

            onPixelHover(x, y, position)
        }
    }
}

/**
 * Small popup card showing hover pixel values.
 */
@Composable
fun HoverPopup(cursorPosition: Offset, hoverPosition: Point, intensity: Int) {
    Box(
        modifier = Modifier
            .offset(cursorPosition.x.dp + 16.dp, cursorPosition.y.dp + 16.dp) // Position slightly to the right of the cursor
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text("X: ${hoverPosition.x}, Y: ${hoverPosition.y}", fontSize = 12.sp)
            Text("Intensity: $intensity", fontSize = 12.sp)
        }
    }
}

/**
 * Extract intensity from an RGB pixel.
 * - If the image is grayscale, it returns the grayscale value.
 * - If the image is color, it converts to grayscale using luminance formula.
 */
fun extractIntensity(rgb: Int): Int {
    val r = (rgb shr 16) and 0xFF // Extract Red
    val g = (rgb shr 8) and 0xFF  // Extract Green
    val b = rgb and 0xFF          // Extract Blue

    return ((0.299 * r) + (0.587 * g) + (0.114 * b)).toInt() // Convert to grayscale intensity
}


