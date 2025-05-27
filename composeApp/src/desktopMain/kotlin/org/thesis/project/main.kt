package org.thesis.project

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/*
 * This file is part of [DeepTwin-X]
 * Copyright (C) 2025 [Fardis Nazemroaya Sedeh]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DeepTwin",
        state = rememberWindowState(width = 1920.dp, height = 1080.dp)

    ) {
        App()
    }
}