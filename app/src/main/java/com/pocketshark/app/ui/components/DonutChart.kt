package com.pocketshark.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pocketshark.app.data.model.Protocol
import com.pocketshark.app.ui.theme.*

/**
 * Canvas-based animated donut chart showing protocol distribution.
 * Renders colored arc segments with a glass center showing total count.
 * Uses per-segment animated sweep angles so data updates smoothly transition
 * without restarting the entire chart animation.
 */
@Composable
fun DonutChart(
    protocolData: Map<Protocol, Long>,
    modifier: Modifier = Modifier,
    chartSize: Int = 180
) {
    val total = protocolData.values.sum().toFloat().coerceAtLeast(1f)
    val sortedEntries = remember(protocolData) {
        protocolData.entries.sortedByDescending { it.value }
    }

    // One-shot intro animation: 0→1 on first composition only
    var hasAppeared by remember { mutableStateOf(false) }
    val introProgress by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "donutIntro"
    )
    LaunchedEffect(Unit) { hasAppeared = true }

    // Per-protocol target sweep angles (proportional fractions of 360°)
    val targetSweeps = remember(protocolData) {
        sortedEntries.map { (_, count) ->
            (count / total) * 360f
        }
    }

    // Animate each sweep independently so updates transition smoothly
    val animatedSweeps = targetSweeps.mapIndexed { index, target ->
        animateFloatAsState(
            targetValue = target * introProgress,
            animationSpec = tween(400, easing = EaseOutCubic),
            label = "sweep_$index"
        ).value
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut chart
        Box(
            modifier = Modifier.size(chartSize.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val strokeWidth = 24f
                val radius = (size.minDimension - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2, radius * 2)

                var startAngle = -90f

                sortedEntries.forEachIndexed { index, (protocol, _) ->
                    val sweep = animatedSweeps.getOrElse(index) { 0f }
                    if (sweep > 0.1f) {
                        drawArc(
                            color = protocol.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweep + 3f // 3° gap between segments
                }

                // Center circle (glass effect)
                drawCircle(
                    color = PocketSharkColors.BgSecondary,
                    radius = radius - strokeWidth
                )
                drawCircle(
                    color = PocketSharkColors.GlassBorder,
                    radius = radius - strokeWidth,
                    style = Stroke(width = 1f)
                )
            }

            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = total.toLong().toString(),
                    style = MonoValueStyle,
                    color = PocketSharkColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "PACKETS",
                    style = PocketSharkTypography.labelSmall,
                    color = PocketSharkColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Legend
        Column(
            modifier = Modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sortedEntries.forEach { (protocol, count) ->
                DonutLegendItem(
                    protocol = protocol,
                    count = count,
                    percentage = (count / total * 100).toInt()
                )
            }
        }
    }
}

@Composable
private fun DonutLegendItem(
    protocol: Protocol,
    count: Long,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = protocol.color)
        }
        Text(
            text = protocol.displayName,
            style = PocketSharkTypography.labelMedium,
            color = PocketSharkColors.TextPrimary,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = "$count",
            style = MonoStyle,
            color = PocketSharkColors.TextSecondary,
            modifier = Modifier.width(50.dp)
        )
        Text(
            text = "$percentage%",
            style = PocketSharkTypography.labelSmall,
            color = PocketSharkColors.TextTertiary
        )
    }
}
