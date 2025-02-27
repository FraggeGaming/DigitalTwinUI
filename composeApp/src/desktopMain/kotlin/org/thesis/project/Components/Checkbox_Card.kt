import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot


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
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (maxWidth > 300.dp){
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row{
                        Text(text = "Select Districts")
                    }
                    //Align in a 2 column grid
                    items.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                            //.border(1.dp, Color.Red)
                            ,
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,

                            ) {
                            rowItems.forEach { label ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier.weight(1f)
                                    //.border(1.dp, Color.Blue)
                                ) {
                                    Checkbox(
                                        checked = checkStates[label] ?: false,
                                        onCheckedChange = { isChecked ->
                                            checkStates[label] = isChecked
                                            onCheckboxChanged(label, isChecked)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
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
            else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp).border(1.dp, Color.Red)
                ) {
                    Row{
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
                                checked = checkStates[label] ?: false,
                                onCheckedChange = { isChecked ->
                                    checkStates[label] = isChecked
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

//    var canvasGlobalOffset by remember { mutableStateOf(Offset.Zero) }
//    var mainRowRightCenter by remember { mutableStateOf(Offset.Zero) }
//    var subLabelRowLeftCenter by remember { mutableStateOf(Offset.Zero) }
//
//    val connections = remember { mutableStateListOf<Connection>() }

    Card(
        modifier = modifier,
        elevation = 8.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),//.onGloballyPositioned { coords ->
//                // Capture the global offset of the root Box (which will be used for conversion)
//                canvasGlobalOffset = coords.positionInRoot()
//            },
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



//                    connections.clear()
                    items.forEach { (mainLabel, subLabels) ->

                        Row(
                            horizontalArrangement = Arrangement.Start,

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Row(
//                                modifier = Modifier.border(1.dp, Color.Yellow)
//                                    .onGloballyPositioned { coordinates ->
//                                        val pos = coordinates.positionInRoot()
//                                        val size = coordinates.size
//                                        // Right center of this row
//                                        mainRowRightCenter = Offset(pos.x + size.width, pos.y + size.height / 2f)
//                                    },
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
                                //modifier = Modifier.border(1.dp, Color.Black),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                subLabels.forEach { subLabel ->
                                    Row(
//                                       modifier = Modifier.onGloballyPositioned { coordinates ->
//                                           val pos = coordinates.positionInRoot()
//                                           val size = coordinates.size
//                                           // Left center of this row
//                                           subLabelRowLeftCenter = Offset(pos.x, pos.y + size.height / 2f)
//                                       },
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

//                                    connections.add(
//                                        Connection(
//                                            start = mainRowRightCenter - canvasGlobalOffset,
//                                            end   = subLabelRowLeftCenter - canvasGlobalOffset
//                                        )
//                                    )



                                }


                            }


                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                    }


                }


            }
            else {
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

    //MyCanvas(connections)
}

data class Connection(val start: Offset, val end: Offset)


@Composable
fun MyCanvas(connections: List<Connection>) {
    // Each time `connections` changes, the Canvas is recomposed and drawn from scratch.
    Canvas(modifier = Modifier.fillMaxSize()) {
        connections.forEach { connection ->
            val path = Path().apply {
                moveTo(connection.start.x, connection.start.y)
                quadraticBezierTo(
                    (connection.start.x + connection.end.x) / 2,
                    connection.start.y - 50, // control point (adjust as needed)
                    connection.end.x,
                    connection.end.y
                )
            }
            drawPath(path = path, color = Color.Black, style = Stroke(width = 4.dp.toPx()))
        }
    }
}
@Composable
fun menuCard(
    modifier: Modifier = Modifier,
    content: List<@Composable () -> Unit>
){

    Card(
        modifier = modifier,
        elevation = 8.dp,

    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically

        ){
            content.forEach { composable ->
                Column(  horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        composable()
                    }

                }

            }

        }
    }
}

@Composable
fun DrawTreePath(start: Offset, end: Offset) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path().apply {
            moveTo(start.x, start.y)
            // Example using a quadratic Bézier curve:
            quadraticBezierTo(
                (start.x + end.x) / 2, start.y - 50, // control point (adjust as needed)
                end.x, end.y
            )
        }
        drawPath(path, color = Color.Black, style = Stroke(width = 4.dp.toPx()))
    }
}

@Composable
fun TreeConnections() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Example positions; adjust these based on your layout
        val inputPos = Offset(100f, size.height / 2)
        val topOutputPos = Offset(300f, size.height / 3)
        val bottomOutputPos = Offset(300f, 2 * size.height / 3)

        // Draw curve from input to top output
        val topPath = Path().apply {
            moveTo(inputPos.x, inputPos.y)
            quadraticTo(
                (inputPos.x + topOutputPos.x) / 2, inputPos.y - 50,  // control point
                topOutputPos.x, topOutputPos.y
            )
        }
        drawPath(path = topPath, color = Color.Black, style = Stroke(width = 4.dp.toPx()))

        // Draw curve from input to bottom output
        val bottomPath = Path().apply {
            moveTo(inputPos.x, inputPos.y)
            quadraticTo(
                (inputPos.x + bottomOutputPos.x) / 2, inputPos.y + 50,  // control point
                bottomOutputPos.x, bottomOutputPos.y
            )
        }
        drawPath(path = bottomPath, color = Color.Black, style = Stroke(width = 4.dp.toPx()))
    }
}



