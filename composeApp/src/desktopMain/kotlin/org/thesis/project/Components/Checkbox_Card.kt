import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .then(
                if (isSelected) Modifier.border(
                    width = 3.dp,
                    color = Color.Blue,
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .background(Color.Green, RoundedCornerShape(8.dp))
            .clickable {
                onCheckboxChanged(id, !isSelected)
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.Black
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
fun modalities(
    selectedData: Set<String>,
    mainLabels: List<String>,
    subLabels: List<String>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    shape: Shape = RoundedCornerShape(8.dp),
    interfaceModel: InterfaceModel,
    mainLabel: String
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
            color = Color.Black
        )

        mainLabels.forEach { mainLabelId ->
            val name = interfaceModel.niftiRepo.getNameFromNiftiId(mainLabelId)
            //println(name)
            interfaceModel.niftiRepo.removeFileMapping(mainLabelId)
            buttonWithCheckbox(
                selectedData,
                mainLabelId ,
                name,
                onCheckboxChanged)
        }

        Text(
            text = "Synthetic output",
            color = Color.Black,
        )
        subLabels.forEach { subLabelId ->
            val name = interfaceModel.niftiRepo.getNameFromNiftiId(subLabelId)

            buttonWithCheckbox(selectedData, subLabelId,name,  onCheckboxChanged)
        }

        if (subLabels.isNotEmpty()){
            TextButton(
                onClick = {
                    //save nifti to user choose of folder
                    val path = interfaceModel.niftiRepo.get(subLabels.first())?.gz_path

                    if (path != null) {
                        val sourceFile = File(path)

                        //Create a native Save dialog
                        val dialog = FileDialog(null as Frame?, "Save NIfTI File", FileDialog.SAVE)
                        dialog.file = sourceFile.name // suggest filename
                        dialog.isVisible = true

                        if (dialog.file != null && dialog.directory != null) {
                            val chosenFile = File(dialog.directory, dialog.file)
                            //Copy file to chosen location
                            sourceFile.copyTo(chosenFile, overwrite = true)
                            //println("File saved to: ${chosenFile.absolutePath}")
                        } else {
                            //println("User canceled the save dialog.")
                        }
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor  = LocalAppColors.current.thirdlyBlue,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxSize()

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Download synthetic nifti")

                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Download synthetic nifti",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
        }

    }
}




@Composable
fun modalitiesTest(
    selectedData: Set<String>,
    mainLabels: List<String>,
    subLabels: List<String>,
    onCheckboxChanged: (String, Boolean) -> Unit,
    shape: Shape = RoundedCornerShape(8.dp),
    interfaceModel: InterfaceModel,
    mainLabel: String
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
            color = Color.Black
        )

        mainLabels.forEach { mainLabelId ->
            val name = interfaceModel.niftiRepo.getNameFromNiftiId(mainLabelId)
            //println(name)
            interfaceModel.niftiRepo.removeFileMapping(mainLabelId)
            buttonWithCheckbox(
                selectedData,
                mainLabelId ,
                name,
                onCheckboxChanged)
        }

        Text(
            text = "Synthetic output",
            color = Color.Black,
        )
        subLabels.forEach { subLabelId ->
            val name = interfaceModel.niftiRepo.getNameFromNiftiId(subLabelId)

            buttonWithCheckbox(selectedData, subLabelId,name,  onCheckboxChanged)
        }

        if (subLabels.isNotEmpty()){
            TextButton(
                onClick = {
                    //save nifti to user choose of folder
                    val path = interfaceModel.niftiRepo.get(subLabels.first())?.gz_path

                    if (path != null) {
                        val sourceFile = File(path)

                        //Create a native Save dialog
                        val dialog = FileDialog(null as Frame?, "Save NIfTI File", FileDialog.SAVE)
                        dialog.file = sourceFile.name // suggest filename
                        dialog.isVisible = true

                        if (dialog.file != null && dialog.directory != null) {
                            val chosenFile = File(dialog.directory, dialog.file)
                            //Copy file to chosen location
                            sourceFile.copyTo(chosenFile, overwrite = true)
                            //println("File saved to: ${chosenFile.absolutePath}")
                        } else {
                            //println("User canceled the save dialog.")
                        }
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor  = LocalAppColors.current.thirdlyBlue,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxSize()

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Download synthetic nifti")

                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Download synthetic nifti",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
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
    interfaceModel: InterfaceModel
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

                                if (expandedMenu == null){
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            menuButton(
                                                mainLabel = mainLabel,
                                                isSelected = false,
                                                onClick = { expandedMenu = mainLabel },
                                                widthFraction = 1f,
                                                shapeSelected = RoundedCornerShape(4.dp)
                                            )
                                        }

                                        // 2. Trash Icon
                                        IconButton(
                                            onClick = { interfaceModel.niftiRepo.removeFileMapping(mainLabel) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                                else{
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
                                }


                                if (isSelected) {
                                    getFileMapping(mainLabel)?.let { (inputList, outputList) ->
                                        //println(inputList)
                                        //println(outputList)
                                        //println("selected Data: $selectedData")
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
                                            ),
                                            interfaceModel,
                                            mainLabel
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

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            menuButton(
                                                mainLabel = mainLabel,
                                                isSelected = false,
                                                onClick = { expandedMenu = mainLabel },
                                                widthFraction = 1f, // This can still stay
                                                shapeSelected = RoundedCornerShape(4.dp)
                                            )
                                        }

                                        // 2. Trash Icon
                                        IconButton(
                                            onClick = { interfaceModel.niftiRepo.removeFileMapping(mainLabel) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = Color.Red
                                            )
                                        }
                                    }

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
                                                ),
                                                interfaceModel = interfaceModel,
                                                mainLabel = selectedMainLabel,

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
                        interfaceModel = interfaceModel,
                    )
                }
            }
        }
    )

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
    var expandedMenu by remember { mutableStateOf<String?>(null) }

    standardCard(
        modifier = modifier,
        content = {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Select files to view",
                        color = LocalAppColors.current.primaryBlue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                        fileKeys.forEach { mainLabel ->
                            val isSelected = expandedMenu == mainLabel
                            Column(
                                modifier = Modifier.wrapContentSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                if (expandedMenu == null){
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            menuButton2(
                                                mainLabel = mainLabel,
                                                isSelected = false,
                                                onClick = { expandedMenu = mainLabel },
                                                widthFraction = 1f,
                                                shapeSelected = RoundedCornerShape(4.dp)
                                            )
                                        }

                                        // 2. Trash Icon
                                        IconButton(
                                            onClick = { interfaceModel.niftiRepo.removeFileMapping(mainLabel) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                                else{
                                    menuButton2(
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
                                }


                                if (isSelected) {
                                    getFileMapping(mainLabel)?.let { (inputList, outputList) ->
                                        Column(
                                            modifier = Modifier
                                                .wrapContentSize(Alignment.Center)
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {

                                            inputList.forEach { mainLabelId ->
                                                val name = interfaceModel.niftiRepo.getNameFromNiftiId(mainLabelId)
                                                interfaceModel.niftiRepo.removeFileMapping(mainLabelId)
                                                buttonNifti(selectedData, mainLabelId , name, onCheckboxChanged)
                                            }

                                            outputList.forEach { subLabelId ->
                                                val name = interfaceModel.niftiRepo.getNameFromNiftiId(subLabelId)
                                                buttonNifti(selectedData, subLabelId,name,  onCheckboxChanged)
                                            }


                                                TextButton(
                                                    onClick = {
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
                                                            //temporary ZIP file
                                                            val tempZipFile = File.createTempFile("nifti_archive_", ".zip")

                                                            //Write files into  ZIP
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

                                                            //Open save dialog
                                                            val dialog = FileDialog(null as Frame?, "Save ZIP Archive", FileDialog.SAVE)
                                                            dialog.file = "${mainLabel}.zip" // suggest a default name
                                                            dialog.isVisible = true

                                                            //Save to chosen location
                                                            if (dialog.file != null && dialog.directory != null) {
                                                                val chosenFile = File(dialog.directory, dialog.file)
                                                                tempZipFile.copyTo(chosenFile, overwrite = true)
                                                                println("Zip file saved to: ${chosenFile.absolutePath}")
                                                            } else {
                                                                println("User canceled the save dialog.")
                                                            }

                                                            //Delete temp file after use
                                                            tempZipFile.deleteOnExit()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.textButtonColors(
                                                        containerColor  = LocalAppColors.current.thirdlyBlue,
                                                        contentColor = Color.Black
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxSize()

                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(text = "Download ZIP")

                                                        Icon(
                                                            imageVector = Icons.Filled.Download,
                                                            contentDescription = "Download patient ZIP",
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                        )
                                                    }
                                                }


                                        }
                                    }
                                }
                            }

                        }


                    activeSelected(
                        selectedData = selectedData,
                        onCheckboxChanged = onCheckboxChanged,
                        interfaceModel = interfaceModel,
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


@Composable
fun menuButton2(
    mainLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    widthFraction: Float,
    shapeSelected: Shape
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(widthFraction).height(40.dp)
            .clickable {onClick()}
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