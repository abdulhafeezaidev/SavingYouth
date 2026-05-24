package com.behavioralfirewall.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: List<Float>,
    lineColor: Color = Color(0xFF80CBC4),
    gradientColor: Color = Color(0x2680CBC4)
) {
    Canvas(
        modifier = modifier.chartSemantics("Line chart demonstrating weekly recovery score trend.")
    ) {
        if (data.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val maxVal = (data.maxOrNull() ?: 100f).coerceAtLeast(1f)
        val stepX = width / (data.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        // We build a smooth bezier curve path
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal * height * 0.75f)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                // Smooth bezier calculation
                val prevX = (index - 1) * stepX
                val prevY = height - (data[index - 1] / maxVal * height * 0.75f)
                
                val controlX1 = prevX + (stepX / 2f)
                val controlY1 = prevY
                val controlX2 = prevX + (stepX / 2f)
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }

            if (index == data.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw background gradient under the curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientColor, Color.Transparent)
            )
        )

        // Draw primary curve line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Draw soft decorative circular indicator points
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal * height * 0.75f)
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(x, y)
            )
            drawCircle(
                color = lineColor,
                radius = 12f,
                center = Offset(x, y),
                style = Stroke(width = 4f)
            )
        }
    }
}

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    data: List<Float>,
    labels: List<String>,
    barColor: Color = Color(0xFFB39DDB)
) {
    Canvas(
        modifier = modifier.chartSemantics("Bar chart showing vulnerable trigger incidents.")
    ) {
        if (data.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val maxVal = (data.maxOrNull() ?: 100f).coerceAtLeast(1f)

        val barCount = data.size
        val gap = 24f
        val barWidth = (width - (gap * (barCount - 1))) / barCount

        data.forEachIndexed { index, value ->
            val left = index * (barWidth + gap)
            val top = height - (value / maxVal * height * 0.8f)
            val right = left + barWidth
            val bottom = height

            // Draw shadow backup bar
            drawRoundRect(
                color = Color(0x0AFFFFFF),
                topLeft = Offset(left, 0f),
                size = Size(barWidth, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )

            // Draw filled bar with smooth corners
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, (bottom - top).coerceAtLeast(8f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
        }
    }
}

@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    percentage: Float,
    arcColor: Color = Color(0xFF80CBC4),
    trackColor: Color = Color(0xFF132A27)
) {
    Box(
        modifier = modifier.chartSemantics("Donut chart demonstrating general success rate is ${(percentage * 100).toInt()}%"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20f
            
            // Draw background track
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidth)
            )

            // Draw progress arc
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = percentage * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Text(
            text = "${(percentage * 100).toInt()}%",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
    }
}
