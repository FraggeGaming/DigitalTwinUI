package org.thesis.project

import CardMenu
import CardWithCheckboxes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.mouse.mouseScrollFilter
import androidx.compose.ui.input.mouse.MouseScrollEvent
import androidx.compose.ui.input.pointer.PointerEventType

import menuCard
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.parseNifti
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.jetbrains.skia.Image
import java.io.ByteArrayOutputStream

fun BufferedImage.toImageBitmapCustom(): ImageBitmap {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, "png", outputStream)
    val imageBytes = outputStream.toByteArray()
    val skiaImage = Image.makeFromEncoded(imageBytes)
    return skiaImage.toComposeImageBitmap()
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val interfaceModel: InterfaceModel = viewModel()


        val navController = rememberNavController()





        NavHost(navController, startDestination = "main") {

            composable("upload"){
                uploadData(interfaceModel = interfaceModel, navMenu = { NavigationButtons(navController, "upload") })
            }

            composable("modelSelect"){
                modelSelect(interfaceModel = interfaceModel, navMenu = { NavigationButtons(navController, "modelSelect") })
            }

            composable("main"){


                imageViewer(interfaceModel = interfaceModel, navMenu = { NavigationButtons(navController, "main") })
            }
        }
    }
}

@Composable
fun NavigationButtons(navController: NavController, selected: String) {
    Row(
        modifier = Modifier.height(height = 70.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { navController.navigate("upload") },
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected == "upload") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
                contentColor = Color.White            // White text
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("1. Upload", textAlign = TextAlign.Center)
        }
        Button(
            onClick = { navController.navigate("modelSelect") },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected == "modelSelect") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
                contentColor = Color.White            // White text
            ),
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
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected == "main") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
                contentColor = Color.White            // White text
            ),
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

fun callPythonScript(scriptPath: String, vararg args: String): String {
    // Construct the command with "python", the script path, and any additional arguments.
    val command = listOf("python", scriptPath) + args
    val process = ProcessBuilder(command)
        .redirectErrorStream(true) // merge stdout and stderr
        .start()

    // Read the process output.
    val output = process.inputStream.bufferedReader().readText()

    // Wait for the process to finish.
    process.waitFor()
    return output
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

    val leftPanelWidth by interfaceModel.leftPanelWidth.collectAsState()
    val rightPanelWidth by interfaceModel.rightPanelWidth.collectAsState()
    val leftPanelExpanded by interfaceModel.leftPanelExpanded.collectAsState()
    val rightPanelExpanded by interfaceModel.rightPanelExpanded.collectAsState()

    val selectedImageIndex by interfaceModel.selectedImageIndex.collectAsState()

   //

    val niftiFile = File( "C:\\Users\\User\\Desktop\\Exjob\\47727\\47727\\BOX_CT\\brain_CT.nii.gz")
    val (axial, coronal, sagittal) = parseNifti(niftiFile)

//    ImageIO.write(axial.first(), "png", File("axial_slice.png"))
//    ImageIO.write(coronal.first(), "png", File("coronal_slice.png"))
//    ImageIO.write(sagittal.first(), "png", File("sagittal_slice.png"))


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

//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .align(Alignment.End),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        IconButton(onClick = { interfaceModel.toggleLeftPanelExpanded() }) {
//                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Collapse Left")
//                        }
//                    }

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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Scroll) {
                                    // Here we assume the scroll delta is available on the first change.
                                    val scrollDelta = event.changes.firstOrNull()?.scrollDelta ?: Offset.Zero
                                    if (scrollDelta.y > 0f) {
                                        interfaceModel.incrementSelectedImageIndex(axial.lastIndex-1)
                                    } else if (scrollDelta.y < 0f) {
                                        interfaceModel.decrementSelectedImageIndex()
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
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
                        if (axial.isNotEmpty()) {
                            Image(
                                bitmap = axial[selectedImageIndex].toComposeImageBitmap(),
                                contentDescription = "Selected Axial Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("No Axial Images")
                        }
                    }


//                Box(
//                    modifier = Modifier.fillMaxWidth().height(200.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Image(painterResource(Res.drawable.compose_multiplatform), null)
//                }

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
            }

        },
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
                }


            }

        }

    )
}


//@Composable
//fun displayImg(images: List<BufferedImage>) {
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .pointerInput(Unit) {
//                awaitPointerEventScope {
//                    while (true) {
//                        val event = awaitPointerEvent()
//                        if (event.type == PointerEventType.Scroll) {
//                            // Here we assume the scroll delta is available on the first change.
//                            val scrollDelta = event.changes.firstOrNull()?.scrollDelta ?: Offset.Zero
//                            if (scrollDelta.y > 0f) {
//                                selectedIndex = (selectedIndex + 1).coerceAtMost(images.lastIndex-1)
//                            } else if (scrollDelta.y < 0f) {
//                                selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
//                            }
//                        }
//                    }
//                }
//            },
//        contentAlignment = Alignment.Center
//    ) {
//        if (images.isNotEmpty()) {
//            Image(
//                bitmap = images[selectedIndex].toComposeImageBitmap(),
//                contentDescription = "Selected Axial Image",
//                modifier = Modifier.fillMaxSize()
//            )
//        } else {
//            Text("No Axial Images")
//        }
//    }
//}



