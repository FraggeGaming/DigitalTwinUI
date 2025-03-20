package org.thesis.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.StateFlow
import navigationButtons
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.thesis.project.Model.InterfaceModel
import androidx.compose.ui.unit.Dp
import org.thesis.project.Components.scrollWithTitle
import org.thesis.project.Screens.imageViewer
import org.thesis.project.Screens.modelSelect
import org.thesis.project.Screens.uploadData


@Composable
@Preview
fun App() {
    MaterialTheme {
        val interfaceModel: InterfaceModel = viewModel()

        val navController = rememberNavController()
        NavHost(navController, startDestination = "main") {

            composable("upload") {
                uploadData(interfaceModel = interfaceModel, navMenu = { navigationButtons(navController, "upload") }, navController)
            }

            composable("modelSelect") {
                modelSelect(
                    interfaceModel = interfaceModel,
                    navMenu = { navigationButtons(navController, "modelSelect") }, navController)
            }

            composable("main") {


                imageViewer(interfaceModel = interfaceModel, navMenu = { navigationButtons(navController, "main") }, navController)
            }
        }
    }
}