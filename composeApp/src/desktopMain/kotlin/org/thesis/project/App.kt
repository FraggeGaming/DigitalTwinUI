package org.thesis.project

import CardWithNestedCheckboxes
import CardWithCheckboxes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role.Companion.RadioButton
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import imaging.composeapp.generated.resources.Res
import imaging.composeapp.generated.resources.compose_multiplatform
import menuCard

@Composable
@Preview
fun App() {
    MaterialTheme {
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
                        val nestedData = listOf(
                            "CT_Patient1" to listOf("PET_Patient1", "MRI_Patient1"),
                            "CT_Patient2" to listOf("PET_Patient2", "MRI_Patient2"),
                        )

                        CardWithNestedCheckboxes(
                            items = nestedData,
                            onCheckboxChanged = { label, isChecked ->
                                println("$label is ${if (isChecked) "selected" else "deselected"}")
                            }
                        )

                        val organs = listOf("Liver", "Heart", "Lung", "Kidney", "Brain")

                        CardWithCheckboxes(
                            items = organs,
                            onCheckboxChanged = { organ, isChecked ->
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
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center) {
                        Text("Center Panel")
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
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { checked = it }
                                    )
                                    Text(text = "Axial")
                                },

                                { var checked by remember { mutableStateOf(false) }
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { checked = it }
                                    )
                                    Text(text = "Coronal")
                                },

                                { var checked by remember { mutableStateOf(false) }

                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { checked = it }
                                    )
                                    Text(text = "Sagittal")
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