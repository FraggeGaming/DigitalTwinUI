package org.thesis.project.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun topAppBar(title: String, modelName: String,  extraContent: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF0050A0)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = "App Logo",
                tint = Color.White
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(modelName, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = "Logo",
                    tint = Color.White
                )
            }
        }

        extraContent()

//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.End,
//            modifier = Modifier.padding(end = 16.dp)
//        ) {
//            IconButton(onClick = { /* show info */ }) {
//                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
//            }
//
//            IconButton(
//                onClick = { interfaceModel.toggleRightPanelExpanded() }
//            ) {
//                Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
//            }
//        }
    }
}