package com.pocketshark.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pocketshark.app.ui.theme.*

/**
 * Compact stat display card with icon, value, label, optional trend, and mini sparkline.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = PocketSharkColors.AccentEmerald,
    trend: String? = null,
    trendPositive: Boolean = true,
    sparklineData: List<Float> = emptyList()
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(12.dp),
        glowColor = iconTint
    ) {
        // Top row: icon + value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                style = MonoValueStyle.copy(fontSize = MonoValueStyle.fontSize * 0.85f),
                color = PocketSharkColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Label + trend row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = PocketSharkTypography.labelSmall,
                color = PocketSharkColors.TextTertiary
            )
            if (trend != null) {
                Text(
                    text = trend,
                    style = PocketSharkTypography.labelSmall,
                    color = if (trendPositive) PocketSharkColors.AccentEmerald
                    else PocketSharkColors.StatusDanger
                )
            }
        }

        // Mini sparkline
        if (sparklineData.size >= 2) {
            Spacer(modifier = Modifier.height(4.dp))
            MiniSparkline(
                data = sparklineData,
                color = iconTint,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )
        }
    }
}

/**
 * Tiny sparkline chart drawn on Canvas for stat card bottoms.
 */
@Composable
fun MiniSparkline(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val maxVal = data.max().coerceAtLeast(1f)
        val stepX = size.width / (data.size - 1)
        val path = Path()

        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = size.height - (value / maxVal) * size.height * 0.85f
            if (i == 0) path.moveTo(x, y)
            else {
                val prevX = (i - 1) * stepX
                val prevY = size.height - (data[i - 1] / maxVal) * size.height * 0.85f
                val cpX = (prevX + x) / 2f
                path.cubicTo(cpX, prevY, cpX, y, x, y)
            }
        }

        drawPath(
            path = path,
            color = color.copy(alpha = 0.7f),
            style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

// ── Status Indicator ───────────────────────────────────────────────────

/**
 * Dot + label composable for displaying capture/connection states.
 */
@Composable
fun StatusIndicator(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Status dot
        if (pulsing) {
            PulsingDot(color = color)
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .aspectRatio(1f)
                    .padding(0.dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = color)
                }
            }
        }

        Text(
            text = label,
            style = PocketSharkTypography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun PulsingDot(color: Color) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(modifier = Modifier.size(8.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = color.copy(alpha = alpha * 0.3f), radius = size.minDimension)
            drawCircle(color = color.copy(alpha = alpha))
        }
    }
}
