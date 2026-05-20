package com.example.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutChartSegment(
    val label: String,
    val value: Float,
    val color: Color,
)

@Composable
fun DonutChart(
    segments: List<DonutChartSegment>,
    centerText: String,
    modifier: Modifier = Modifier,
    chartSize: Dp = 140.dp,
    strokeWidth: Dp = 20.dp,
) {
    if (segments.isEmpty()) return

    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(chartSize)) {
                val stroke = strokeWidth.toPx()
                var startAngle = -90f

                segments.forEach { segment ->
                    val sweepAngle = (segment.value / total) * 360f
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle.coerceAtLeast(0.5f),
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt),
                    )
                    startAngle += sweepAngle
                }
            }
            Text(
                text = centerText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            segments.take(5).forEach { segment ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = segment.color)
                    }
                    Text(
                        text = segment.label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    val percent = if (total > 0f) {
                        ((segment.value / total) * 100).toInt()
                    } else {
                        0
                    }
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
