package org.thesis.project

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import navigationButtons
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.thesis.project.Model.InterfaceModel
import org.thesis.project.Screens.imageViewer
//import org.thesis.project.Screens.modelSelect
import org.thesis.project.Screens.uploadData


@Composable
@Preview
fun App() {
    MaterialTheme {
        val interfaceModel: InterfaceModel = viewModel()

        val navController = rememberNavController()
        NavHost(navController, startDestination = "main") {

            composable("upload") {
                uploadData(
                    interfaceModel = interfaceModel,
                    navMenu = { navigationButtons(navController, "upload") },
                    navController
                )
            }

//            composable("modelSelect") {
//                modelSelect(
//                    interfaceModel = interfaceModel,
//                    navMenu = { navigationButtons(navController, "modelSelect") }, navController
//                )
//            }

            composable("main") {


                imageViewer(
                    interfaceModel = interfaceModel,
                    navMenu = { navigationButtons(navController, "main") },
                    navController
                )
            }
        }
    }
}