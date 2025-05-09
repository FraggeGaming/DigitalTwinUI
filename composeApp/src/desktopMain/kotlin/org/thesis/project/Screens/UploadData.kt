package org.thesis.project.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.thesis.project.Components.*
import org.thesis.project.Model.FileMappingFull
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.UploadFileMetadata
import org.thesis.project.TooltipManager
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun uploadData(
    interfaceModel: InterfaceModel,
    navController: NavHostController
) {
    val uploadedFiles by interfaceModel.fileUploader.uploadedFileMetadata.collectAsState()
    var currentlySelected by remember { mutableStateOf<UploadFileMetadata?>(null) }
    var showModelPopup by remember { mutableStateOf(false) }
    val models by interfaceModel.modelRunner.mLModels.collectAsState()
    val selectedMappings by interfaceModel.niftiRepo.jsonMapper.selectedMappings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val hasFetchedModels by interfaceModel.modelRunner.hasFetchedModels.collectAsState()
    val mappings by interfaceModel.niftiRepo.jsonMapper.mappings.collectAsState()

    //Determines if info popout should appear
    val infoMode = interfaceModel.infoMode.collectAsState()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        LaunchedEffect(Unit){
            interfaceModel.niftiRepo.jsonMapper.loadMappings()
            interfaceModel.setInfoMode(false)
            TooltipManager.clearAll()
            interfaceModel.resetRegionsAndModalities()
        }

        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComponentInfoBox(
                    id = "previousSavedCards",
                    infoMode,
                    infoText =
                        "This section displays your previously generated results. " +
                            "You can view or delete them.",
                    content = {
                        previousSavedCards(
                            mappings = mappings,
                            selectedMappings = selectedMappings,
                            interfaceModel = interfaceModel,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = mappings.isNotEmpty(),
                    arrowDirection = TooltipArrowDirection.Top
                )

            }

            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ComponentInfoBox(
                    id = "FileUploadComponent",
                    infoMode,
                    infoText = "Use this button to upload NIfTI files for viewing or AI-based translation.",
                    content = {
                        FileUploadComponent(
                            text = "Click To Upload File",
                            interfaceModel.fileUploader::addFile,
                        )
                    },
                    enabled = true,
                    arrowDirection = TooltipArrowDirection.Bottom
                )

                uploadedFiles.forEachIndexed { index, metadata ->
                    println(uploadedFiles.toString())

                    ComponentInfoBox(
                        id = "UploadInputCard",
                        infoMode,
                        infoText = "Fill out the form to run the generation, or to visualize the added data",
                        content = {
                            UploadInputCard(
                                metadata = metadata,
                                interfaceModel = interfaceModel,
                                index = index,
                                onSelect = { currentlySelected = it },
                                onShowPopup = { showModelPopup = true },
                                coroutineScope,
                                snackbarHostState,
                                uploadedFiles

                            )
                        },
                        enabled = uploadedFiles.isNotEmpty(),
                        arrowDirection = TooltipArrowDirection.Top
                    )

                }

                if(uploadedFiles.isNotEmpty() || selectedMappings.isNotEmpty()){
                    ComponentInfoBox(
                        id = "RunModelComponent",
                        infoMode,
                        infoText = "Use this button to view the selected files",
                        content = {
                            Button(
                                onClick = {
                                    val (canContinue, errorMsg) = interfaceModel.fileUploader.uploadRunCheck(uploadedFiles, mappings)

                                    if (canContinue){
                                        interfaceModel.triggerModelRun()
                                        navController.navigate("main")
                                    }
                                    else{
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(errorMsg)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LocalAppColors.current.primaryBlue,
                                    contentColor = LocalAppColors.current.textColor
                                ),
                                modifier = Modifier.width(200.dp).height(100.dp)
                            ) {
                                Text("View files", textAlign = TextAlign.Center, color = Color.White)
                            }
                        },
                        enabled = true,
                        arrowDirection = TooltipArrowDirection.Bottom
                    )
                }
            }
            if (showModelPopup && currentlySelected != null) {
                coroutineScope.launch {
                    interfaceModel.modelRunner.fetchMLModels(metadata = currentlySelected!!)
                }

                Dialog(onDismissRequest = { showModelPopup = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .widthIn(max = 1000.dp)
                            .wrapContentHeight()
                            .padding(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()).fillMaxWidth()) {

                            if (!hasFetchedModels){
                                Text("Fetching generative models...")
                            }

                            else if (models.isEmpty()){
                                Text("No matching models exists. Please select different modalities or regions")
                            }

                            else {
                                Text("Matching Models", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(12.dp))

                                val matchingModels = models.filter { model ->
                                    model.inputModality == currentlySelected?.modality
                                }


                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    maxItemsInEachRow = 3,

                                ) {
                                    println(matchingModels)
                                    matchingModels.forEach { model ->
                                        standardCard(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            content = {

                                                Text(text = model.title, style = MaterialTheme.typography.titleMedium)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = model.description,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    "Input: ${model.inputModality}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    "Output: ${model.outputModality}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Button(onClick = {
                                                    coroutineScope.launch {
                                                        val updated = currentlySelected!!.copy(model = model)
                                                        val fileIndex =
                                                            uploadedFiles.indexOf(currentlySelected!!)
                                                        interfaceModel.fileUploader.updateMetadata(
                                                            fileIndex,
                                                            updated
                                                        )
                                                        showModelPopup = false
                                                    }
                                                }
                                                ) {
                                                    Text("Select Model", color = Color.White)
                                                }

                                            }
                                        )
                                    }
                                }
                            }


                            Spacer(Modifier.height(16.dp))
                            TextButton(onClick = { showModelPopup = false }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun UploadInputCard(
    metadata: UploadFileMetadata,
    interfaceModel: InterfaceModel,
    index: Int,
    onSelect: (UploadFileMetadata) -> Unit,
    onShowPopup: () -> Unit,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    uploadedFiles: List<UploadFileMetadata>
) {

    val regions by interfaceModel.regions.collectAsState()
    val modalities by interfaceModel.modalities.collectAsState()

    if (modalities.isEmpty()){
        coroutineScope.launch {
            interfaceModel.fetchModalities()
        }

    }

    if (regions.isEmpty()){
        coroutineScope.launch {
            interfaceModel.fetchRegions()
        }
    }

    standardCard(
        modifier = Modifier.width(300.dp).heightIn(min = 500.dp, max = 700.dp),
        contentAlignment = Alignment.CenterHorizontally,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("File: ${File(metadata.filePath).name}", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = metadata.title,
                    onValueChange = {
                        val updated = metadata.copy(title = it)
                        interfaceModel.fileUploader.updateMetadata(index, updated)
                    },
                    label = { Text("Title") },
                    isError = false
                )


                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )

                if(metadata.model == null){
                    Text("Only fill out the fields below if the file is to be translated",
                        style = MaterialTheme.typography.bodySmall
                    )

                }

                dropDownMenuCustom(
                    label = "Modality",
                    selected = metadata.modality,
                    options = interfaceModel.modalities.value,
                    onSelected = {
                        val updated = metadata.copy(modality = it).copy(model = null)
                        interfaceModel.fileUploader.updateMetadata(index, updated)
                    }
                )

                dropDownMenuCustom(
                    label = "Region",
                    selected = metadata.region,
                    options = interfaceModel.regions.value,
                    onSelected = {
                        val updated = metadata.copy(region = it).copy(model = null)
                        interfaceModel.fileUploader.updateMetadata(index, updated)
                    }
                )

                //Model card

                if (metadata.model != null) {
                    standardCard(
                        modifier = Modifier.width(300.dp),
                        content = {
                            Text(text = metadata.model!!.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Input Modality: ${metadata.model!!.inputModality}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Output Modality: ${metadata.model!!.outputModality}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            TextButton(onClick = {
                                onSelect(metadata)
                                onShowPopup()
                            }) {
                                Text("Change Model")
                            }

                        }
                    )
                }

                //Select Model
                else{
                    Button(
                        onClick = {
                            val missingField = when {
                                metadata.modality.isBlank() -> "Modality"
                                metadata.title.isBlank() -> "Title"
                                metadata.region.isBlank() -> "Region"
                                else -> null
                            }
                            print(missingField)

                            if (missingField != null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please select a $missingField first.")
                                }
                            } else {
                                onSelect(metadata)
                                onShowPopup()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.primaryBlue,
                            contentColor = LocalAppColors.current.textColor
                        ),
                        shape = RoundedCornerShape(4.dp)

                    ) {
                        Text("Select Model" , color = Color.White)
                    }
                }

                //Add ground truth
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    if (metadata.groundTruthFilePath.isEmpty()) {
                        Text("Ground Truth File")
                        fileUploadCircle(onSelected = {
                            val updated = metadata.copy(groundTruthFilePath = it)
                            interfaceModel.fileUploader.updateMetadata(index, updated)
                            println(uploadedFiles)
                        })
                    }

                    else {

                        Text(
                            text = File(metadata.groundTruthFilePath).name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                val updated = metadata.copy(groundTruthFilePath = "")
                                interfaceModel.fileUploader.updateMetadata(index, updated)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove file",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { interfaceModel.fileUploader.removeFile(index) }) {
                        Text("Remove")
                    }
                }
            }

        }
    )
}

@Composable
fun previousSavedCards(
    mappings: List<FileMappingFull>,
    selectedMappings: List<FileMappingFull>,
    interfaceModel: InterfaceModel,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                        if (delta != 0f) {
                            coroutineScope.launch {
                                scrollState.scrollBy(delta*10)
                            }
                        }
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            mappings.forEach { mapping ->
                val isSelected = mapping in selectedMappings

                standardCard(
                    modifier = Modifier.width(200.dp),
                    contentAlignment = Alignment.CenterHorizontally,
                    content = {
                        Text(
                            text = mapping.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                interfaceModel.niftiRepo.jsonMapper.toggleSelectedMapping(mapping)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Select Mapping",
                                    tint = if (isSelected) Color.Green else Color.Black
                                )
                            }

                            IconButton(onClick = {
                                interfaceModel.niftiRepo.fullDelete(mapping)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Mapping", tint = Color.Red)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ComponentInfoBox(
    id: String,
    showInfo: State<Boolean>,
    infoText: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    arrowDirection: TooltipArrowDirection = TooltipArrowDirection.Left
) {
    val position = remember { mutableStateOf(Offset.Zero) }
    val size = remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned {
            position.value = it.positionInRoot()
            size.value = it.size
        }
    ) {

        content()
    }

    val TOOLTIP_WIDTH = 250f
    val TOOLTIP_HEIGHT = 60f

    val arrowPadding = 70f

    //TODO move to interfacemodel
    //Determine where to place the tooltip based on direction
    val tooltipOffset = when (arrowDirection) {
        TooltipArrowDirection.Left -> Offset(
            position.value.x + size.value.width - 15f,
            position.value.y + size.value.height / 2f - TOOLTIP_HEIGHT / 2f
        )
        TooltipArrowDirection.Right -> Offset(
            position.value.x - TOOLTIP_WIDTH - 15f,
            position.value.y + size.value.height / 2f - TOOLTIP_HEIGHT / 2f
        )
        TooltipArrowDirection.Bottom -> Offset(
            position.value.x + size.value.width / 2f - 125f,
            position.value.y - arrowPadding
        )
        TooltipArrowDirection.Top -> Offset(
            position.value.x + size.value.width / 2f - 125f,
            position.value.y + size.value.height - 15f
        )
    }

    // Register floating tooltip
    LaunchedEffect(showInfo.value, enabled, position.value, size.value) {
        TooltipManager.clear(id)
        if (showInfo.value && enabled) {
            TooltipManager.show(id) {
                InfoBox(
                    text = infoText,
                    position = tooltipOffset,
                    arrowDirection = arrowDirection
                )
            }
        }
        if (!showInfo.value) {
            TooltipManager.clearAll()
        }
    }
}








