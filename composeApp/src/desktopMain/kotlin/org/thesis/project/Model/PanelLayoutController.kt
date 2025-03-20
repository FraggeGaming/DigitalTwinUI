package org.thesis.project.Model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PanelLayoutController {
    // Define initial values as constants or properties
    private val initialLeftPanelWidth: Dp = 400.dp
    private val initialRightPanelWidth: Dp = 300.dp

    // Panel layout state using Dp
    private val _leftPanelWidth = MutableStateFlow(initialLeftPanelWidth)
    val leftPanelWidth: StateFlow<Dp> = _leftPanelWidth

    private val _rightPanelWidth = MutableStateFlow(initialRightPanelWidth)
    val rightPanelWidth: StateFlow<Dp> = _rightPanelWidth

    private val _leftPanelExpanded = MutableStateFlow(true)
    val leftPanelExpanded: StateFlow<Boolean> = _leftPanelExpanded

    private val _rightPanelExpanded = MutableStateFlow(true)
    val rightPanelExpanded: StateFlow<Boolean> = _rightPanelExpanded

    fun updateLeftPanelWidth(newWidth: Dp) {
        _leftPanelWidth.value = newWidth
    }

    fun updateRightPanelWidth(newWidth: Dp) {
        _rightPanelWidth.value = newWidth
    }

    fun toggleLeftPanelExpanded() {
        _leftPanelExpanded.value = !_leftPanelExpanded.value
    }

    fun toggleRightPanelExpanded() {
        _rightPanelExpanded.value = !_rightPanelExpanded.value
    }
}