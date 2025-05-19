package org.thesis.project

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DeepTwin",
        state = rememberWindowState(width = 1920.dp, height = 1080.dp)

    ) {
        App()
    }
}