package org.thesis.project.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun InfoBox(
    text: String,
    position: Offset,
    width: Dp = 250.dp,
    arrowDirection: TooltipArrowDirection = TooltipArrowDirection.Left
) {
    val arrowSize = 12.dp

    Box(
        modifier = Modifier
            .absoluteOffset { IntOffset(position.x.toInt(), position.y.toInt()) }
            .zIndex(1000f)
    ) {
        when (arrowDirection) {
            TooltipArrowDirection.Top -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TooltipArrow(arrowDirection)
                    InfoContent(text, width)
                }
            }

            TooltipArrowDirection.Bottom -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    InfoContent(text, width)
                    TooltipArrow(arrowDirection)
                }
            }

            TooltipArrowDirection.Left -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TooltipArrow(arrowDirection)
                    InfoContent(text, width)
                }
            }

            TooltipArrowDirection.Right -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoContent(text, width)
                    TooltipArrow(arrowDirection)
                }
            }
        }
    }
}

@Composable
private fun InfoContent(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = text,
            color = Color.Black,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

enum class TooltipArrowDirection {
    Left, Right, Top, Bottom
}
@Composable
fun TooltipArrow(direction: TooltipArrowDirection) {

    val color = LocalAppColors.current.primaryBlue

    Canvas(
        modifier = Modifier
            .size(12.dp)
            .padding(2.dp)
    ) {
        val triangle = androidx.compose.ui.graphics.Path().apply {
            when (direction) {
                TooltipArrowDirection.Left -> {
                    moveTo(size.width, 0f)
                    lineTo(0f, size.height / 2)
                    lineTo(size.width, size.height)
                }
                TooltipArrowDirection.Right -> {
                    moveTo(0f, 0f)
                    lineTo(size.width, size.height / 2)
                    lineTo(0f, size.height)
                }
                TooltipArrowDirection.Top -> {
                    moveTo(0f, size.height)
                    lineTo(size.width / 2, 0f)
                    lineTo(size.width, size.height)
                }
                TooltipArrowDirection.Bottom -> {
                    moveTo(0f, 0f)
                    lineTo(size.width / 2, size.height)
                    lineTo(size.width, 0f)
                }
            }
            close()
        }

        drawPath(
            path = triangle,
            color = color,
            style = Fill
        )

        drawPath(
            path = triangle,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}