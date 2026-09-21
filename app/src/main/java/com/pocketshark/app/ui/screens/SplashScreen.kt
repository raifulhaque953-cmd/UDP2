package com.pocketshark.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketshark.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * Premium animated splash screen with a cybersecurity/SOC aesthetic.
 *
 * Animation sequence (~2s total, auto-navigates at 2.4s):
 *  0.0–1.0s  Particles drift inward from edges, data lines converge toward center
 *  0.3–1.2s  Radar scanning rings pulse outward from center
 *  0.6–1.4s  Shark fin logo fades in and scales up at convergence point
 *  0.8–1.6s  "POCKETSHARK" brand text slides up + fades in
 *  1.0–1.8s  Subtitle "NETWORK • ANALYZE • SECURE" fades in
 *  1.8–2.4s  Hold, then navigate
 */
@Composable
fun SplashScreen(
    onNavigateToDashboard: () -> Unit
) {
    // ── Master timeline: drives particle convergence 0→1 ──
    val masterProgress = remember { Animatable(0f) }

    // ── Logo entrance ──
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.5f) }

    // ── Brand text entrance ──
    val brandAlpha = remember { Animatable(0f) }
    val brandOffsetY = remember { Animatable(20f) }

    // ── Subtitle entrance ──
    val subtitleAlpha = remember { Animatable(0f) }

    // ── Version badge ──
    val versionAlpha = remember { Animatable(0f) }

    // Orchestrate the animation sequence
    LaunchedEffect(Unit) {
        // Particles converge over 1.4s
        launch {
            masterProgress.animateTo(1f, tween(1400, easing = EaseInOutCubic))
        }

        // Logo appears at 600ms
        delay(600)
        launch {
            logoAlpha.animateTo(1f, tween(600, easing = EaseOutCubic))
        }
        launch {
            logoScale.animateTo(1f, spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ))
        }

        // Brand text at 800ms
        delay(200)
        launch {
            brandAlpha.animateTo(1f, tween(500, easing = EaseOutCubic))
        }
        launch {
            brandOffsetY.animateTo(0f, tween(500, easing = EaseOutCubic))
        }

        // Subtitle at 1000ms
        delay(200)
        launch {
            subtitleAlpha.animateTo(1f, tween(500, easing = EaseOutCubic))
        }

        // Version at 1200ms
        delay(200)
        launch {
            versionAlpha.animateTo(1f, tween(400, easing = EaseOutCubic))
        }

        // Navigate after hold
        delay(1000)
        onNavigateToDashboard()
    }

    // ── Pre-compute stable particle data once ──
    val particleData = remember { generateParticleData(50) }
    val dataLineData = remember { generateDataLineData(12) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020608))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // ── Full-screen canvas for particles and data lines ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val progress = masterProgress.value

            // 1. Background ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF22D3EE).copy(alpha = 0.06f * progress),
                        Color(0xFF0F766E).copy(alpha = 0.03f * progress),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = size.minDimension * 0.5f
                )
            )

            // 2. Network particles converging toward center
            drawConvergingParticles(
                particles = particleData,
                center = Offset(cx, cy),
                progress = progress,
                canvasSize = size
            )

            // 3. Data lines (thin glowing lines from edges toward center)
            drawDataLines(
                lines = dataLineData,
                center = Offset(cx, cy),
                progress = progress,
                canvasSize = size
            )
        }

        // ── Central content column ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Shark fin logo ──
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
            ) {
                drawSharkLogo(
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension * 0.42f
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── "POCKETSHARK" brand ──
            Text(
                text = "POCKETSHARK",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily,
                color = PocketSharkColors.TextPrimary,
                letterSpacing = 4.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = brandAlpha.value
                    translationY = brandOffsetY.value
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Subtitle ──
            Text(
                text = "NETWORK  \u2022  ANALYZE  \u2022  SECURE",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily,
                color = PocketSharkColors.AccentCyan.copy(alpha = 0.7f),
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                }
            )
        }

        // ── Version badge at bottom ──
        Text(
            text = "v1.0.0",
            style = PocketSharkTypography.labelSmall,
            color = PocketSharkColors.TextTertiary.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .graphicsLayer { alpha = versionAlpha.value }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Particle System
// ═══════════════════════════════════════════════════════════════════════

private data class ParticleState(
    /** Angle from center in radians (determines direction) */
    val angle: Float,
    /** Starting distance as fraction of canvas half-diagonal (1.0 = edge) */
    val startRadius: Float,
    /** Size of the particle dot */
    val size: Float,
    /** Base alpha */
    val alpha: Float,
    /** Speed multiplier (particles arrive at different times) */
    val speedBias: Float,
    /** Slight wobble frequency */
    val wobbleFreq: Float,
    /** Whether this particle leaves a fading trail line */
    val hasTrail: Boolean
)

private fun generateParticleData(count: Int): List<ParticleState> {
    val rng = Random(42)
    return List(count) {
        ParticleState(
            angle = rng.nextFloat() * 2f * PI.toFloat(),
            startRadius = 0.6f + rng.nextFloat() * 0.5f,
            size = 1.5f + rng.nextFloat() * 2.5f,
            alpha = 0.3f + rng.nextFloat() * 0.5f,
            speedBias = 0.5f + rng.nextFloat() * 0.5f,
            wobbleFreq = 2f + rng.nextFloat() * 4f,
            hasTrail = rng.nextFloat() > 0.55f
        )
    }
}

private fun DrawScope.drawConvergingParticles(
    particles: List<ParticleState>,
    center: Offset,
    progress: Float,
    canvasSize: Size
) {
    val halfDiag = sqrt(canvasSize.width * canvasSize.width +
            canvasSize.height * canvasSize.height) / 2f

    for (p in particles) {
        // Each particle converges at its own pace
        val t = ((progress - (1f - p.speedBias) * 0.3f) / p.speedBias).coerceIn(0f, 1f)
        val easedT = t * t * (3f - 2f * t) // smoothstep

        val maxR = p.startRadius * halfDiag
        val endR = 8f + p.size * 2f // cluster near center, not exactly at 0
        val currentR = maxR * (1f - easedT) + endR * easedT

        // Small perpendicular wobble
        val wobble = sin(progress * p.wobbleFreq * PI.toFloat() * 2f) * 6f * (1f - easedT)
        val perpAngle = p.angle + PI.toFloat() / 2f

        val px = center.x + cos(p.angle) * currentR + cos(perpAngle) * wobble
        val py = center.y + sin(p.angle) * currentR + sin(perpAngle) * wobble

        // Fade out as they arrive very close
        val fadeNearCenter = if (easedT > 0.85f) (1f - easedT) / 0.15f else 1f
        val alpha = p.alpha * fadeNearCenter

        // Trail line (from previous position toward center)
        if (p.hasTrail && easedT < 0.9f) {
            val trailLen = 25f * (1f - easedT)
            val tx = px + cos(p.angle) * trailLen
            val ty = py + sin(p.angle) * trailLen
            drawLine(
                color = Color(0xFF22D3EE).copy(alpha = alpha * 0.3f),
                start = Offset(tx, ty),
                end = Offset(px, py),
                strokeWidth = 1f
            )
        }

        // Particle dot
        drawCircle(
            color = Color(0xFF22D3EE).copy(alpha = alpha),
            radius = p.size,
            center = Offset(px, py)
        )

        // Soft glow halo on brighter particles
        if (p.alpha > 0.5f) {
            drawCircle(
                color = Color(0xFF22D3EE).copy(alpha = alpha * 0.15f),
                radius = p.size * 3f,
                center = Offset(px, py)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Data Lines (thin converging lines from edges)
// ═══════════════════════════════════════════════════════════════════════

private data class DataLine(
    val angle: Float,
    val length: Float,     // 0.3–0.7 of half diagonal
    val alpha: Float,
    val speedBias: Float,
    val width: Float
)

private fun generateDataLineData(count: Int): List<DataLine> {
    val rng = Random(77)
    return List(count) {
        DataLine(
            angle = it * (2f * PI.toFloat() / count) + rng.nextFloat() * 0.3f,
            length = 0.25f + rng.nextFloat() * 0.35f,
            alpha = 0.08f + rng.nextFloat() * 0.15f,
            speedBias = 0.6f + rng.nextFloat() * 0.4f,
            width = 0.5f + rng.nextFloat() * 1f
        )
    }
}

private fun DrawScope.drawDataLines(
    lines: List<DataLine>,
    center: Offset,
    progress: Float,
    canvasSize: Size
) {
    val halfDiag = sqrt(canvasSize.width * canvasSize.width +
            canvasSize.height * canvasSize.height) / 2f

    for (line in lines) {
        val t = ((progress - (1f - line.speedBias) * 0.2f) / line.speedBias).coerceIn(0f, 1f)
        val easedT = t * t

        // Line slides inward: outer end moves in, inner end moves in faster
        val outerR = halfDiag * line.length * (1f - easedT * 0.7f)
        val innerR = outerR * (1f - line.length) * (1f - easedT)

        val startX = center.x + cos(line.angle) * outerR
        val startY = center.y + sin(line.angle) * outerR
        val endX = center.x + cos(line.angle) * innerR
        val endY = center.y + sin(line.angle) * innerR

        // Fade in during first half, fade out in last quarter
        val fadeIn = (progress * 3f).coerceIn(0f, 1f)
        val fadeOut = if (easedT > 0.75f) (1f - easedT) / 0.25f else 1f

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF22D3EE).copy(alpha = line.alpha * fadeIn * fadeOut * 0.3f),
                    Color(0xFF22D3EE).copy(alpha = line.alpha * fadeIn * fadeOut)
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = line.width,
            cap = StrokeCap.Round
        )
    }
}



// ═══════════════════════════════════════════════════════════════════════
//  Shark Fin Logo (icon-grade vector with gradients and highlights)
// ═══════════════════════════════════════════════════════════════════════

private fun DrawScope.drawSharkLogo(center: Offset, radius: Float) {
    val R = radius

    // Outer ring with metallic gradient
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF22D3EE),
                Color(0xFF34D399),
                Color(0xFF0A303A)
            ),
            startY = center.y - R,
            endY = center.y + R
        ),
        radius = R,
        center = center,
        style = Stroke(width = R * 0.12f)
    )

    // Inner glow disc
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF22D3EE).copy(alpha = 0.06f),
                Color.Transparent
            ),
            center = center,
            radius = R * 0.85f
        )
    )

    // Drop shadow for the fin
    val shadowOffset = R * 0.06f
    val shadowPath = Path().apply {
        moveTo(center.x - R * 0.35f + shadowOffset, center.y + R * 0.3f + shadowOffset)
        lineTo(center.x + R * 0.35f + shadowOffset, center.y + R * 0.3f + shadowOffset)
        cubicTo(
            center.x + R * 0.35f + shadowOffset, center.y + R * 0.3f + shadowOffset,
            center.x + R * 0.25f + shadowOffset, center.y + R * 0.05f + shadowOffset,
            center.x + R * 0.2f + shadowOffset, center.y - R * 0.4f + shadowOffset
        )
        cubicTo(
            center.x - R * 0.1f + shadowOffset, center.y - R * 0.4f + shadowOffset,
            center.x - R * 0.32f + shadowOffset, center.y - R * 0.1f + shadowOffset,
            center.x - R * 0.35f + shadowOffset, center.y + R * 0.3f + shadowOffset
        )
        close()
    }
    drawPath(path = shadowPath, color = Color(0xFF000000).copy(alpha = 0.4f))

    // Central shark fin with gradient fill
    val finPath = Path().apply {
        moveTo(center.x - R * 0.35f, center.y + R * 0.3f)
        lineTo(center.x + R * 0.35f, center.y + R * 0.3f)
        cubicTo(
            center.x + R * 0.35f, center.y + R * 0.3f,
            center.x + R * 0.25f, center.y + R * 0.05f,
            center.x + R * 0.2f, center.y - R * 0.4f
        )
        cubicTo(
            center.x - R * 0.1f, center.y - R * 0.4f,
            center.x - R * 0.32f, center.y - R * 0.1f,
            center.x - R * 0.35f, center.y + R * 0.3f
        )
        close()
    }
    drawPath(
        path = finPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF22D3EE),
                Color(0xFF34D399),
                Color(0xFF0F766E)
            ),
            startY = center.y - R * 0.4f,
            endY = center.y + R * 0.3f
        )
    )

    // Specular highlight on the fin leading edge
    val highlightPath = Path().apply {
        moveTo(center.x - R * 0.35f, center.y + R * 0.3f)
        cubicTo(
            center.x - R * 0.32f, center.y - R * 0.1f,
            center.x - R * 0.1f, center.y - R * 0.4f,
            center.x + R * 0.2f, center.y - R * 0.4f
        )
    }
    drawPath(
        path = highlightPath,
        color = Color.White.copy(alpha = 0.45f),
        style = Stroke(width = R * 0.05f, cap = StrokeCap.Round)
    )
}
