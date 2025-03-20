package org.thesis.project.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.thesis.project.Components.FileUploadComponent
import org.thesis.project.Components.topAppBar
import org.thesis.project.Model.InterfaceModel

@Composable
fun uploadData(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit,
    navController: NavHostController
) {
    val uploadedFiles by interfaceModel.fileUploader.uploadedFileMetadata.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        topAppBar(title = "App Name" , modelName = "Model Name")


        navMenu()
        Text(text = "Upload Data")
        FileUploadComponent(interfaceModel)

        uploadedFiles?.let { metadata ->
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
                            interfaceModel.fileUploader.updateMetadata(updated)
                        },
                        label = { Text("Title") },
                        isError = false // no need for duplication check anymore
                    )

                    DropdownMenuField(
                        label = "Modality",
                        selected = metadata.modality,
                        options = listOf("CT", "PET", "MRI"),
                        onSelected = {
                            val updated = metadata.copy(modality = it)
                            interfaceModel.fileUploader.updateMetadata(updated)
                        }
                    )

                    DropdownMenuField(
                        label = "Region",
                        selected = metadata.region,
                        options = listOf("Head", "Lung", "Total Body"),
                        onSelected = {
                            val updated = metadata.copy(region = it)
                            interfaceModel.fileUploader.updateMetadata(updated)
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { interfaceModel.fileUploader.removeFile() }) {
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