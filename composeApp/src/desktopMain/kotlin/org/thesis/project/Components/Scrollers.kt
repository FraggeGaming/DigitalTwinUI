package org.thesis.project.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

@Composable
fun scrollSlider(
    selectedData: StateFlow<Set<String>>,
    scrollStep: StateFlow<Float>,
    maxIndexMap: StateFlow<Map<String, Float>>,
    onUpdate: (Float) -> Unit
) {
    val currentScrollStep by scrollStep.collectAsState()
    val selectedFilenames by selectedData.collectAsState()
    val maxSizeMap by maxIndexMap.collectAsState()

    val maxValue = selectedFilenames.mapNotNull { maxSizeMap[it] }.maxOrNull() ?: 1f

    var sliderPosition by remember { mutableStateOf(currentScrollStep) }

    Column {
        Text(text = "Slice: ${currentScrollStep.toInt()} / ${maxValue.toInt()}")
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                sliderPosition = newValue
                onUpdate(newValue)
            },
            valueRange = 0f..maxValue,
            steps = 0
        )

    }

    LaunchedEffect(currentScrollStep) {
        if (currentScrollStep != sliderPosition) {
            sliderPosition = currentScrollStep
        }
    }
}

@Composable
fun scrollWithTitle(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier
){
    Text(title)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier
    )
}