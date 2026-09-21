package com.pocketshark.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PocketShark Design Token System
 * Liquid Glass cybersecurity aesthetic — near-black backgrounds with emerald accents.
 */
object PocketSharkColors {

    // ── Backgrounds ────────────────────────────────────────────────────
    val BgPrimary = Color(0xFF050A05)
    val BgSecondary = Color(0xFF0A120A)
    val BgTertiary = Color(0xFF0F1A0F)

    // ── Glass Surfaces ─────────────────────────────────────────────────
    val GlassBg = Color(0xA60A140C)           // rgba(10, 20, 12, 0.65)
    val GlassBgLight = Color(0x400A140C)      // rgba(10, 20, 12, 0.25)
    val GlassBorder = Color(0x1F34D399)       // rgba(52, 211, 153, 0.12)
    val GlassBorderActive = Color(0x3334D399) // rgba(52, 211, 153, 0.20)
    val GlassHighlight = Color(0x0F34D399)    // rgba(52, 211, 153, 0.06)

    // ── Accent Colors ──────────────────────────────────────────────────
    val AccentEmerald = Color(0xFF34D399)
    val AccentDeepGreen = Color(0xFF059669)
    val AccentLime = Color(0xFFA3E635)
    val AccentCyan = Color(0xFF22D3EE)
    val AccentCyanMuted = Color(0x8022D3EE)   // 50% opacity

    // ── Protocol Colors ────────────────────────────────────────────────
    val ProtocolTcp = Color(0xFF34D399)
    val ProtocolUdp = Color(0xFFA78BFA)
    val ProtocolDns = Color(0xFF22D3EE)
    val ProtocolTls = Color(0xFF60A5FA)
    val ProtocolIcmp = Color(0xFFFBBF24)
    val ProtocolHttp = Color(0xFFF87171)

    // ── Status Colors ──────────────────────────────────────────────────
    val StatusReady = AccentEmerald
    val StatusActive = Color(0xFF22C55E)
    val StatusWarning = Color(0xFFFBBF24)
    val StatusDanger = Color(0xFFF87171)
    val StatusInfo = Color(0xFF60A5FA)

    // ── Severity Colors ────────────────────────────────────────────────
    val SeverityLow = Color(0xFF60A5FA)
    val SeverityMedium = Color(0xFFFBBF24)
    val SeverityHigh = Color(0xFFF97316)
    val SeverityCritical = Color(0xFFEF4444)

    // ── Text Colors ────────────────────────────────────────────────────
    val TextPrimary = Color(0xFFF8FAFC)        // Slate-50 crisp white
    val TextSecondary = Color(0xFFCBD5E1)      // Slate-300 high contrast light slate
    val TextTertiary = Color(0xFF94A3B8)       // Slate-400 clear neutral slate
    val TextActive = AccentEmerald

    // ── Connection State Colors ────────────────────────────────────────
    val ConnEstablished = Color(0xFF22C55E)
    val ConnSyn = Color(0xFFFBBF24)
    val ConnFin = Color(0xFFF97316)
    val ConnClosed = Color(0xFFEF4444)
    val ConnActive = Color(0xFF34D399)

    // ── Chart Colors ───────────────────────────────────────────────────
    val ChartGradientStart = AccentEmerald.copy(alpha = 0.4f)
    val ChartGradientEnd = AccentEmerald.copy(alpha = 0.02f)
    val ChartGrid = Color(0x1AFFFFFF)
    val ChartLine = AccentEmerald
}
