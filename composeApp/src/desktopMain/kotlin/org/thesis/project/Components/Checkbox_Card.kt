import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign



@Composable
fun CardWithCheckboxes(
    items: List<String>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkStates = remember { mutableStateMapOf<String, Boolean>().apply {
        items.forEach { this[it] = false }
    } }

    Card(
        modifier = modifier,
        elevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Row{
                    Text(text = "Select Districts")
                }
                //Align in a 2 column grid
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { label ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = checkStates[label] ?: false,
                                    onCheckedChange = { isChecked ->
                                        checkStates[label] = isChecked
                                        onCheckboxChanged(label, isChecked)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = label)
                            }
                        }

                       //So that the last checkbox (if odd number) does not gets centered
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CardWithNestedCheckboxes(
    items: List<Pair<String, List<String>>>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkStates = remember { mutableStateMapOf<String, Boolean>().apply {
        items.forEach { (main, subs) ->
            this[main] = false
            subs.forEach { this[it] = false }
        }
    } }

    Card(
        modifier = modifier,
        elevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
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
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                Checkbox(
                                    checked = checkStates[mainLabel] ?: false,
                                    onCheckedChange = { isChecked ->
                                        checkStates[mainLabel] = isChecked
                                        onCheckboxChanged(mainLabel, isChecked)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = mainLabel)
                            }

                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            subLabels.forEach { subLabel ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
        }
    }
}



