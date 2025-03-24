package org.thesis.project.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp

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
                .width(180.dp)
        ) {
            Text(text)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(icon, contentDescription = if (expanded) "Collapse" else "Expand")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                color = LocalAppColors.current.thirdlyBlue,
                shape = RoundedCornerShape(8.dp)
            )
        ) {
            options.forEach { option ->
                var isHovered by remember { mutableStateOf(false) }
                val isSelected = option == selected

                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isSelected -> LocalAppColors.current.thirdlyBlue.copy(alpha = 0.8f)
                                isHovered -> LocalAppColors.current.thirdlyBlue.copy(alpha = 0.7f)
                                else -> Color.Transparent
                            }
                        )
                        .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                        .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
