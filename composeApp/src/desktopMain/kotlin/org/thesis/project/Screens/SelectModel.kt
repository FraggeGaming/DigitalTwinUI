package org.thesis.project.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.thesis.project.Components.standardCard
import org.thesis.project.Components.topAppBar
import org.thesis.project.Model.InterfaceModel


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun modelSelect(
    interfaceModel: InterfaceModel,
    navMenu: @Composable () -> Unit,
    navController: NavHostController
) {
    val uploadedFile by interfaceModel.fileUploader.uploadedFileMetadata.collectAsState()
    val models by interfaceModel.modelRunner.mLModels.collectAsState()

    val matchingModels = models.filter { model ->
        model.inputModality == uploadedFile?.modality
    }
    val coroutineScope = rememberCoroutineScope()

    standardCard(content = {
        uploadedFile?.let { Text(it.title) }
        uploadedFile?.let { Text(it.modality) }
        uploadedFile?.let { Text(it.region) }
    }
    )
    println(uploadedFile?.title)


    println(matchingModels.toString())


    Column(modifier = Modifier.fillMaxSize()) {
        topAppBar(title = "App Name" , modelName = "Model Name")


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
                                        coroutineScope.launch {
                                            interfaceModel.modelRunner.runModel(model)
                                            navController.navigate("main")
                                        }
                                    }
                                ) {
                                    Text("Select Model")
                                }
                            }
                        }
                    )
                }
            }

            Button(
                onClick = {
                    navController.navigate("main")
                    //run backend
                },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF0050A0), // Blue background
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                //.fillMaxHeight()
            ) {
                Text("Run model", textAlign = TextAlign.Center)
            }
        }


        //show all models that have inputModalty that has the same modality as the uploadedfiles.modality
        //model should be shown with
        //standardCard
        //then title, description and a button to select it /run
    }
}