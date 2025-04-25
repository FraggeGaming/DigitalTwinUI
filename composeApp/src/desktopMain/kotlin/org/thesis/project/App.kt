package org.thesis.project

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import navigationButtons
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.thesis.project.Components.regionVasterbottenTheme
import org.thesis.project.Components.topAppBar
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Screens.imageViewer
import org.thesis.project.Screens.uploadData


@Composable
@Preview
fun App() {
    regionVasterbottenTheme {
        val interfaceModel: InterfaceModel = viewModel()

        val navController = rememberNavController()
        NavHost(navController, startDestination = "upload") {

            composable("upload") {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        topAppBar(
                            title = "App Name",
                            modelName = "Model Name",
                            navMenu = { navigationButtons(navController, "upload") },
                            extraContent = { extraContent(interfaceModel, "upload") }
                        )

                        uploadData(
                            interfaceModel = interfaceModel,
                            navMenu = { navigationButtons(navController, "upload") },
                            navController
                        )

                    }

                    TooltipOverlay()
                }

            }

            composable("main") {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        topAppBar(
                            title = "App Name",
                            modelName = "Model Name",
                            navMenu = { navigationButtons(navController, "main") },
                            extraContent = { extraContent(interfaceModel, "main") }
                        )
                        imageViewer(
                            interfaceModel = interfaceModel,
                            navMenu = { navigationButtons(navController, "main") },
                            navController
                        )

                    }

                    TooltipOverlay()
                }


            }

        }
    }
}

@Composable
fun TooltipOverlay() {
    TooltipManager.Render()
}

object TooltipManager {
    // ✅ Now Compose will recompose when this changes
    val tooltips = mutableStateMapOf<String, @Composable () -> Unit>()

    fun show(id: String, content: @Composable () -> Unit) {
        tooltips[id] = content
    }

    fun clear(id: String) {
        tooltips.remove(id)
    }

    fun clearAll() {
        tooltips.clear()
    }

    @Composable
    fun Render() {
        tooltips.values.forEach { it() }
    }
}

@Composable
fun extraContent(interfaceModel: InterfaceModel, selected: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.padding(end = 16.dp)
    ) {
        IconButton(onClick = { interfaceModel.toggleInfoMode() }) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
        }

        if (selected == "main"){
            IconButton(
                onClick = { interfaceModel.panelLayout.toggleRightPanelExpanded() }
            ) {
                Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
            }
        }

    }
}

