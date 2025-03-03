import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip


@Composable
fun CardWithCheckboxes(
    selectedData: Set<String>,
    items: List<String>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

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
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row {
                        Text(text = "Select Districts")
                    }
                    //Align in a 2 column grid
                    items.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            //.border(1.dp, Color.Red)
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,

                            ) {
                            rowItems.forEach { label ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier.weight(1f)
                                        .clickable {
                                            val isCurrentlyChecked = selectedData.contains(label)
                                            // Toggle the checkbox state
                                            onCheckboxChanged(label, !isCurrentlyChecked)
                                        }
                                    //.border(1.dp, Color.Blue)
                                ) {
                                    Checkbox(
                                        checked = selectedData.contains(label),
                                        onCheckedChange = { isChecked ->
                                            onCheckboxChanged(label, isChecked)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = label)
                                }
                            }

                            //So that the last checkbox (if odd number) does not get centered
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)//.border(1.dp, Color.Red)
                ) {
                    Row {
                        Text(text = "Select Districts")
                    }
                    //Align in a 2 column grid

                    items.forEach { label ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),//.border(1.dp, Color.Blue),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Checkbox(
                                checked = selectedData.contains(label),
                                onCheckedChange = { isChecked ->
                                    onCheckboxChanged(label, isChecked)
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = label)
                        }
                    }
                }
            }

        }
    }
}

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
fun modalities(selectedData: Set<String>, mainLabel: String, subLabels: List<String>, onCheckboxChanged: (String, Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)

            .padding(8.dp),

        ) {
        Text(
            text = "Input",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {

            TextButton(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = LocalContentColor.current
                ),
                onClick = {
                val isCurrentlyChecked = selectedData.contains(mainLabel)
                onCheckboxChanged(mainLabel, !isCurrentlyChecked)

                //isMenuExpanded = false
            }) {
                Checkbox(
                    checked = selectedData.contains(mainLabel),
                    onCheckedChange = { isChecked ->
                        onCheckboxChanged(mainLabel, isChecked)
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = mainLabel)
            }

        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Synthetic output",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )
        subLabels.forEach { subLabel ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = LocalContentColor.current
                    ),
                    onClick = {
                    val isCurrentlyChecked = selectedData.contains(subLabel)
                    onCheckboxChanged(subLabel, !isCurrentlyChecked)

                }) {
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
    }
}

@Composable
fun CardMenu(
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

            items.forEach { (mainLabel, subLabels) ->
                var isMenuExpanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    TextButton(
                        onClick = { isMenuExpanded = !isMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = mainLabel.substringAfter("_"),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (isMenuExpanded) {

                        modalities(
                            selectedData = selectedData,
                            mainLabel = mainLabel,
                            subLabels = subLabels,
                            onCheckboxChanged = onCheckboxChanged)
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

@Composable
fun activeSelected(
    selectedData: Set<String>,
    onCheckboxChanged: (String, Boolean) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Dynamically determine the chunk size based on available width
        val chunkSize = if (maxWidth < 300.dp) 1 else 2 // Use 1 if width < 300.dp, otherwise 2

        val rows = selectedData.toList().chunked(chunkSize) // Dynamically chunk the data

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { label ->
                        selectedButtonRemove(
                            modifier = Modifier,
                            text = label,
                            onclick = {
                                val isCurrentlyChecked = selectedData.contains(label)
                                onCheckboxChanged(label, !isCurrentlyChecked)
                            }
                        )
                    }
                    // Add Spacer if the current row doesn't fill up the entire row
                    if (rowItems.size < chunkSize) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}





@Composable
fun selectedButtonRemove(modifier: Modifier = Modifier, text: String, onclick: () -> Unit) {
    TextButton(
        onClick = {
            onclick()
        },
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = Color.LightGray,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
        // Constrain the button size
        modifier = Modifier
            .size(150.dp, 50.dp) // Set the width and height of the button
            .then(modifier) // Use any additional modifiers passed externally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth() // Ensure contents are properly placed
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f), // Allow Text and Icon to share space proportionally
                textAlign = TextAlign.Start // Text remains visible always
            )
            Spacer(modifier = Modifier.width(8.dp)) // Space between Text and Icon
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Exit",
                modifier = Modifier
                    .size(24.dp) // Fixed Icon size
            )
        }
    }
}



@Composable
fun menuCard(
    modifier: Modifier = Modifier,
    content: List<@Composable () -> Unit>
) {

    Card(
        modifier = modifier,
        elevation = 8.dp,

        ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically

        ) {
            content.forEach { composable ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        composable()
                    }

                }

            }

        }
    }
}




