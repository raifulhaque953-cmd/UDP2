package com.pocketshark.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.pocketshark.app.capture.CaptureState
import com.pocketshark.app.ui.theme.PocketSharkColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Large circular glass capture button with shark fin icon, circular text,
 * and breathing glow animation. Matches the premium monitoring control design.
 */
@Composable
fun CaptureButton(
    captureState: CaptureState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCapturing = captureState is CaptureState.Running
    val isPreparing = captureState is CaptureState.Preparing

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "captureGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isCapturing) 0.75f else 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isCapturing) 800 else 2500,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    // Slow rotation for the circular text
    val textRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "textRotation"
    )

    val buttonColor = when {
        isCapturing -> PocketSharkColors.StatusDanger
        isPreparing -> PocketSharkColors.StatusWarning
        else -> PocketSharkColors.AccentEmerald
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale"
    )

    val circularText = when {
        isCapturing -> "S T O P   C A P T U R E   ·   "
        isPreparing -> "P R E P A R I N G   ·   ·   ·   "
        else -> "S T A R T   C A P T U R E   ·   "
    }

    val density = LocalDensity.current

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow aura
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(glowScale)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                buttonColor.copy(alpha = glowAlpha * 0.25f),
                                buttonColor.copy(alpha = glowAlpha * 0.08f),
                                Color.Transparent
                            ),
                            radius = size.minDimension / 1.2f
                        )
                    )
                }
        )

        // Circular text ring
        Canvas(
            modifier = Modifier.size(150.dp)
        ) {
            val textRadius = size.minDimension / 2f - 4f
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(
                    (glowAlpha * 180).toInt(),
                    (buttonColor.red * 255).toInt(),
                    (buttonColor.green * 255).toInt(),
                    (buttonColor.blue * 255).toInt()
                )
                textSize = with(density) { 8.dp.toPx() }
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                isAntiAlias = true
                letterSpacing = 0.15f
            }

            val chars = circularText.toCharArray()
            val totalChars = chars.size
            val anglePerChar = 360f / totalChars

            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(textRotation, centerX, centerY)

            for (i in chars.indices) {
                val angle = Math.toRadians((i * anglePerChar - 90.0))
                val x = centerX + textRadius * cos(angle).toFloat()
                val y = centerY + textRadius * sin(angle).toFloat()

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.translate(x, y)
                drawContext.canvas.nativeCanvas.rotate(i * anglePerChar)
                drawContext.canvas.nativeCanvas.drawText(
                    chars[i].toString(),
                    -paint.measureText(chars[i].toString()) / 2f,
                    paint.textSize / 3f,
                    paint
                )
                drawContext.canvas.nativeCanvas.restore()
            }

            drawContext.canvas.nativeCanvas.restore()
        }

        // Glow ring border
        Box(
            modifier = Modifier
                .size(125.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            buttonColor.copy(alpha = glowAlpha * 0.6f),
                            buttonColor.copy(alpha = glowAlpha * 0.1f),
                            buttonColor.copy(alpha = glowAlpha * 0.6f),
                            buttonColor.copy(alpha = glowAlpha * 0.1f),
                            buttonColor.copy(alpha = glowAlpha * 0.6f),
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main button circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(pressScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            buttonColor.copy(alpha = 0.20f),
                            buttonColor.copy(alpha = 0.08f),
                            PocketSharkColors.BgSecondary.copy(alpha = 0.9f),
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            buttonColor.copy(alpha = 0.4f),
                            buttonColor.copy(alpha = 0.15f),
                            buttonColor.copy(alpha = 0.4f),
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Shark fin icon drawn with Canvas
            Canvas(modifier = Modifier.size(40.dp)) {
                val w = size.width
                val h = size.height

                val finPath = Path().apply {
                    // Main dorsal fin shape
                    moveTo(w * 0.5f, h * 0.1f)       // tip of fin
                    cubicTo(
                        w * 0.55f, h * 0.25f,
                        w * 0.75f, h * 0.55f,
                        w * 0.85f, h * 0.7f
                    )
                    // Right curve down
                    cubicTo(
                        w * 0.9f, h * 0.78f,
                        w * 0.82f, h * 0.82f,
                        w * 0.7f, h * 0.8f
                    )
                    // Bottom wave
                    cubicTo(
                        w * 0.55f, h * 0.76f,
                        w * 0.45f, h * 0.76f,
                        w * 0.3f, h * 0.8f
                    )
                    // Left curve
                    cubicTo(
                        w * 0.18f, h * 0.82f,
                        w * 0.12f, h * 0.78f,
                        w * 0.2f, h * 0.7f
                    )
                    // Back up to tip
                    cubicTo(
                        w * 0.3f, h * 0.5f,
                        w * 0.4f, h * 0.3f,
                        w * 0.5f, h * 0.1f
                    )
                    close()
                }

                drawPath(
                    path = finPath,
                    color = buttonColor,
                    style = Fill
                )
                drawPath(
                    path = finPath,
                    color = buttonColor.copy(alpha = 0.5f),
                    style = Stroke(width = 1f)
                )
            }
        }
    }
}
