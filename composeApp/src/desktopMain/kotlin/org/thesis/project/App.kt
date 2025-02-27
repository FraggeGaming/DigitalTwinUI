package org.thesis.project

import CardWithNestedCheckboxes
import CardWithCheckboxes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import imaging.composeapp.generated.resources.Res
import imaging.composeapp.generated.resources.compose_multiplatform

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
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,

                ) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center) {
                        Text("Center Panel")
                    }


                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painterResource(Res.drawable.compose_multiplatform), null)
                    }
                }


            },
            rightContent = {
                Text("Right Panel")
            }

        )
    }
}