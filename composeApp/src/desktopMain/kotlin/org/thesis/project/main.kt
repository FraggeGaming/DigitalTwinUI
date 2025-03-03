package org.thesis.project

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Imaging",
        state = rememberWindowState(width = 1600.dp, height = 900.dp)

    ) {
        App()
    }
}