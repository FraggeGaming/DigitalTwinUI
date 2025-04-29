import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.thesis.project.Components.LocalAppColors
import org.thesis.project.Components.standardCard
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Model.NiftiView
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream





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
    id: String,
    label: String,
    onCheckboxChanged: (String, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val isCurrentlyChecked = selectedData.contains(id)
                onCheckboxChanged(id, !isCurrentlyChecked)
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selectedData.contains(id),
                onCheckedChange = { isChecked ->
                    onCheckboxChanged(id, isChecked)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = LocalAppColors.current.primaryBlue,
                    uncheckedColor = LocalAppColors.current.primaryBlue,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                color = Color.Black
            )
        }


    }
}

@Composable
fun buttonNifti(
    selectedData: Set<String>,
    id: String,
    label: String,
    onCheckboxChanged: (String, Boolean) -> Unit,
) {
    val isSelected = selectedData.contains(id)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(
                    width = 3.dp,
                    color = LocalAppColors.current.primaryBlue,
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clip(RoundedCornerShape(8.dp))
            .background(LocalAppColors.current.thirdlyBlue)
            .clickable {
                onCheckboxChanged(id, !isSelected)
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.Black,
            style = MaterialTheme.typography.bodySmall
        )
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
fun cardMenu2(
    selectedData: Set<String>,
    fileKeys: List<String>,
    getFileMapping: (String) -> Pair<List<String>, List<String>>?,
    onCheckboxChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    interfaceModel: InterfaceModel
) {
    var expandedMenus by remember { mutableStateOf<Set<String>>(emptySet()) }
    standardCard(
        modifier = modifier,
        content = {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select files to view",
                        color = LocalAppColors.current.primaryBlue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,

                    )

                    fileKeys.forEachIndexed {index, mainLabel ->
                        val isSelected = expandedMenus.contains(mainLabel)
                        Column(
                            modifier = Modifier.wrapContentSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            expandedMenus = if (expandedMenus.contains(mainLabel)) {
                                                expandedMenus - mainLabel
                                            } else {
                                                expandedMenus + mainLabel
                                            }
                                        }
                                        .height(40.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Text(
                                            text = mainLabel,
                                            textAlign = TextAlign.Center,
                                            color = Color.Black,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (expandedMenus.contains(mainLabel)) "Collapse" else "Expand",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .graphicsLayer {
                                                    rotationZ = if (expandedMenus.contains(mainLabel)) 180f else 0f
                                                }
                                        )
                                    }
                                }

                                // Trash Icon
                                IconButton(
                                    onClick = {
                                        interfaceModel.modelRunner.cancelJob(mainLabel)
                                        interfaceModel.niftiRepo.removeFileMapping(mainLabel)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color.Red
                                    )
                                }
                            }

                            if (isSelected) {
                                getFileMapping(mainLabel)?.let { (inputList, outputList) ->


                                    inputList.forEach { mainLabelId ->
                                        val name = interfaceModel.niftiRepo.getNameFromNiftiId(mainLabelId)
                                        buttonNifti(selectedData, mainLabelId , name, onCheckboxChanged)
                                    }

                                    outputList.forEach { subLabelId ->
                                        val name = interfaceModel.niftiRepo.getNameFromNiftiId(subLabelId)
                                        buttonNifti(selectedData, subLabelId,name,  onCheckboxChanged)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 3.dp,
                                                color = LocalAppColors.current.primaryGray,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Transparent)
                                            .clickable {
                                                val paths = mutableListOf<String>()
                                                inputList.forEach { id ->
                                                    interfaceModel.niftiRepo.get(id)?.gz_path?.let { path ->
                                                        paths.add(path)
                                                    }
                                                }
                                                outputList.forEach { id ->
                                                    interfaceModel.niftiRepo.get(id)?.gz_path?.let { path ->
                                                        paths.add(path)
                                                    }
                                                }

                                                if (paths.isNotEmpty()) {
                                                    val tempZipFile = File.createTempFile("nifti_archive_", ".zip")

                                                    ZipOutputStream(tempZipFile.outputStream()).use { zipOut ->
                                                        paths.forEach { filePath ->
                                                            val file = File(filePath)
                                                            if (file.exists()) {
                                                                val entry = ZipEntry(file.name)
                                                                zipOut.putNextEntry(entry)
                                                                file.inputStream().use { input ->
                                                                    input.copyTo(zipOut)
                                                                }
                                                                zipOut.closeEntry()
                                                            }
                                                        }
                                                    }

                                                    val dialog = FileDialog(null as Frame?, "Save ZIP Archive", FileDialog.SAVE)
                                                    dialog.file = "${mainLabel}.zip"
                                                    dialog.isVisible = true

                                                    if (dialog.file != null && dialog.directory != null) {
                                                        val chosenFile = File(dialog.directory, dialog.file)
                                                        tempZipFile.copyTo(chosenFile, overwrite = true)
                                                        println("Zip file saved to: ${chosenFile.absolutePath}")
                                                    } else {
                                                        println("User canceled the save dialog.")
                                                    }

                                                    tempZipFile.deleteOnExit()
                                                }
                                            }
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Download ZIP",
                                                color = Color.Black,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.Download,
                                                contentDescription = "Download patient ZIP",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (index != fileKeys.lastIndex){
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp).padding(top = 8.dp),
                                    thickness = 1.dp,
                                    color = LocalAppColors.current.primaryDarkGray
                                )
                            }

                        }

                    }
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
            .fillMaxWidth(widthFraction).height(40.dp)
            .background(
                color = LocalAppColors.current.secondaryBlue,
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
    onCheckboxChanged: (String, Boolean) -> Unit,
    interfaceModel: InterfaceModel
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        //println(selectedData)
        selectedData.toList().forEach { button ->
            if (button.isNotEmpty()) {
                val name = interfaceModel.niftiRepo.getNameFromNiftiId(button)
                selectedButtonRemove(
                    modifier = Modifier,
                    text = name,
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
            .size(150.dp, 70.dp)
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