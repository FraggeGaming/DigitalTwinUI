package org.thesis.project.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    text: String,
    onSelected: (String) -> Unit,
) {

    Box(
        modifier = Modifier
            .width(150.dp)
            .height(150.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .clickable {
                val selectedFiles = selectFilesMultipleAWT()
                selectedFiles.forEach { path ->
                    onSelected(path)
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
            Text(text, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun fileUploadCircle(
    onSelected: (String) -> Unit,
    ){

    Box(
        modifier = Modifier
            .size(64.dp) //Circular size
            .clip(CircleShape)
            .background(Color.LightGray)
            .clickable {
                val selectedFiles = selectFilesMultipleAWT()
                selectedFiles.forEach { path ->
                    onSelected(path)
                    println(path)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Files",
            tint = Color.DarkGray,
            modifier = Modifier.size(32.dp)
        )
    }
}
