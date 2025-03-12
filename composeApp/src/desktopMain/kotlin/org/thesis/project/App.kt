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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import menuCard
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.NiftiView
import java.awt.Point
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import buttonWithCheckboxSet
import java.awt.image.BufferedImage


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
    val selectedSettings by interfaceModel.selectedSettings.collectAsState()

    //Takes the file path and parses the nifti
    //val niftiFile = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"


    //val niftiFile1 = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_PET\\liver_PET.nii.gz"

    val file1 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\\\CTres.nii.gz"

    val file2= "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_PET\\brain_PET.nii.gz"
    //TODO change this to a dynamic path

    val title = "Patient_1"
    val inputFiles = listOf(file1) // Example input NIfTI files
    val outputFiles = listOf(file2) // Example output NIfTI files

    var selectedOption by remember { mutableStateOf("") } //TODO add to model
    //TODO refactor to separate files. Refactor to viewmodel

    interfaceModel.parseNiftiData(title, inputFiles, outputFiles)
    interfaceModel.updateSelectedViews(NiftiView.AXIAL, true)

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

                                        val (slices, image) = interfaceModel.getSlicesFromVolume(selectedView, filename)

                                        imageDisplay(
                                            voxelData = slices,
                                            image = image,
                                            index = currentIndex,
                                            label = selectedView,
                                            scaleFactor = 1f,
                                            selectedSettings = selectedSettings,
                                            modality = interfaceModel.extractModality(filename) ?: "",
                                            interfaceModel = interfaceModel
                                        )
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
                                { Row {
                                    buttonWithCheckboxSet(selectedViews, NiftiView.AXIAL, interfaceModel::updateSelectedViews)

                                }
                                },

                                {
                                    Row{
                                        buttonWithCheckboxSet(selectedViews, NiftiView.CORONAL, interfaceModel::updateSelectedViews)

                                    }
                                },

                                {
                                    Row {
                                        buttonWithCheckboxSet(selectedViews, NiftiView.SAGITTAL, interfaceModel::updateSelectedViews)

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
                        onUpdate = { value -> interfaceModel.setScrollPosition(value) } // Convert Int -> Float
                    )



                    if (selectedSettings.contains("pixel") && selectedOption == ""){
                        RadioButtonList(
                            selectedOption = selectedOption,
                            onRadioSelected = { option ->
                                selectedOption = option
                            }
                        )
                    }


                }


            }

        }

    )
}






@Composable
fun RadioButtonList(
    selectedOption: String, // Currently selected option
    onRadioSelected: (String) -> Unit // Callback for handling selection
) {
    val options = listOf("CT", "PET", "MRI")
    //TODO change to enum from model

    Column(modifier = Modifier.padding(16.dp)) {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRadioSelected(option) } // Calls the provided onClick logic
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = { onRadioSelected(option) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option, fontSize = 16.sp)
            }
        }
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
fun imageDisplay(
    voxelData: List<List<List<Float>>>,
    image: List<BufferedImage>,
    index: Int,
    label: NiftiView,
    scaleFactor: Float = 1f,
    selectedSettings: Set<String>,
    modality: String,
    interfaceModel: InterfaceModel
) {
    if (voxelData.isEmpty() || voxelData.size <= index) {
        Text("No images")
        return
    }


    val currentVoxelSlice = voxelData[index]
    val bitmap = image[index].toComposeImageBitmap()

    var isHoveringLocal by remember { mutableStateOf(false) }
    // This will store the layout position of the Box
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var scaleFactorState by remember { mutableStateOf(scaleFactor) }

    println(modality)


    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$label - Slice $index")

        key(currentVoxelSlice) {
            Box(
                modifier = Modifier
                    .size((bitmap.width * scaleFactorState).dp, (bitmap.height * scaleFactorState).dp)
                    .onGloballyPositioned { coordinates -> layoutCoordinates = coordinates } // Get layout coordinates
                    .pointerInput(currentVoxelSlice) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val position = event.changes.first().position

                                // Use layout coordinates if available
                                val localPosition = layoutCoordinates?.localPositionOf(layoutCoordinates!!, position) ?: position

                                interfaceModel.updatePointerPosition(
                                    position = localPosition,
                                    scaleFactor = scaleFactorState,
                                    width = (bitmap.height).toInt(),
                                    height = (bitmap.width ).toInt(),
                                    currentVoxelSlice = currentVoxelSlice
                                )
                            }
                        }

                    }
                    .onPointerEvent(PointerEventType.Move) {
                    }
                    .onPointerEvent(PointerEventType.Enter) {
                        isHoveringLocal = true
                    }
                    .onPointerEvent(PointerEventType.Exit) {
                        isHoveringLocal = false
                    }
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "$label Image",
                    modifier = Modifier.size((bitmap.width * scaleFactorState).dp, (bitmap.height * scaleFactorState).dp)
                )

                if (selectedSettings.contains("pixel") && isHoveringLocal && interfaceModel.isHovering.value) {
                    interfaceModel.hoverPosition.value?.let { pos ->
                        interfaceModel.voxelValue.value?.let { value ->
                            val formattedValue = formatVoxelValue(value, modality)
                            println("modality: $modality")
                            println(formattedValue)
                            HoverPopup(
                                cursorPosition = interfaceModel.cursorPosition.value,
                                hoverPosition = pos,
                                string = formattedValue
                            )
                        }
                    }
                }
            }
        }
    }
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
    return when (modality) {
        "CT" -> "HU: ${value.toInt()}"  // Hounsfield Units (CT)
        "MRI" -> "Signal Intensity: ${"%.3f".format(value)}"  // MRI Signal
        "PET" -> "SUV: ${"%.2f".format(value)}"  // Standardized Uptake Value (PET)
        else -> "Value: ${"%.3f".format(value)}"
    }
}





