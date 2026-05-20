package com.example.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class LineChartPoint(
    val label: String,
    val value: Float,
)

@Composable
fun LineAreaChart(
    points: List<LineChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    areaColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    dotColor: Color = MaterialTheme.colorScheme.primary,
) {
    if (points.size < 2) return

    val maxValue = points.maxOf { it.value }.coerceAtLeast(1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val coordinates = points.mapIndexed { index, point ->
            val x = index * stepX
            val y = height - (point.value / maxValue) * height
            Offset(x, y)
        }

        // Draw area fill
        val areaPath = Path().apply {
            moveTo(coordinates.first().x, height)
            coordinates.forEach { lineTo(it.x, it.y) }
            lineTo(coordinates.last().x, height)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(areaColor, areaColor.copy(alpha = 0.02f)),
                startY = 0f,
                endY = height,
            ),
        )

        // Draw line
        val linePath = Path().apply {
            moveTo(coordinates.first().x, coordinates.first().y)
            for (i in 1 until coordinates.size) {
                val prev = coordinates[i - 1]
                val curr = coordinates[i]
                val cpX = (prev.x + curr.x) / 2f
                cubicTo(cpX, prev.y, cpX, curr.y, curr.x, curr.y)
            }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Draw dots
        coordinates.forEach { point ->
            drawCircle(
                color = dotColor,
                radius = 4.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point,
            )
        }
    }
}
