import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.thesis.project.Components.LocalAppColors
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
            Text(text = "Select Districts",  color = LocalAppColors.current.primaryBlue,)

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
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable {
                val isCurrentlyChecked = selectedData.contains(label)
                onCheckboxChanged(label, !isCurrentlyChecked)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {


        Checkbox(
            checked = selectedData.contains(label),
            onCheckedChange = { isChecked ->
                onCheckboxChanged(label, isChecked)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = LocalAppColors.current.primaryBlue,
                uncheckedColor = LocalAppColors.current.primaryBlue,
                checkmarkColor = Color.White
            )
        )

        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = Color.Black)
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
                containerColor  = Color.Transparent,
                contentColor = Color.White,
            ),
            onClick = {
                val isCurrentlyChecked = selectedData.contains(label)
                onCheckboxChanged(label, !isCurrentlyChecked)
            },
            shape = RoundedCornerShape(0.dp)
        ) {

            Checkbox(
                checked = selectedData.contains(label),
                onCheckedChange = { isChecked ->
                    onCheckboxChanged(label, isChecked)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = Color.White,
                    checkmarkColor = LocalAppColors.current.primaryBlue
                )
            )

            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label.displayName, color = Color.White)
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
            .wrapContentSize(Alignment.Center)
            .clip(shape)
            .background(LocalAppColors.current.thirdlyBlue)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Input",
            color = Color.Black,

        )

        mainLabels.forEach { mainLabel ->
            buttonWithCheckbox(selectedData, mainLabel, onCheckboxChanged)
        }

        Text(
            text = "Synthetic output",
            color = Color.Black,
        )
        subLabels.forEach { subLabel ->
            buttonWithCheckbox(selectedData, subLabel, onCheckboxChanged)
        }
    }
}


@Composable
fun cardMenu(
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
                        color = LocalAppColors.current.primaryBlue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (boxMaxWidth < 200.dp) {
                        // Vertical list of menu items
//                        Column(
//                            verticalArrangement = Arrangement.spacedBy(8.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
                        fileKeys.forEach { mainLabel ->
                            val isSelected = expandedMenu == mainLabel
                            Column(
                                modifier = Modifier.wrapContentSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                menuButton(
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
                                            selectedData = selectedData,
                                            mainLabels = inputList,
                                            subLabels = outputList,
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
//                            }
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
                                    menuButton(
                                        mainLabel = mainLabel,
                                        isSelected = false,
                                        onClick = { expandedMenu = mainLabel },
                                        widthFraction = 1f,
                                        shapeSelected = RoundedCornerShape(4.dp)
                                    )
                                }
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.weight(0.4f).wrapContentSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    fileKeys.forEach { mainLabel ->
                                        val isSelected = expandedMenu == mainLabel
                                        menuButton(
                                            mainLabel = mainLabel,
                                            isSelected = isSelected,
                                            onClick = { expandedMenu = if (isSelected) null else mainLabel },
                                            widthFraction = if (isSelected) 1f else 0.8f,
                                            shapeSelected = RoundedCornerShape(
                                                topStart = 4.dp,
                                                bottomStart = 4.dp,
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
                                                selectedData = selectedData,
                                                mainLabels = inputList,
                                                subLabels = outputList,
                                                onCheckboxChanged = onCheckboxChanged,
                                                shape = RoundedCornerShape(
                                                    topStart = 0.dp,
                                                    bottomStart = 4.dp,
                                                    topEnd = 0.dp,
                                                    bottomEnd = 4.dp
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
fun menuButton(
    mainLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    widthFraction: Float,
    shapeSelected: Shape
) {
    Row(
        modifier = Modifier
            .fillMaxSize().height(40.dp)
            .background(
                color = LocalAppColors.current.thirdlyBlue,
                shape = if (isSelected) {
                    shapeSelected
                } else {
                    RoundedCornerShape(4.dp)
                }
            ).clickable {onClick()}
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = mainLabel,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun activeSelected(
    selectedData: Set<String>,
    onCheckboxChanged: (String, Boolean) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        println(selectedData)
        selectedData.toList().forEach { button ->
            if (button.isNotEmpty()) {
                selectedButtonRemove(
                    modifier = Modifier,
                    text = button,
                    onclick = {
                        val isCurrentlyChecked = selectedData.contains(button)
                        onCheckboxChanged(button, !isCurrentlyChecked)
                    }
                )
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
            containerColor  = LocalAppColors.current.thirdlyBlue,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .size(100.dp, 50.dp)
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text
            )

            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Exit",
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}







