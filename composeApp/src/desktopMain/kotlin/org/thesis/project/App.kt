package org.thesis.project

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
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
//import org.thesis.project.Screens.modelSelect
import org.thesis.project.Screens.uploadData


@Composable
@Preview
fun App() {
    regionVasterbottenTheme {
        val interfaceModel: InterfaceModel = viewModel()

        val navController = rememberNavController()
        NavHost(navController, startDestination = "main") {

            composable("upload") {
                Column(modifier = Modifier.fillMaxSize()) {


                    topAppBar(
                        title = "App Name",
                        modelName = "Model Name",
                        navMenu = { navigationButtons(navController, "upload") },
                        extraContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                IconButton(onClick = { /* show info */ }) {
                                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                                }

                                IconButton(
                                    onClick = { interfaceModel.panelLayout.toggleRightPanelExpanded() }
                                ) {
                                    Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
                                }
                            }

                        }
                    )

                    uploadData(
                        interfaceModel = interfaceModel,
                        navMenu = { navigationButtons(navController, "upload") },
                        navController
                    )

                }
            }

            composable("main") {
                Column(modifier = Modifier.fillMaxSize()) {

                    topAppBar(
                        title = "App Name",
                        modelName = "Model Name",
                        navMenu = { navigationButtons(navController, "main") },
                        extraContent = {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                IconButton(onClick = { /* show info */ }) {
                                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                                }

                                IconButton(
                                    onClick = { interfaceModel.panelLayout.toggleRightPanelExpanded() }
                                ) {
                                    Icon(Icons.Filled.Tune, contentDescription = "Expand Right", tint = Color.White)
                                }
                            }

                        }
                    )
                    imageViewer(
                        interfaceModel = interfaceModel,
                        navMenu = { navigationButtons(navController, "main") },
                        navController
                    )

                }

            }

        }
    }
}
