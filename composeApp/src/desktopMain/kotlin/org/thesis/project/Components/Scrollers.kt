package org.thesis.project.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.thesis.project.Model.NiftiData

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


    scrollWithTitle(
        title = "Slice",
        value = sliderPosition,
        onValueChange = { newValue ->
            sliderPosition = newValue
            onUpdate(newValue)
        },
        valueRange = 0f..maxValue,
    )



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
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, color = LocalAppColors.current.primaryBlue)

        var inputText by remember { mutableStateOf(value.toInt().toString()) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Slider(
                value = value,
                onValueChange = {
                    inputText = it.toInt().toString()
                    onValueChange(it)
                },
                valueRange = valueRange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = LocalAppColors.current.primaryBlue,
                    activeTrackColor = LocalAppColors.current.secondaryBlue,
                    inactiveTrackColor = LocalAppColors.current.primaryGray
                )
            )

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(32.dp)
                    //.border(1.dp, LocalAppColors.current.primaryDarkGray, RoundedCornerShape(6.dp))
                    .background(LocalAppColors.current.secondaryBlue, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { newText ->
                        inputText = newText
                        val floatValue = newText.toFloatOrNull()?.coerceIn(valueRange.start, valueRange.endInclusive)
                        if (floatValue != null) {
                            onValueChange(floatValue)
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}