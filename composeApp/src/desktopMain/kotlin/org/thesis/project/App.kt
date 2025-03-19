package org.thesis.project

import CardMenu
import CardWithCheckboxes
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import menuCard
import navigationButtons
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.thesis.project.Components.voxelImageDisplay
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.NiftiView
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import org.thesis.project.Components.FileUploadComponent
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
                uploadData(interfaceModel = interfaceModel, navMenu = { navigationButtons(navController, "upload") }, navController)
            }

            composable("modelSelect") {
                modelSelect(
                    interfaceModel = interfaceModel,
                    navMenu = { navigationButtons(navController, "modelSelect") }, navController)
            }

            composable("main") {


                imageViewer(interfaceModel = interfaceModel, navMenu = { navigationButtons(navController, "main") }, navController)
            }
        }
    }
}


@Composable
fun uploadData(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit,
    navController: NavHostController
) {
    val uploadedFiles by interfaceModel.uploadedFilesMetadata.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF0050A0)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Title",
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = "App Logo",
                    tint = Color.White
                )
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Model Name", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "Logo",
                        tint = Color.White
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                IconButton(onClick = { /* show info */ }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }

                IconButton(
                    onClick = { interfaceModel.toggleRightPanelExpanded() }
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
                }
            }
        }


        navMenu()
        Text(text = "Upload Data")
        FileUploadComponent(interfaceModel)

        uploadedFiles.forEachIndexed { index, metadata ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = 8.dp
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("File: ${metadata.filePath}", style = MaterialTheme.typography.body1)

                    OutlinedTextField(
                        value = metadata.title,
                        onValueChange = {
                            val updated = metadata.copy(title = it)
                            interfaceModel.updateFileMetadata(index, updated)
                        },
                        label = { Text("Title") },
                        isError = uploadedFiles.withIndex().any { (i, item) -> i != index && item.title == metadata.title }
                    )

                    DropdownMenuField(
                        label = "Modality",
                        selected = metadata.modality,
                        options = listOf("CT", "PET", "MRI"),
                        onSelected = {
                            val updated = metadata.copy(modality = it)
                            interfaceModel.updateFileMetadata(index, updated)
                        }
                    )

                    DropdownMenuField(
                        label = "Region",
                        selected = metadata.region,
                        options = listOf("Head", "Lung", "Total Body"),
                        onSelected = {
                            val updated = metadata.copy(region = it)
                            interfaceModel.updateFileMetadata(index, updated)
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { interfaceModel.removeUploadedFile(index) }) {
                            Text("Remove")
                        }
                    }
                }
            }
        }

        Button(
            onClick = { navController.navigate("modelSelect") },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF0050A0), // Blue background
                contentColor = Color.White
            ),
            modifier = Modifier
                .weight(1f)
                //.fillMaxHeight()
        ) {
            Text("Save and Choose Model", textAlign = TextAlign.Center)
        }

        //For each file in
        //    private val _filesToUpload = MutableStateFlow(listOf<String>())
        //    val filesToUpload: StateFlow<List<String>> = _filesToUpload.asStateFlow()
        //Set title, Modality (CT, PET, etc), if title already exisist, show popup
        //Let user set the district, or if its total body (this is then used in the model selection)
        //Let the user remove this file if they want
        //Save this all to the model in one dataclass then go to "choose model"

        //Create this dataclass in the model
        /*
        * DataClass input
        *
        * String title
        *
        * * Modality (CT, PET, etc)
        * District, or total body
        *
        * Model (Apply in next screen)
        *
        * NiftiData - will be set later on after the model has been run so dont touch now
        * */
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DropdownMenuField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                ){
                    Text(option)
                }
            }
        }
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun modelSelect(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit,
    navController: NavHostController
) {
    val uploadedFiles by interfaceModel.uploadedFilesMetadata.collectAsState()
    val models by interfaceModel.mLModels.collectAsState()

    val matchingModels = models.filter { model ->
        uploadedFiles.any { it.modality == model.inputModality }
    }

    uploadedFiles.forEach { file ->
        standardCard(
            content = {
                Text(file.title)
                Text(file.modality)
                Text(file.region)
            }
        )
    }


    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF0050A0)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Title",
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = "App Logo",
                    tint = Color.White
                )
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Model Name", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "Logo",
                        tint = Color.White
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                IconButton(onClick = { /* show info */ }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }

                IconButton(
                    onClick = { interfaceModel.toggleRightPanelExpanded() }
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
                }
            }
        }


        val scrollState = rememberScrollState()

        // Left Panel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color.LightGray)
                .padding(12.dp), // move padding here
            verticalArrangement = Arrangement.spacedBy(16.dp), // combine spacing
            horizontalAlignment = Alignment.Start
        ) {
            navMenu()
        }


        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 2 // or 3 depending on how many you want per row
            ) {
                matchingModels.forEach { model ->
                    standardCard(
                        modifier = Modifier
                            .width(300.dp), // Adjust width to control card sizing
                        content = {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = model.title, style = MaterialTheme.typography.h6)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = model.description, style = MaterialTheme.typography.body1)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Input Modality: ${model.inputModality}", style = MaterialTheme.typography.caption)
                                Text("Output Modality: ${model.outputModality}", style = MaterialTheme.typography.caption)

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        println(model)
                                    }
                                ) {
                                    Text("Select Model")
                                }
                            }
                        }
                    )
                }
            }
        }


        Button(
            onClick = { navController.navigate("main") },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF0050A0), // Blue background
                contentColor = Color.White
            ),
            modifier = Modifier
                .weight(1f)
            //.fillMaxHeight()
        ) {
            Text("2. Model Select", textAlign = TextAlign.Center)
        }


        //show all models that have inputModalty that has the same modality as the uploadedfiles.modality
        //model should be shown with
        //standardCard
        //then title, description and a button to select it /run
    }
}


@Composable
fun imageViewer(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit,
    navController: NavHostController
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
    val niftiFile = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"


    val niftiFile1 = "C:\\Users\\User\\Desktop\\Exjob\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\BOX_PET\\liver_PET.nii.gz"

    //val file1 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\CTres.nii.gz"

    //val file2 = "G:\\Coding\\Imaging\\composeApp\\src\\desktopMain\\resources\\testScans\\PET.nii.gz"
    //TODO change this to a dynamic path

    val title = "Patient_1"
    val inputFiles = listOf(niftiFile) //Example input NIfTI files
    val outputFiles = listOf(niftiFile1) //Example output NIfTI files

    interfaceModel.updateSelectedViews(NiftiView.AXIAL, true)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF0050A0)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Title",
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = "App Logo",
                    tint = Color.White
                )
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Model Name", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "Logo",
                        tint = Color.White
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                IconButton(onClick = { /* show info */ }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }

                IconButton(
                    onClick = { interfaceModel.toggleRightPanelExpanded() }
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
            toggleLeftPanel = { interfaceModel.toggleLeftPanelExpanded() },
            toggleRightPanel = { interfaceModel.toggleRightPanelExpanded() },

            leftContent = {


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

                val coroutineScope = rememberCoroutineScope()

                Button(onClick = {
                    coroutineScope.launch {
                        interfaceModel.parseNiftiData(title, inputFiles, outputFiles)
                    }
                }) {
                    Text("Parse NIfTI")
                }
                standardCard (
                    content ={
                        ScrollSlider(
                            selectedData = interfaceModel.selectedData,
                            scrollStep = interfaceModel.scrollStep,
                            maxIndexMap = interfaceModel.maxSelectedImageIndex,
                            onUpdate = { value -> interfaceModel.setScrollPosition(value) } // Convert Int -> Float
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
                            val imageIndices by interfaceModel.getImageIndices(filename).collectAsState()
                            val currentIndex = when (selectedView) {
                                NiftiView.AXIAL -> imageIndices.first
                                NiftiView.CORONAL -> imageIndices.second
                                NiftiView.SAGITTAL -> imageIndices.third
                            }

                            val (slices, spacingPx) = interfaceModel.getSlicesFromVolume(selectedView, filename)
                            val modality = interfaceModel.extractModality(filename)

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
                                        if (modality != null) {

                                                voxelImageDisplay(
                                                    voxelSlice = slices[currentIndex],
                                                    interfaceModel = interfaceModel,
                                                    modality = modality,
                                                    pixelSpacing = spacingPx
                                                )
                                            }
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


@Composable
fun windowControls(interfaceModel: InterfaceModel) {
    val windowing by interfaceModel.windowing.collectAsState()
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
                        interfaceModel.windowPresets.forEach { (label, preset) ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedPresetLabel = label
                                    interfaceModel.setPreset(preset)
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
                onValueChange = { interfaceModel.setWindowing(it, windowing.width) },
                valueRange = -1000f..1000f,
                modifier = Modifier.fillMaxWidth()
            )

            scrollWithTitle(
                title = "Window Width: ${windowing.width.toInt()}",
                value = windowing.width,
                onValueChange = { interfaceModel.setWindowing(windowing.center, it) },
                valueRange = 1f..2500f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )

}

@Composable
fun scrollWithTitle(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier
){
    Text(title)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier
    )
}

@Composable
fun standardCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 12.dp,
    verticalSpacing: Dp = 8.dp,
    contentAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            //.padding(contentPadding),
        ,shape = RoundedCornerShape(12.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalAlignment = contentAlignment
            ) {
            content()
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
        Text(text = "Slice: ${currentScrollStep.toInt()} / ${maxValue.toInt()}")
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                sliderPosition = newValue
                onUpdate(newValue)
            },
            valueRange = 0f..maxValue,
            steps = 0
        )

    }

    LaunchedEffect(currentScrollStep) {
        if (currentScrollStep != sliderPosition) {
            sliderPosition = currentScrollStep
        }
    }
}