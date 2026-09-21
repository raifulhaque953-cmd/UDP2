package com.pocketshark.app.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketshark.app.capture.CaptureState
import com.pocketshark.app.ui.components.*
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.CaptureViewModel

/**
 * Main capture dashboard screen — redesigned with hero status card containing
 * a live traffic waveform, shark-fin monitoring control, 3×2 stat grid with
 * sparklines, and protocol distribution donut chart.
 *
 * Uses WindowInsets for safe-area handling and adaptive dimensions for
 * consistent rendering across Android screen sizes.
 */
@Composable
fun CaptureScreen(
    onSettingsClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {},
    onViewDetailsClick: () -> Unit = {},
    viewModel: CaptureViewModel = viewModel()
) {
    val captureState by viewModel.captureState.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val protocolDist by viewModel.protocolDistribution.collectAsStateWithLifecycle()
    val alertCount by viewModel.alertCount.collectAsStateWithLifecycle()
    val trafficHistory by viewModel.trafficHistory.collectAsStateWithLifecycle()
    val packetSparkline by viewModel.packetSparkline.collectAsStateWithLifecycle()
    val tcpSparkline by viewModel.tcpSparkline.collectAsStateWithLifecycle()
    val udpSparkline by viewModel.udpSparkline.collectAsStateWithLifecycle()
    val dnsSparkline by viewModel.dnsSparkline.collectAsStateWithLifecycle()
    val alertSparkline by viewModel.alertSparkline.collectAsStateWithLifecycle()
    val dataSparkline by viewModel.dataSparkline.collectAsStateWithLifecycle()

    // VPN consent launcher
    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startCapture()
        }
    }

    // Adaptive dimensions based on screen width
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)
    val cardSpacing = (screenWidthDp * 0.025f).coerceIn(6.dp, 12.dp)

    // Adaptive font sizes for hero card
    val durationFontSize = if (screenWidthDp < 360.dp) 22.sp else 28.sp
    val capturedFontSize = if (screenWidthDp < 360.dp) 20.sp else 24.sp

    // Adaptive graph height
    val graphHeight = (screenWidthDp * 0.2f).coerceIn(56.dp, 90.dp)

    // Adaptive donut chart size based on available width minus legend space
    val donutChartSize = ((screenWidthDp - horizontalPadding * 2) * 0.38f)
        .coerceIn(100.dp, 160.dp)
        .value.toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            // Apply status bar inset so the header clears the system bar
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            // Bottom padding: clear the floating bottom nav + navigation bar inset
            .navigationBarsPadding()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top Bar ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                            append("Pocket")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Shark")
                        }
                    },
                    style = PocketSharkTypography.headlineMedium,
                    color = PocketSharkColors.TextPrimary
                )
                Text(
                    text = "Network Analyzer",
                    style = PocketSharkTypography.labelSmall,
                    color = PocketSharkColors.TextTertiary
                )
            }
            GlassIconButton(
                icon = Icons.Default.Settings,
                onClick = onSettingsClick,
                tint = PocketSharkColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ── Hero Status Card ───────────────────────────────────────────
        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            delay = 100
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left side: Status + Duration
                Column(modifier = Modifier.weight(1f)) {
                    StatusIndicator(
                        label = when (captureState) {
                            is CaptureState.Idle -> "READY"
                            is CaptureState.Preparing -> "PREPARING"
                            is CaptureState.Running -> "CAPTURING"
                            is CaptureState.Stopped -> "STOPPED"
                            is CaptureState.Error -> "ERROR"
                        },
                        color = when (captureState) {
                            is CaptureState.Running -> PocketSharkColors.StatusActive
                            is CaptureState.Error -> PocketSharkColors.StatusDanger
                            is CaptureState.Preparing -> PocketSharkColors.StatusWarning
                            else -> PocketSharkColors.StatusReady
                        },
                        pulsing = captureState is CaptureState.Running
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (stats.startTime > 0) stats.formattedDuration else "00:00",
                        style = MonoValueStyle.copy(fontSize = durationFontSize),
                        color = PocketSharkColors.TextPrimary
                    )
                    Text(
                        text = "DURATION",
                        style = PocketSharkTypography.labelSmall,
                        color = PocketSharkColors.TextTertiary,
                        letterSpacing = 1.sp
                    )
                }

                // Right side: Captured packets + Total data
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CAPTURED",
                        style = PocketSharkTypography.labelSmall,
                        color = PocketSharkColors.TextTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${stats.packetCount}",
                        style = MonoValueStyle.copy(fontSize = capturedFontSize),
                        color = PocketSharkColors.AccentEmerald
                    )
                    Text(
                        text = "PACKETS",
                        style = PocketSharkTypography.labelSmall,
                        color = PocketSharkColors.TextTertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TOTAL DATA",
                        style = PocketSharkTypography.labelSmall,
                        color = PocketSharkColors.TextTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = stats.formattedBytes,
                        style = PocketSharkTypography.titleSmall.copy(
                            fontSize = if (screenWidthDp < 360.dp) 14.sp else 16.sp
                        ),
                        color = PocketSharkColors.TextSecondary
                    )
                }
            }

            // Embedded traffic waveform
            // Embedded traffic waveform
            Spacer(modifier = Modifier.height(10.dp))
            TrafficGraph(
                dataPoints = trafficHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(graphHeight)
            )

            if (captureState is CaptureState.Error) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = (captureState as CaptureState.Error).message,
                    style = PocketSharkTypography.bodySmall,
                    color = PocketSharkColors.StatusDanger
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Monitoring Control ─────────────────────────────────────────
        Text(
            text = "MONITORING CONTROL",
            style = PocketSharkTypography.labelSmall,
            color = PocketSharkColors.TextTertiary,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CaptureButton(
            captureState = captureState,
            onClick = {
                val vpnIntent = viewModel.toggleCapture()
                vpnIntent?.let { vpnLauncher.launch(it) }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Overview Section (3×2 grid) ────────────────────────────────
        GlassSectionHeader(
            title = "OVERVIEW",
            trailing = {
                Text(
                    text = "View All  ›",
                    style = PocketSharkTypography.labelSmall,
                    color = PocketSharkColors.AccentEmerald,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(cardSpacing)
        ) {
            // Row 1: Packets, Data, TCP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                StatCard(
                    label = "Packets",
                    value = "${stats.packetCount}",
                    icon = Icons.Default.SwapVert,
                    iconTint = PocketSharkColors.AccentEmerald,
                    trend = "${String.format("%.1f", stats.packetsPerSecond)}/s",
                    sparklineData = packetSparkline,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Data",
                    value = stats.formattedBytes,
                    icon = Icons.Default.Storage,
                    iconTint = PocketSharkColors.AccentCyan,
                    sparklineData = dataSparkline,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "TCP",
                    value = "${stats.tcpCount}",
                    icon = Icons.Default.Cable,
                    iconTint = PocketSharkColors.ProtocolTcp,
                    sparklineData = tcpSparkline,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: UDP, DNS, Alerts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                StatCard(
                    label = "UDP",
                    value = "${stats.udpCount}",
                    icon = Icons.Default.Speed,
                    iconTint = PocketSharkColors.ProtocolUdp,
                    sparklineData = udpSparkline,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "DNS",
                    value = "${stats.dnsCount}",
                    icon = Icons.Default.Dns,
                    iconTint = PocketSharkColors.ProtocolDns,
                    sparklineData = dnsSparkline,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Alerts",
                    value = "$alertCount",
                    icon = Icons.Default.Shield,
                    iconTint = if (alertCount > 0) PocketSharkColors.StatusWarning
                    else PocketSharkColors.AccentDeepGreen,
                    sparklineData = alertSparkline,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Protocol Distribution ──────────────────────────────────────
        if (protocolDist.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            GlassSectionHeader(
                title = "PROTOCOL DISTRIBUTION",
                trailing = {
                    Text(
                        text = "View Details  ›",
                        style = PocketSharkTypography.labelSmall,
                        color = PocketSharkColors.AccentEmerald,
                        modifier = Modifier.clickable { onViewDetailsClick() }
                    )
                }
            )

            AnimatedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                delay = 300
            ) {
                DonutChart(
                    protocolData = protocolDist,
                    chartSize = donutChartSize
                )
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}
