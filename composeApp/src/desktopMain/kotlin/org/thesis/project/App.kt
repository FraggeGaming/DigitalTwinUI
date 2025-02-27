package org.thesis.project

import CardMenu
import CardWithCheckboxes
import CardWithNestedCheckboxes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import imaging.composeapp.generated.resources.Res
import imaging.composeapp.generated.resources.compose_multiplatform
import menuCard

class FakeViewModel {
    val nestedData: List<Pair<String, List<String>>> = listOf(
        "CT_Patient1" to listOf("PET_Patient1", "MRI_Patient1"),
        "CT_Patient2" to listOf("PET_Patient2", "MRI_Patient2")
    )
    val organs: List<String> = listOf("Liver", "Heart", "Lung", "Kidney", "Brain")

    var selectedDistricts: Set<String> by mutableStateOf(setOf())
    var selectedData: Set<String> by mutableStateOf(setOf())
    var selectedSettings: Set<String> by mutableStateOf(setOf())

    fun updateSelectedData(label: String, isSelected: Boolean) {
        selectedData = if (isSelected) {
            selectedData + label
        } else {
            selectedData - label
        }
    }

    fun updateSelectedSettings(label: String, isSelected: Boolean) {
        selectedSettings = if (isSelected) {
            selectedSettings + label
        } else {
            selectedSettings - label
        }
    }

    fun updateSelectedDistrict(label: String, isSelected: Boolean) {
        selectedDistricts = if (isSelected) {
            selectedDistricts + label
        } else {
            selectedDistricts - label
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val model = FakeViewModel()
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

                        CardMenu(
                            //onShowPopup = { showPopup = !showPopup },
                            model.selectedData,
                            items = model.nestedData,
                            onCheckboxChanged = { label, isChecked ->
                                model.updateSelectedData(label, isChecked)

                                println("$label is ${if (isChecked) "selected" else "deselected"}")
                            }
                        )

                        CardWithCheckboxes(
                            model.selectedDistricts,
                            items = model.organs,
                            onCheckboxChanged = { organ, isChecked ->
                                model.updateSelectedDistrict(organ, isChecked)
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
                        model.selectedData.forEach { selected ->
                            Text(text = selected)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    )
                    {
                        model.selectedDistricts.forEach { selected ->
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
}