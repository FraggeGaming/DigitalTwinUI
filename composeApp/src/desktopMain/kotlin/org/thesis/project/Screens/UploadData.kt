package org.thesis.project.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.thesis.project.Components.*
import org.thesis.project.Model.FileMappingFull
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
    val selectedMappings by interfaceModel.niftiRepo.jsonMapper.selectedMappings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            navMenu()

            Row(){
                interfaceModel.niftiRepo.jsonMapper.loadMappings()
                val mappings = interfaceModel.niftiRepo.jsonMapper.getAllMappings()

                mappings.forEachIndexed { index, mapping ->
                    val isSelected = mapping in selectedMappings

                    standardCard(
                        modifier = Modifier
                            .width(200.dp)
                            .wrapContentHeight()
                            .background(if (isSelected) Color(0xFFA5D6A7) else Color.White), // ✅ Green background if selected
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

                                    println("Selected mappings: ${selectedMappings.map { it.title }}")
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Select Mapping")
                                }

                                IconButton(onClick = {
                                    println("Would remove mapping: ${mapping.title}")
                                    // Optional: handle deleting a mapping if needed
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Mapping")
                                }
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FileUploadComponent(
                    text = "Click To Upload File",
                    interfaceModel.fileUploader::addFile)


                uploadedFiles.forEachIndexed { index, metadata ->
                    println(uploadedFiles.toString())
                    standardCard(
                        modifier = Modifier.width(300.dp).wrapContentHeight(),
                        contentAlignment = Alignment.CenterHorizontally,
                        content = {
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


                            dropDownMenuCustom(
                                label = "Modality",
                                selected = metadata.modality,
                                options = listOf("CT", "PET", "MRI"),
                                onSelected = {
                                    val updated = metadata.copy(modality = it).copy(model = null)
                                    interfaceModel.fileUploader.updateMetadata(index, updated)
                                }
                            )

                            dropDownMenuCustom(
                                label = "Region",
                                selected = metadata.region,
                                options = listOf("Head", "Lung", "Total Body"),
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
//                                                val updated = metadata.copy(model = null)
//                                                interfaceModel.fileUploader.updateMetadata(index, updated)
                                            currentlySelected = metadata
                                            showModelPopup = true
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
                                            currentlySelected = metadata
                                            showModelPopup = true
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
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
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }


                            //Remove
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


                if(uploadedFiles.isNotEmpty() || selectedMappings.isNotEmpty()){
                    Button(
                        onClick = {

                            val canContinue = uploadCheck(coroutineScope, snackbarHostState, uploadedFiles)


                            if (canContinue){
                                coroutineScope.launch {
                                    interfaceModel.establishFolderIntegrity()

                                    interfaceModel.modelRunner.runModel()
                                    navController.navigate("main")
                                }
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.primaryBlue,
                            contentColor = LocalAppColors.current.textColor
                        ),
                        modifier = Modifier.width(200.dp).height(100.dp)
                    ) {
                        Text("Run Generation", textAlign = TextAlign.Center, color = Color.White)
                    }
                }

            }







            if (showModelPopup && currentlySelected != null) {
                Dialog(onDismissRequest = { showModelPopup = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                            .padding(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Matching Models", style = MaterialTheme.typography.titleSmall)
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

fun uploadCheck(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    uploadedFiles: List<UploadFileMetadata>
): Boolean{

    val titles = uploadedFiles.map { it.title }
    val duplicateTitles = titles.groupingBy { it }.eachCount().filter { it.value > 1 }

    if (duplicateTitles.isNotEmpty()) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Duplicate titles found: ${duplicateTitles.keys}")
        }
       return false
    }

    uploadedFiles.forEach{file ->
        val missingField = when {
            file.title.isBlank() -> "Title"
            file.modality.isBlank() -> "Modality"
            file.region.isBlank() -> "Region"
            file.model == null -> "Model"
            else -> null
        }

        if (missingField != null) {

            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please select a $missingField for ${file.filePath}.")
            }

            return false
        }
    }

    return true
}
