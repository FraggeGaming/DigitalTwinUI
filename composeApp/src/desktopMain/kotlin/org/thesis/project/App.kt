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
import kotlinx.serialization.json.Json
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.NiftiView
import java.awt.image.BufferedImage
import java.awt.Point
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
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
    //val niftiFile = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_CT\\liver_CT.nii.gz"
    //val file = parseNifti(niftiFile, interfaceModel)

    //val niftiFile1 = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CT.nii.gz"
    //val file1 = parseNifti(niftiFile1, interfaceModel)

    val file1 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_CT\\brain_CT.nii.gz"

    val file2= "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_PET\\brain_PET.nii.gz"

    val title = "Patient_1"
    val inputFiles = listOf(file1) // Example input NIfTI files
    val outputFiles = listOf(file2) // Example output NIfTI files



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

                            val (axialIndex, coronalIndex, sagittalIndex) = imageIndices

                            if (images != null){
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    if (selectedViews.contains(NiftiView.AXIAL.toString()))
                                        imageDisplay(images.axialImages ,images.axialVoxels, axialIndex, NiftiView.AXIAL.toString(), 2f)
                                    if (selectedViews.contains(NiftiView.CORONAL.toString()))
                                        imageDisplay(images.coronalImages, images.coronalVoxels, coronalIndex, NiftiView.CORONAL.toString(),2f)
                                    if (selectedViews.contains(NiftiView.SAGITTAL.toString()))
                                        imageDisplay(images.sagittalImages, images.sagittalVoxels, sagittalIndex, NiftiView.SAGITTAL.toString(),2f)
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
fun imageDisplay(
    images: List<BufferedImage>,
    voxelData: List<List<List<Float>>>, // 3D voxel data (slices -> rows -> columns)
    index: Int, // Slice index
    label: String,
    scaleFactor: Float = 2f
) {
    var hoverPosition by remember { mutableStateOf<Point?>(null) }
    var voxelValue by remember { mutableStateOf<Float?>(null) }
    var cursorPosition by remember { mutableStateOf(Offset.Zero) }

    if (images.isEmpty() || voxelData.isEmpty() || voxelData.size <= index) {
        Text("No images")
        return
    }

    val image = images[index]
    val bitmap = image.toComposeImageBitmap()
    val currentVoxelSlice = voxelData[index] // Get the updated 2D voxel slice

    // Reset hover state when index changes
    LaunchedEffect(index) {
        hoverPosition = null
        voxelValue = null
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$label - Slice $index")

        // Force full recomposition on index change
        key(index) {
            Box(
                modifier = Modifier
                    .size((image.width * scaleFactor).dp, (image.height * scaleFactor).dp)
                    .pointerInput(Unit) { // This will recompose when key(index) triggers
                        detectPointerMovement(image, scaleFactor, currentVoxelSlice) { x, y, position, voxel ->
                            hoverPosition = Point(x, y)
                            voxelValue = voxel
                            cursorPosition = position
                        }
                    }
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "$label Image",
                    modifier = Modifier.size((image.width * scaleFactor).dp, (image.height * scaleFactor).dp)
                )

                hoverPosition?.let { pos ->
                    voxelValue?.let { value ->
                        val formattedValue = formatVoxelValue(value, "CT") // Adjust modality as needed
                        HoverPopup(cursorPosition, pos, formattedValue)
                    }
                }
            }
        }
    }
}



fun formatVoxelValue(value: Float, modality: String): String {
    return when (modality) {
        "CT" -> "HU: ${value.toInt()}"  // Hounsfield Units (CT)
        "MRI" -> "Signal Intensity: ${"%.3f".format(value)}"  // MRI Signal
        "PET" -> "SUV: ${"%.2f".format(value)}"  // Standardized Uptake Value (PET)
        else -> "Value: ${"%.3f".format(value)}"
    }
}



suspend fun PointerInputScope.detectPointerMovement(
    image: BufferedImage,
    scaleFactor: Float,
    voxelSlice: List<List<Float>>, // 2D voxel data for the current slice
    onVoxelHover: (Int, Int, Offset, Float) -> Unit
) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val position = event.changes.first().position

            val x = (position.x / scaleFactor).toInt()
            val y = (position.y / scaleFactor).toInt()

            // Ensure x and y are within bounds
            if (x in voxelSlice[0].indices && y in voxelSlice.indices) {
                val voxelValue = voxelSlice[y][x] // Access voxel value from 2D list
                onVoxelHover(x, y, position, voxelValue)
            }
        }
    }
}

/**
 * Small popup card showing hover pixel values.
 */
@Composable
fun HoverPopup(cursorPosition: Offset, hoverPosition: Point, voxelValue: String) {
    Box(
        modifier = Modifier
            .offset(cursorPosition.x.dp + 16.dp, cursorPosition.y.dp + 16.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text("X: ${hoverPosition.x}, Y: ${hoverPosition.y}", fontSize = 12.sp)
            Text("Voxel Value: $voxelValue", fontSize = 12.sp)
        }
    }
}




