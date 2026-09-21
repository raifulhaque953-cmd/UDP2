package com.pocketshark.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * PocketShark dark color scheme mapped to the liquid glass design tokens.
 */
private val PocketSharkColorScheme = darkColorScheme(
    primary = PocketSharkColors.AccentEmerald,
    onPrimary = PocketSharkColors.BgPrimary,
    primaryContainer = PocketSharkColors.AccentDeepGreen,
    onPrimaryContainer = PocketSharkColors.TextPrimary,
    secondary = PocketSharkColors.AccentCyan,
    onSecondary = PocketSharkColors.BgPrimary,
    secondaryContainer = Color(0xFF0D3D3D),
    onSecondaryContainer = PocketSharkColors.AccentCyan,
    tertiary = PocketSharkColors.AccentLime,
    onTertiary = PocketSharkColors.BgPrimary,
    background = PocketSharkColors.BgPrimary,
    onBackground = PocketSharkColors.TextPrimary,
    surface = PocketSharkColors.BgSecondary,
    onSurface = PocketSharkColors.TextPrimary,
    surfaceVariant = PocketSharkColors.BgTertiary,
    onSurfaceVariant = PocketSharkColors.TextSecondary,
    outline = PocketSharkColors.GlassBorder,
    outlineVariant = PocketSharkColors.GlassHighlight,
    error = PocketSharkColors.StatusDanger,
    onError = PocketSharkColors.TextPrimary,
)

/**
 * PocketShark Material 3 theme with liquid glass aesthetics.
 * Always uses dark theme — the app is designed exclusively for dark mode.
 *
 * Disables the Compose stretch overscroll effect to prevent
 * "Software rendering doesn't support drawRenderNode" crashes.
 * The stretch effect internally uses RenderNode which is incompatible
 * with software-rendered views (e.g. BlurView's PreDrawBlurController).
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PocketSharkTheme(content: @Composable () -> Unit) {
    val colorScheme = PocketSharkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PocketSharkColors.BgPrimary.toArgb()
            window.navigationBarColor = PocketSharkColors.BgPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    // Disable the stretch overscroll effect to avoid drawRenderNode crash
    // when BlurView or other Android Views force a software rendering path.
    // Normal scrolling behavior is unaffected — only the rubber-band stretch
    // visual is suppressed.
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PocketSharkTypography,
            content = content
        )
    }
}
