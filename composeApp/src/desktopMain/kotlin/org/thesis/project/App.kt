package org.thesis.project

import CardMenu
import CardWithCheckboxes
import CardWithNestedCheckboxes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import imaging.composeapp.generated.resources.Res
import imaging.composeapp.generated.resources.compose_multiplatform
import menuCard
import modalities


@Composable
@Preview
fun App() {
    MaterialTheme {
        val interfaceModel: InterfaceModel = viewModel()


        val navController = rememberNavController()

        NavHost(navController, startDestination = "main") {

            composable("upload"){
                uploadData(interfaceModel = interfaceModel, navMenu = { NavigationButtons(navController) })
            }

            composable("modelSelect"){
                modelSelect(interfaceModel = interfaceModel, navMenu = { NavigationButtons(navController) })
            }

            composable("main"){


                imageViewer(interfaceModel = interfaceModel, navMenu = { NavigationButtons(navController) })
            }
        }
    }
}

@Composable
fun NavigationButtons(navController: NavController) {
    Row(
        modifier = Modifier.size(width = 350.dp, height = 70.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { navController.navigate("upload") },
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("1. Upload", textAlign = TextAlign.Center)
        }
        Button(
            onClick = { navController.navigate("modelSelect") },
            shape = RectangleShape,
            // Center button can remain with default shape (or you can also set a shape if needed)
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("2. Model Select", textAlign = TextAlign.Center)
        }
        Button(
            onClick = { navController.navigate("main") },
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("3. Result", textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun uploadData(interfaceModel: InterfaceModel,
               navMenu: @Composable () -> Unit){
    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        navMenu()
        Text(text = "Upload Data")
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
    val nestedData by interfaceModel.nestedData.collectAsState()
    val selectedDistricts by interfaceModel.selectedDistricts.collectAsState()
    val organs by interfaceModel.organs.collectAsState()

    MainPanelLayout(

        leftContent = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(400.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    navMenu()

                    CardMenu(
                        //onShowPopup = { showPopup = !showPopup },
                        selectedData = selectedData,
                        items = nestedData,
                        onCheckboxChanged = { label, isChecked ->
                            interfaceModel.updateSelectedData(label, isChecked)

                            println("$label is ${if (isChecked) "selected" else "deselected"}")
                        }
                    )

                    CardWithCheckboxes(
                        selectedDistricts,
                        items = organs,
                        onCheckboxChanged = { organ, isChecked ->
                            interfaceModel.updateSelectedDistrict(organ, isChecked)
                            println("$organ is ${if (isChecked) "selected" else "deselected"}")
                        }
                    )
                }
            }

        },
        centerContent = {

            Column(
                modifier = Modifier
                    .fillMaxSize() // Fill the entire Box
                    .padding(bottom = 20.dp), // Optional padding to give room for menuCard
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // Push content to top/bottom


            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Center Panel")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                )
                {
                    selectedData.forEach { selected ->
                        Text(text = selected)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                )
                {
                    selectedDistricts.forEach { selected ->
                        Text(text = selected)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }


                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(16.dp)
                )
                {
                    menuCard(
                        modifier = Modifier.fillMaxWidth(),
                        content = listOf(
                            {
                                var checked by remember { mutableStateOf(false) }

                                Row(
                                    modifier = Modifier
                                        .clickable { checked = !checked }, // Toggle the checkbox state when Row is clicked
                                    verticalAlignment = Alignment.CenterVertically // Align Checkbox and Text
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { checked = it }
                                    )
                                    Text(text = "Axial")
                                }


                            },

                            {
                                var checked by remember { mutableStateOf(false) }

                                Row(
                                    modifier = Modifier
                                        .clickable { checked = !checked }, // Toggle the checkbox state when Row is clicked
                                    verticalAlignment = Alignment.CenterVertically // Align Checkbox and Text
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { checked = it }
                                    )
                                    Text(text = "Coronal")
                                }
                            },

                            {
                                var checked by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .clickable { checked = !checked }, // Toggle the checkbox state when Row is clicked
                                    verticalAlignment = Alignment.CenterVertically // Align Checkbox and Text
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { checked = it }
                                    )
                                    Text(text = "Sagittal")
                                }

                                //clickableCheckBox(text = "Sagittal", model.selectedSettings)
                            },

                            {

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(50.dp)
                                        .background(Color.Gray)  // Line color
                                )


                            },

                            {
                                TextButton(
                                    onClick = { /* Handle click */ },
                                    colors = ButtonDefaults.textButtonColors(
                                        backgroundColor = Color.Transparent, // No background
                                        contentColor = Color.Unspecified // Keeps default text/icon color
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
                                    onClick = { /* Handle click */ },
                                    colors = ButtonDefaults.textButtonColors(
                                        backgroundColor = Color.Transparent, // No background
                                        contentColor = Color.Unspecified // Keeps default text/icon color
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
        },
        rightContent = {
            Text("Right Panel")
        }

    )
}

