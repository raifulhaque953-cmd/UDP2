package com.pocketshark.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.pocketshark.app.ui.theme.PocketSharkColors

/**
 * Canvas-based smooth rolling live traffic line/area graph.
 * Connected strictly to REAL packet capture statistics.
 * Features a fixed-window time series buffer, smooth Y-axis scaling,
 * cubic Bézier curves, and continuous non-jittering flow.
 */
@Composable
fun TrafficGraph(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = PocketSharkColors.ChartLine,
    fillGradientStart: Color = PocketSharkColors.ChartGradientStart,
    fillGradientEnd: Color = PocketSharkColors.ChartGradientEnd,
    gridColor: Color = PocketSharkColors.ChartGrid
) {
    // Fixed window buffer size of 30 samples
    val targetSize = 30

    // Ensure samples list always has exactly targetSize elements by padding 0s on the left
    val samples = remember(dataPoints) {
        if (dataPoints.size >= targetSize) {
            dataPoints.takeLast(targetSize)
        } else {
            List(targetSize - dataPoints.size) { 0f } + dataPoints
        }
    }

    // Smoothly animate Y-axis max scale to prevent vertical jumping on sudden spikes
    val rawMax = samples.maxOrNull()?.coerceAtLeast(5f) ?: 5f
    val animatedMaxY by animateFloatAsState(
        targetValue = rawMax,
        animationSpec = tween(400, easing = LinearOutSlowInEasing),
        label = "YMaxAnim"
    )

    // Pulsing head indicator for active capture stream
    val infiniteTransition = rememberInfiniteTransition(label = "GraphHeadPulse")
    val headPulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseRadius"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 8f

        // Draw horizontal grid background lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = padding + (height - 2 * padding) * i / gridLines
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 0.5f
            )
        }

        val pointCount = samples.size
        if (pointCount < 2) return@Canvas

        val stepX = (width - 2 * padding) / (pointCount - 1)

        // Compute stable canvas coordinates (X positions remain 100% fixed)
        val points = ArrayList<Offset>(pointCount)
        for (i in 0 until pointCount) {
            val x = padding + i * stepX
            val normalizedY = (samples[i] / animatedMaxY).coerceIn(0f, 1f)
            val y = height - padding - normalizedY * (height - 2 * padding)
            points.add(Offset(x, y))
        }

        // Build continuous cubic Bézier curve
        val linePath = Path()
        val fillPath = Path()

        linePath.moveTo(points[0].x, points[0].y)
        fillPath.moveTo(points[0].x, height - padding)
        fillPath.lineTo(points[0].x, points[0].y)

        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val cpx = (prev.x + curr.x) / 2f

            linePath.cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
            fillPath.cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
        }

        val lastPoint = points.last()
        fillPath.lineTo(lastPoint.x, height - padding)
        fillPath.close()

        // 1. Draw area gradient fill under line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillGradientStart, fillGradientEnd),
                startY = 0f,
                endY = height
            )
        )

        // 2. Draw glowing trend line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Draw dots along recent points for clean technical look
        val stepInterval = 3
        for (i in 0 until points.size - 1 step stepInterval) {
            val pt = points[i]
            if (samples[i] > 0f) {
                drawCircle(
                    color = lineColor.copy(alpha = 0.6f),
                    radius = 2f,
                    center = pt
                )
            }
        }

        // 4. Highlight current live data head at the rightmost position
        val isLiveActive = samples.last() > 0f
        if (isLiveActive) {
            drawCircle(
                color = lineColor.copy(alpha = 0.25f),
                radius = headPulseRadius,
                center = lastPoint
            )
        }

        drawCircle(
            color = lineColor,
            radius = 4f,
            center = lastPoint
        )
        drawCircle(
            color = PocketSharkColors.BgPrimary,
            radius = 2f,
            center = lastPoint
        )
    }
}

