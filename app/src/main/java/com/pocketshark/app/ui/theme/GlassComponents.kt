package com.pocketshark.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reusable liquid glass composables for the PocketShark design system.
 */

// ── Glass Card ─────────────────────────────────────────────────────────

/**
 * Multi-layer translucent card with the liquid glass effect.
 * Renders: dark gradient background → inner highlight → thin glowing border → content.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = PocketSharkColors.AccentEmerald,
    cornerRadius: Dp = 20.dp,
    glowIntensity: Float = 0.12f,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                // Outer glow shadow
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.04f),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    size = size
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PocketSharkColors.GlassBg,
                        PocketSharkColors.GlassBg.copy(alpha = 0.5f),
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        glowColor.copy(alpha = glowIntensity),
                        glowColor.copy(alpha = glowIntensity * 0.3f),
                    )
                ),
                shape = shape
            )
    ) {
        // Inner highlight line at top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            glowColor.copy(alpha = 0.15f),
                            Color.Transparent,
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

// ── Glass Button ───────────────────────────────────────────────────────

/**
 * Tactile glass-surface button with press-scale animation and glow.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glowColor: Color = PocketSharkColors.AccentEmerald,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.2f),
                        glowColor.copy(alpha = 0.08f),
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = glowColor.copy(alpha = 0.25f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

// ── Glass Icon Button ──────────────────────────────────────────────────

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = PocketSharkColors.TextSecondary,
    size: Dp = 40.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconBtnScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(PocketSharkColors.GlassBgLight)
            .border(1.dp, PocketSharkColors.GlassBorder, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ── Glass Top Bar ──────────────────────────────────────────────────────

@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = PocketSharkTypography.headlineMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = actions
        )
    }
}

// ── Glass Section Header ───────────────────────────────────────────────

@Composable
fun GlassSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = PocketSharkTypography.titleSmall,
            color = PocketSharkColors.TextSecondary
        )
        trailing()
    }
}

// ── Glass Divider ──────────────────────────────────────────────────────

@Composable
fun GlassDivider(
    modifier: Modifier = Modifier,
    color: Color = PocketSharkColors.GlassBorder
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

// ── Animated Glass Card (fade + slide entrance) ────────────────────────

@Composable
fun AnimatedGlassCard(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    glowColor: Color = PocketSharkColors.AccentEmerald,
    content: @Composable ColumnScope.() -> Unit
) {
    val animatedAlpha = remember { Animatable(0f) }
    val animatedOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        launch {
            animatedAlpha.animateTo(
                1f,
                animationSpec = tween(500, easing = EaseOutCubic)
            )
        }
        launch {
            animatedOffset.animateTo(
                0f,
                animationSpec = tween(500, easing = EaseOutCubic)
            )
        }
    }

    GlassCard(
        modifier = modifier
            .graphicsLayer {
                alpha = animatedAlpha.value
                translationY = animatedOffset.value
            },
        glowColor = glowColor,
        content = content
    )
}
