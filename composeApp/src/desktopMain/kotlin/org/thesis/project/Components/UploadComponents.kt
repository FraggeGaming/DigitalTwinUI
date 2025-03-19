package org.thesis.project.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.thesis.project.Model.InterfaceModel

import java.awt.FileDialog
import java.awt.Frame

fun selectFilesMultipleAWT(): List<String> {
    val dialog = FileDialog(null as Frame?, "Select Files", FileDialog.LOAD)
    dialog.isMultipleMode = true
    dialog.isVisible = true

    return dialog.files?.map { it.absolutePath } ?: emptyList()
}

@Composable
fun FileUploadComponent(
    interfaceModel: InterfaceModel
) {
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .border(2.dp, if (isDragging) Color.Blue else Color.Gray, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragCancel = { isDragging = false },
                    onDragEnd = { isDragging = false },
                    onDrag = { _, _ -> }
                )
            }
            .clickable {
                val selectedFiles = selectFilesMultipleAWT()
                selectedFiles.forEach { path ->
                    interfaceModel.addFileForUpload(path)
                    println(path)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Upload,
                contentDescription = "Upload",
                tint = Color.DarkGray,
                modifier = Modifier.size(48.dp)
            )
            Text("Drag & Drop or Click to Upload", textAlign = TextAlign.Center)
        }
    }
}
