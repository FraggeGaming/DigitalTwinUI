import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.thesis.project.Components.standardCard
import org.thesis.project.Model.NiftiView


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardWithCheckboxes(
    selectedData: Set<String>,
    items: List<String>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    standardCard(
        modifier = modifier,
        contentAlignment = Alignment.CenterHorizontally,
        content = {
            Text(text = "Select Districts")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { label ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                val isChecked = selectedData.contains(label)
                                onCheckboxChanged(label, !isChecked)
                            }
                    ) {
                        Checkbox(
                            checked = selectedData.contains(label),
                            onCheckedChange = { isChecked ->
                                onCheckboxChanged(label, isChecked)
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label)
                    }
                }
            }
        }
    )
}

@Composable
fun buttonWithCheckbox(
    selectedData: Set<String>,
    label: String,
    onCheckboxChanged: (String, Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(
            //modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color.Transparent,
                contentColor = LocalContentColor.current
            ),
            onClick = {
                val isCurrentlyChecked = selectedData.contains(label)
                onCheckboxChanged(label, !isCurrentlyChecked)
            }
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

@Composable
fun buttonWithCheckboxSet(
    selectedData: Set<NiftiView>,
    label: NiftiView,
    onCheckboxChanged: (NiftiView, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(
            modifier = modifier,
            //modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
            ),
            onClick = {
                val isCurrentlyChecked = selectedData.contains(label)
                onCheckboxChanged(label, !isCurrentlyChecked)
            }
        ) {
            Checkbox(
                checked = selectedData.contains(label),
                onCheckedChange = { isChecked ->
                    onCheckboxChanged(label, isChecked)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = Color.White,
                    checkmarkColor = Color(0xFF0050A0)
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label.displayName)
        }
    }
}

@Composable
fun modalities(
    selectedData: Set<String>,
    mainLabels: List<String>,
    subLabels: List<String>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.LightGray)
            .padding(8.dp)
    ) {
        Text(
            text = "Input",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

        mainLabels.forEach { mainLabel ->
            buttonWithCheckbox(selectedData, mainLabel, onCheckboxChanged)

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
            buttonWithCheckbox(selectedData, subLabel, onCheckboxChanged)
        }
    }
}


@Composable
fun CardMenu(
    selectedData: Set<String>,
    fileKeys: List<String>,
    getFileMapping: (String) -> Pair<List<String>, List<String>>?,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedMenu by remember { mutableStateOf<String?>(null) }

    standardCard(
        modifier = modifier,
        content = {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val boxMaxWidth = maxWidth

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select files to view",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (boxMaxWidth < 300.dp) {
                        // Vertical list of menu items
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            fileKeys.forEach { mainLabel ->
                                val isSelected = expandedMenu == mainLabel
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    MenuButton(
                                        mainLabel = mainLabel,
                                        isSelected = isSelected,
                                        onClick = { expandedMenu = if (isSelected) null else mainLabel },
                                        widthFraction = 1f,
                                        shapeSelected = RoundedCornerShape(
                                            topStart = 8.dp,
                                            bottomStart = 0.dp,
                                            topEnd = 8.dp,
                                            bottomEnd = 0.dp
                                        )
                                    )
                                    if (isSelected) {
                                        getFileMapping(mainLabel)?.let { (inputList, outputList) ->

                                            modalities(
                                                selectedData = selectedData, // ✅ Inputs are now correctly mapped
                                                mainLabels = inputList,
                                                subLabels = outputList, // ✅ Outputs go into modalities
                                                onCheckboxChanged = onCheckboxChanged,
                                                shape = RoundedCornerShape(
                                                    topStart = 0.dp,
                                                    bottomStart = 8.dp,
                                                    topEnd = 0.dp,
                                                    bottomEnd = 8.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Wide layout
                        if (expandedMenu == null) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                fileKeys.forEach { mainLabel ->
                                    MenuButton(
                                        mainLabel = mainLabel,
                                        isSelected = false,
                                        onClick = { expandedMenu = mainLabel },
                                        widthFraction = 1f,
                                        shapeSelected = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.weight(0.4f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    fileKeys.forEach { mainLabel ->
                                        val isSelected = expandedMenu == mainLabel
                                        MenuButton(
                                            mainLabel = mainLabel,
                                            isSelected = isSelected,
                                            onClick = { expandedMenu = if (isSelected) null else mainLabel },
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
                                    modifier = Modifier.weight(0.6f),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    expandedMenu?.let { selectedMainLabel ->
                                        getFileMapping(selectedMainLabel)?.let { (inputList, outputList) ->
                                            modalities(
                                                selectedData = selectedData, // ✅ Inputs are now correctly mapped
                                                mainLabels = inputList,
                                                subLabels = outputList, // ✅ Outputs go into modalities
                                                onCheckboxChanged = onCheckboxChanged,
                                                shape = RoundedCornerShape(
                                                    topStart = 0.dp,
                                                    bottomStart = 8.dp,
                                                    topEnd = 0.dp,
                                                    bottomEnd = 8.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    activeSelected(
                        selectedData = selectedData,
                        onCheckboxChanged = onCheckboxChanged,
                    )
                }
            }
        }
    )

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
                shape = if (isSelected) {
                    shapeSelected
                } else {
                    RoundedCornerShape(8.dp)
                }
            )
    ) {
        Text(
            text = mainLabel,
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
        val chunkSize = if (maxWidth < 300.dp) 1 else 2 // Use 1 if width < 300.dp, otherwise 2

        val rows = selectedData.toList().chunked(chunkSize)

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
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rowItems.isNotEmpty()) {
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
        modifier = Modifier
            .size(150.dp, 50.dp)
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Exit",
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}







