package org.thesis.project.Components

import activeSelected
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import modalities

@Composable
fun CardWithNestedCheckboxes(
    selectedData: Set<String>,
    items: List<Pair<String, List<String>>>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkStates = remember(items, selectedData) {
        mutableStateMapOf<String, Boolean>().apply {
            items.forEach { (main, subs) ->
                this[main] = selectedData.contains(main)
                subs.forEach { sub ->
                    this[sub] = selectedData.contains(sub)
                }
            }
        }
    }


    Card(
        modifier = modifier,
        elevation = 8.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            if (maxWidth > 300.dp) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)//.border(1.dp, Color.Red)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Input",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )

                        Text(
                            text = "Output",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                    items.forEach { (mainLabel, subLabels) ->

                        Row(
                            horizontalArrangement = Arrangement.Start,

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checkStates[mainLabel] ?: false,
                                    onCheckedChange = { isChecked ->
                                        checkStates[mainLabel] = isChecked
                                        onCheckboxChanged(mainLabel, isChecked)
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = mainLabel)
                            }



                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                subLabels.forEach { subLabel ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start,
                                    ) {
                                        Checkbox(
                                            checked = checkStates[subLabel] ?: false,
                                            onCheckedChange = { isChecked ->
                                                checkStates[subLabel] = isChecked
                                                onCheckboxChanged(subLabel, isChecked)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = subLabel)


                                    }
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp).border(1.dp, Color.Red)
                ) {


                    items.forEach { (mainLabel, subLabels) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                text = "Input",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),//.border(1.dp, Color.Blue),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {

                            Checkbox(
                                checked = checkStates[mainLabel] ?: false,
                                onCheckedChange = { isChecked ->
                                    checkStates[mainLabel] = isChecked
                                    onCheckboxChanged(mainLabel, isChecked)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = mainLabel)

                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = "Output",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                        }
                        subLabels.forEach { subLabel ->
                            Row(
                                modifier = Modifier.border(1.dp, Color.Yellow).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                Checkbox(
                                    checked = checkStates[subLabel] ?: false,
                                    onCheckedChange = { isChecked ->
                                        checkStates[subLabel] = isChecked
                                        onCheckboxChanged(subLabel, isChecked)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = subLabel)
                            }
                        }


                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                    }
                }
            }
        }
    }
}


@Composable
fun CardMenu1(
    selectedData: Set<String>,
    items: List<Pair<String, List<String>>>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {

    Card(
        modifier = modifier,
        elevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)//.border(1.dp, Color.Red)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Input",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Adds spacing between buttons
                ) {

                    items.forEach { (mainLabel, subLabels) ->
                        var isMenuExpanded by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp), // Optional padding between items
                            verticalAlignment = Alignment.CenterVertically, // Align content vertically
                            horizontalArrangement = Arrangement.Start // Align content horizontally
                        ) {
                            // Button
                            TextButton(
                                onClick = { isMenuExpanded = true },
                                modifier = Modifier.weight(1f) // Adjusts button size for flexible layout
                            ) {
                                Text(
                                    text = mainLabel.substringAfter("_"),
                                    textAlign = TextAlign.Center
                                )
                            }


                            Box() {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Input",
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    Checkbox(
                                        checked = selectedData.contains(mainLabel),
                                        onCheckedChange = { isChecked ->
                                            onCheckboxChanged(mainLabel, isChecked)
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = mainLabel)
                                }

                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    color = Color.Gray,
                                    thickness = 1.dp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "Synthetic output",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                            subLabels.forEach { subLabel ->
                                TextButton(onClick = {
                                    val isCurrentlyChecked = selectedData.contains(subLabel)
                                    onCheckboxChanged(subLabel, !isCurrentlyChecked)

                                    //isMenuExpanded = false
                                }) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start,
                                    ) {
                                        Checkbox(
                                            checked = selectedData.contains(subLabel),
                                            onCheckedChange = { isChecked ->
                                                onCheckboxChanged(subLabel, isChecked)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = subLabel)


                                    }
                                }
                            }


                            //Dropdown Menu for each item
//                            DropdownMenu(
//                                expanded = isMenuExpanded,
//                                onDismissRequest = { isMenuExpanded = false },
//                            ) {
//
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.Center
//                                ) {
//                                    Text(
//                                        text = "Input",
//                                        textAlign = TextAlign.Center
//                                    )
//                                }
//                                DropdownMenuItem(onClick = {
//                                    // Handle submenu item click
//                                    val isCurrentlyChecked = selectedData.contains(mainLabel)
//                                    // Toggle the checkbox state
//                                    onCheckboxChanged(mainLabel, !isCurrentlyChecked)
//
//                                    //isMenuExpanded = false
//                                }) {
//
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        horizontalArrangement = Arrangement.Start,
//                                    ) {
//                                        Checkbox(
//                                            checked = selectedData.contains(mainLabel),
//                                            onCheckedChange = { isChecked ->
//                                                onCheckboxChanged(mainLabel, isChecked)
//                                            }
//                                        )
//                                        Spacer(modifier = Modifier.height(4.dp))
//                                        Text(text = mainLabel)
//                                    }
//
//                                }
//
//                                Divider(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 8.dp),
//                                    color = Color.Gray,
//                                    thickness = 1.dp
//                                )
//
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.Center,
//                                ) {
//                                    Text(
//                                        text = "Synthetic output",
//                                        modifier = Modifier.weight(1f),
//                                        textAlign = TextAlign.Center
//                                    )
//                                }
//                                subLabels.forEach { subLabel ->
//                                    DropdownMenuItem(onClick = {
//                                        val isCurrentlyChecked = selectedData.contains(subLabel)
//                                        onCheckboxChanged(subLabel, !isCurrentlyChecked)
//
//                                        //isMenuExpanded = false
//                                    }) {
//                                        Row(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.Start,
//                                        ) {
//                                            Checkbox(
//                                                checked = selectedData.contains(subLabel),
//                                                onCheckedChange = { isChecked ->
//                                                    onCheckboxChanged(subLabel, isChecked)
//                                                }
//                                            )
//                                            Spacer(modifier = Modifier.width(4.dp))
//                                            Text(text = subLabel)
//
//
//                                        }
//                                    }
//                                }
//                            }

                        }
                    }
                    val rows = selectedData.toList().chunked(2)
                    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { label ->
                                    TextButton(
                                        onClick = {
                                            val isCurrentlyChecked = selectedData.contains(label)
                                            onCheckboxChanged(label, !isCurrentlyChecked)
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            backgroundColor = Color.LightGray,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = label)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Exit"
                                        )
                                    }
                                }
                                // If the row has only one element, add a Spacer to take up the remaining space.
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardMenu2(
    selectedData: Set<String>,
    items: List<Pair<String, List<String>>>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = 8.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Input",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            var expandedMenu by remember { mutableStateOf<String?>(null) }

            items.forEach { (mainLabel, subLabels) ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    TextButton(
                        onClick = { expandedMenu = if (expandedMenu == mainLabel) null else mainLabel },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = mainLabel.substringAfter("_"),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (expandedMenu == mainLabel) {

                        modalities(
                            selectedData = selectedData,
                            mainLabel = mainLabel,
                            subLabels = subLabels,
                            onCheckboxChanged = onCheckboxChanged
                        )
                    }
                }
            }


            activeSelected(
                selectedData = selectedData,
                onCheckboxChanged = onCheckboxChanged
            )


        }
    }
}