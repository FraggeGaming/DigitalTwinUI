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
import androidx.compose.ui.graphics.Shape


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
                            modifier = Modifier.fillMaxWidth().clickable {
                                val isCurrentlyChecked = selectedData.contains(label)
                                // Toggle the checkbox state
                                onCheckboxChanged(label, !isCurrentlyChecked)
                            },//.border(1.dp, Color.Blue),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,


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
                }
            }

        }
    }
}


@Composable
fun modalities(
    selectedData: Set<String>,
    mainLabel: String,
    subLabels: List<String>,
    onCheckboxChanged: (String, Boolean) -> Unit
) {
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
    //tracking which menu is expanded
    var expandedMenu by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier,
        elevation = 8.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val boxMaxWidth = maxWidth

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

                //show a vertical list
                if (boxMaxWidth < 300.dp) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items.forEach { (mainLabel, subLabels) ->
                            val isSelected = expandedMenu == mainLabel
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                MenuButton(
                                    mainLabel = mainLabel,
                                    isSelected = isSelected,
                                    onClick = {
                                        expandedMenu = if (isSelected) null else mainLabel
                                    },
                                    widthFraction = 1f,
                                    shapeSelected = RoundedCornerShape(8.dp)
                                )
                                if (isSelected) {
                                    modalities(
                                        selectedData = selectedData,
                                        mainLabel = mainLabel,
                                        subLabels = subLabels,
                                        onCheckboxChanged = onCheckboxChanged
                                    )
                                }
                            }
                        }
                    }
                } else {
                    //Wide layout
                    if (expandedMenu == null) {
                        //show all buttons in a vertical column.
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items.forEach { (mainLabel, _) ->
                                MenuButton(
                                    mainLabel = mainLabel,
                                    isSelected = false,
                                    onClick = { expandedMenu = mainLabel },
                                    widthFraction = 1f,
                                    shapeSelected = RoundedCornerShape(8.dp),
                                )
                            }
                        }
                    } else {
                        //show a two-column layout:
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(0.4f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                items.forEach { (mainLabel, _) ->
                                    val isSelected = expandedMenu == mainLabel
                                    MenuButton(
                                        mainLabel = mainLabel,
                                        isSelected = isSelected,
                                        onClick = { expandedMenu = if (isSelected) null else mainLabel },
                                        // When not selected, use a slightly smaller width.
                                        widthFraction = if (isSelected) 1f else 0.8f,
                                        shapeSelected = RoundedCornerShape(
                                            topStart = 8.dp,
                                            bottomStart = 8.dp,
                                            topEnd = 0.dp,
                                            bottomEnd = 0.dp
                                        )
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .weight(0.6f)
                                    .background(
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(
                                            topStart = 0.dp,
                                            bottomStart = 0.dp,
                                            topEnd = 8.dp,
                                            bottomEnd = 8.dp
                                        )
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                expandedMenu?.let { selectedMainLabel ->
                                    val selectedItem = items.find { it.first == selectedMainLabel }
                                    selectedItem?.let { (mainLabel, subLabels) ->
                                        modalities(
                                            selectedData = selectedData,
                                            mainLabel = mainLabel,
                                            subLabels = subLabels,
                                            onCheckboxChanged = onCheckboxChanged
                                        )
                                    }
                                }
                            }
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
}

@Composable
fun MenuButton(
    mainLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    widthFraction: Float,
    shapeSelected: Shape
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .background(
                color = Color.LightGray,
                shape = if (isSelected) { shapeSelected } else { RoundedCornerShape(8.dp) }
            )
    ) {
        Text(
            text = mainLabel.substringAfter("_"),
            textAlign = TextAlign.Center,
            color = Color.Black
        )
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




        if (chunkSize == 2) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // First column (always present)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rowItems.size >= 1) {
                                selectedButtonRemove(
                                    modifier = Modifier,
                                    text = rowItems[0],
                                    onclick = {
                                        val isCurrentlyChecked = selectedData.contains(rowItems[0])
                                        onCheckboxChanged(rowItems[0], !isCurrentlyChecked)
                                    }
                                )
                            }
                        }
                        // Second column (empty if not available)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rowItems.size >= 2) {
                                selectedButtonRemove(
                                    modifier = Modifier,
                                    text = rowItems[1],
                                    onclick = {
                                        val isCurrentlyChecked = selectedData.contains(rowItems[1])
                                        onCheckboxChanged(rowItems[1], !isCurrentlyChecked)
                                    }
                                )
                            }
                        }

                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),

                ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically

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




