package org.thesis.project.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.thesis.project.Components.FileUploadComponent
import org.thesis.project.Components.standardCard
import org.thesis.project.Components.topAppBar
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.UploadFileMetadata
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun uploadData(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit,
    navController: NavHostController
) {
    val uploadedFiles by interfaceModel.fileUploader.uploadedFileMetadata.collectAsState()
    var currentlySelected by remember { mutableStateOf<UploadFileMetadata?>(null) }
    var showModelPopup by remember { mutableStateOf(false) }
    val models by interfaceModel.modelRunner.mLModels.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            topAppBar(title = "App Name" , modelName = "Model Name")

            navMenu()

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.width(400.dp).wrapContentHeight()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    uploadedFiles.forEachIndexed { index, metadata ->
                        standardCard(
                            contentAlignment = Alignment.CenterHorizontally,
                            content = {
                                Text("File: ${File(metadata.filePath).name}", style = MaterialTheme.typography.body1)

                                OutlinedTextField(
                                    value = metadata.title,
                                    onValueChange = {
                                        val updated = metadata.copy(title = it)
                                        interfaceModel.fileUploader.updateMetadata(index, updated)
                                    },
                                    label = { Text("Title") },
                                    isError = false
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    DropdownMenuField(
                                        label = "Modality",
                                        selected = metadata.modality,
                                        options = listOf("CT", "PET", "MRI"),
                                        onSelected = {
                                            val updated = metadata.copy(modality = it)
                                            interfaceModel.fileUploader.updateMetadata(index, updated)
                                        }
                                    )

                                    DropdownMenuField(
                                        label = "Region",
                                        selected = metadata.region,
                                        options = listOf("Head", "Lung", "Total Body"),
                                        onSelected = {
                                            val updated = metadata.copy(region = it)
                                            interfaceModel.fileUploader.updateMetadata(index, updated)
                                        }
                                    )
                                }

                                if (metadata.model != null) {
                                    standardCard(
                                        modifier = Modifier.width(300.dp),
                                        content = {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(text = metadata.model!!.title, style = MaterialTheme.typography.h6)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = metadata.model!!.description, style = MaterialTheme.typography.body1)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Input Modality: ${metadata.model!!.inputModality}", style = MaterialTheme.typography.caption)
                                                Text("Output Modality: ${metadata.model!!.outputModality}", style = MaterialTheme.typography.caption)
                                            }
                                        }
                                    )
                                }

                                Button(
                                    onClick = {
                                        val missingField = when {
                                            metadata.modality.isBlank() -> "Modality"
                                            metadata.title.isBlank() -> "Title"
                                            metadata.region.isBlank() -> "Region"
                                            else -> null
                                        }

                                        if (missingField != null) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Please select a $missingField first.")
                                            }
                                        } else {
                                            currentlySelected = metadata
                                            showModelPopup = true
                                        }
                                    }
                                ) {
                                    Text("Select Model")
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { interfaceModel.fileUploader.removeFile(index) }) {
                                        Text("Remove")
                                    }
                                }
                            }
                        )
                    }

                    if (showModelPopup && currentlySelected != null) {
                        Dialog(onDismissRequest = { showModelPopup = false }) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                elevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .wrapContentHeight()
                                    .padding(16.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Matching Models", style = MaterialTheme.typography.h6)
                                    Spacer(Modifier.height(12.dp))

                                    val matchingModels = models.filter { model ->
                                        model.inputModality == currentlySelected?.modality
                                    }

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        maxItemsInEachRow = 2
                                    ) {
                                        matchingModels.forEach { model ->
                                            standardCard(
                                                modifier = Modifier.width(300.dp),
                                                content = {
                                                    Column(Modifier.padding(16.dp)) {
                                                        Text(text = model.title, style = MaterialTheme.typography.h6)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(text = model.description, style = MaterialTheme.typography.body1)
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text("Input: ${model.inputModality}", style = MaterialTheme.typography.caption)
                                                        Text("Output: ${model.outputModality}", style = MaterialTheme.typography.caption)
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                        Button(onClick = {
                                                            coroutineScope.launch {
                                                                val updated = currentlySelected!!.copy(model = model)
                                                                val fileIndex = uploadedFiles.indexOf(currentlySelected!!)
                                                                interfaceModel.fileUploader.updateMetadata(fileIndex, updated)
                                                                showModelPopup = false
                                                            }
                                                        }) {
                                                            Text("Select Model")
                                                        }
                                                    }
                                                }
                                            )
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

                    FileUploadComponent(interfaceModel)

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                interfaceModel.modelRunner.runModel()
                                navController.navigate("main")
                            }
                        },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF0050A0),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.width(100.dp).height(100.dp)
                    ) {
                        Text("Run Generation", textAlign = TextAlign.Center)
                    }
                }
            }
        }
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
    val icon = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown
    val text = selected.ifEmpty { label }
    Box {
        Button(onClick = { expanded = true }) {
            Text(text)
            Spacer(Modifier.width(8.dp))
            Icon(icon, contentDescription = if (expanded) "Collapse" else "Expand")
        }
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