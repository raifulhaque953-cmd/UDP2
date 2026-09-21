package com.pocketshark.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.view.ViewGroup
import androidx.compose.ui.viewinterop.AndroidView
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import com.pocketshark.app.ui.navigation.BottomNavItem
import com.pocketshark.app.ui.navigation.bottomNavItems
import com.pocketshark.app.ui.theme.PocketSharkColors
import com.pocketshark.app.ui.theme.PocketSharkTypography
import com.pocketshark.app.viewmodel.PacketsViewModel
import kotlinx.coroutines.launch

/**
 * Host screen containing a HorizontalPager to allow horizontal swiping
 * between the 5 main dashboard screens: Capture, Packets, Analytics, Connect, and Security.
 */
@Composable
fun DashboardScreen(
    onNavigateToPacketDetail: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    // 5 pages for: Capture (0), Packets (1), Analytics (2), Connect (3), Security (4)
    val pagerState = rememberPagerState(initialPage = 0) { 5 }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
    ) {
        // Horizontal Page Scroll area
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> CaptureScreen(
                    onSettingsClick = onSettingsClick,
                    onViewAllClick = onViewAllClick,
                    onViewDetailsClick = onViewDetailsClick
                )
                1 -> {
                    val packetsVm: PacketsViewModel = viewModel()
                    PacketsScreen(
                        viewModel = packetsVm,
                        onPacketClick = onNavigateToPacketDetail
                    )
                }
                2 -> AnalyticsScreen()
                3 -> ConnectionsScreen()
                4 -> SecurityScreen()
            }
        }

        // Floating Bottom Nav Bar synchronized with Pager
        GlassBottomNavBar(
            items = bottomNavItems,
            selectedIndex = pagerState.currentPage,
            onItemClick = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = index,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = EaseOutCubic
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun getContextActivity(context: android.content.Context): android.app.Activity? {
    var ctx = context
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) {
            return ctx
        }
        ctx = ctx.baseContext
    }
    return null
}

@Composable
private fun GlassBottomNavBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadiusValue = 40.dp
    val shape = RoundedCornerShape(cornerRadiusValue)
    
    // Animate a breathing pulse for the background glow
    val infiniteTransition = rememberInfiniteTransition(label = "liquidGlow")
    val liquidPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquidPulse"
    )

    // Animated offset of the selected tab index (e.g., 0.0 to 4.0)
    val animatedOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tabOffset"
    )

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(82.dp)
            .graphicsLayer {
                shadowElevation = 12f
                clip = true
                this.shape = shape
            }
    ) {
        // 1. Native Backdrop Blur Background
        AndroidView(
            factory = { ctx ->
                BlurView(ctx).apply {
                    val density = ctx.resources.displayMetrics.density
                    val rPx = cornerRadiusValue.value * density
                    val shapeDrawable = android.graphics.drawable.GradientDrawable().apply {
                        setShape(android.graphics.drawable.GradientDrawable.RECTANGLE)
                        cornerRadius = rPx
                        // Very dark tealy-green tint
                        setColor(android.graphics.Color.parseColor("#DF081414"))
                    }
                    background = shapeDrawable
                    clipToOutline = true // Ensure rounded clipping of the blurred content

                    // Force hardware acceleration on this view to prevent
                    // "Software rendering doesn't support drawRenderNode" crash.
                    // BlurView's PreDrawBlurController can force a software layer
                    // internally which conflicts with Compose's stretch overscroll.
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                    post {
                        // Only set up blur if the view is hardware-accelerated.
                        // If not, the tinted background still looks good without blur.
                        if (!isHardwareAccelerated) return@post

                        try {
                            val activity = getContextActivity(ctx)
                            val rootView = activity?.window?.decorView as? ViewGroup
                            if (rootView != null) {
                                val windowBackground = activity.window.decorView.background
                                // Blur radius: 24f (20-28dp range)
                                setupWith(rootView, RenderScriptBlur(ctx))
                                    .setFrameClearDrawable(windowBackground)
                                    .setBlurRadius(24f)
                            }
                        } catch (_: Exception) {
                            // Graceful degradation: if blur setup fails (e.g. due to
                            // RenderScript unavailability), the tinted background remains
                        }
                    }
                }
            },
            update = {},
            modifier = Modifier.fillMaxSize()
        )

        // 2. Luminous border overlay and glows drawn in Compose
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val rShape = CornerRadius(size.height / 2f)

                    // Glowing back shadow
                    drawRoundRect(
                        color = Color(0xFF22D3EE).copy(alpha = 0.05f * liquidPulse),
                        cornerRadius = rShape,
                        size = size
                    )

                    // Outer highlight stroke (Luminous Border - 20% opacity)
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF22D3EE).copy(alpha = 0.20f),
                                Color(0xFF34D399).copy(alpha = 0.05f)
                            )
                        ),
                        cornerRadius = rShape,
                        size = size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8f)
                    )

                    // Specular top-left highlight curve tracing the upper edge of the outer glass bar
                    val rOuter = size.height / 2f
                    val outerHighlightPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, rOuter)
                        cubicTo(
                            0f, rOuter * 0.25f,
                            rOuter * 0.25f, 0f,
                            rOuter, 0f
                        )
                        lineTo(size.width - rOuter, 0f)
                    }
                    drawPath(
                        path = outerHighlightPath,
                        color = Color.White.copy(alpha = 0.45f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f)
                    )
                }
        )

        // 3. Tab controls and sliding indicator
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
        val totalWidth = maxWidth
        val tabWidth = totalWidth / items.size
        val capsuleShape = RoundedCornerShape(percent = 50)

        // Sliding Active Capsule Background
        Box(
            modifier = Modifier
                .offset(x = tabWidth * animatedOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x28051E1E), // Green-teal glass highlight
                            Color(0x0C030B0B)
                        )
                    ),
                    shape = capsuleShape
                )
                .drawBehind {
                    // Radial green glow under the active capsule
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF34D399).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension * 0.9f
                    )

                    // Specular curve highlight on the top-left curve of the active capsule
                    val rCapsule = size.height / 2f
                    val highlightPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, rCapsule)
                        cubicTo(
                            0f, rCapsule * 0.25f,
                            rCapsule * 0.25f, 0f,
                            rCapsule, 0f
                        )
                        lineTo(rCapsule + size.width * 0.2f, 0f)
                    }
                    drawPath(
                        path = highlightPath,
                        color = Color.White.copy(alpha = 0.5f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f)
                    )
                }
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF22D3EE).copy(alpha = 0.85f),
                            Color(0xFF34D399).copy(alpha = 0.2f)
                        )
                    ),
                    shape = capsuleShape
                )
        )

        // Row of transparent NavItems
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index

                GlassNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
}

@Composable
private fun GlassNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PocketSharkColors.AccentEmerald
        else PocketSharkColors.TextTertiary,
        animationSpec = tween(
            durationMillis = 180,
            easing = EaseOutCubic
        ),
        label = "glassNavIconColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                vertical = 9.dp,
                horizontal = 4.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer {
                    if (isSelected) {
                        scaleX = 1.06f
                        scaleY = 1.06f
                    }
                }
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = item.label,
            style = PocketSharkTypography.labelSmall.copy(
                fontSize = PocketSharkTypography.labelSmall.fontSize * 0.9f
            ),
            color = if (isSelected) PocketSharkColors.AccentEmerald
            else PocketSharkColors.TextTertiary
        )
    }
}
