package org.thesis.project.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem


/**
 * The dropdown menu used across the UI
 * **/
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun dropDownMenuCustom(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown
    val text = if (selected.isEmpty()) label else selected

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalAppColors.current.thirdlyBlue,
                contentColor = LocalAppColors.current.textColor
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(bottom = 4.dp)
                .fillMaxWidth()
        ) {
            Text(text)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(icon, contentDescription = if (expanded) "Collapse" else "Expand")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(180.dp)
            , containerColor = LocalAppColors.current.thirdlyBlue
        ) {
            options.forEach { option ->
                var isHovered by remember { mutableStateOf(false) }

                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                        .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

    }
}
