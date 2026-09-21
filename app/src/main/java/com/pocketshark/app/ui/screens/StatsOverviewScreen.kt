package com.pocketshark.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketshark.app.ui.components.TrafficGraph
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.CaptureViewModel

@Composable
fun StatsOverviewScreen(
    onBack: () -> Unit,
    viewModel: CaptureViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val trafficHistory by viewModel.trafficHistory.collectAsStateWithLifecycle()

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // ── Top Header ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = onBack,
                tint = PocketSharkColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Overview Details",
                style = PocketSharkTypography.headlineMedium,
                color = PocketSharkColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Live Traffic Waveform ──────────────────────────────────────
        GlassSectionHeader(title = "LIVE TRAFFIC WAVEFORM")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            delay = 50
        ) {
            TrafficGraph(
                dataPoints = trafficHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Capture Metrics ────────────────────────────────────────────
        GlassSectionHeader(title = "CAPTURE METRICS")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            delay = 100
        ) {
            MetricRow(label = "Duration", value = if (stats.startTime > 0) stats.formattedDuration else "00:00")
            MetricRow(label = "Total Packets", value = "${stats.packetCount}")
            MetricRow(label = "Total Data Bytes", value = stats.formattedBytes)
            MetricRow(label = "Avg Packet Rate", value = "${String.format("%.1f", stats.packetsPerSecond)} packets/s")
            MetricRow(label = "Avg Data Rate", value = "${String.format("%.1f", stats.bytesPerSecond / 1024f)} KB/s")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Protocol Details ───────────────────────────────────────────
        GlassSectionHeader(title = "PROTOCOL COUNTS")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            delay = 150
        ) {
            MetricRow(label = "TCP Packets", value = "${stats.tcpCount}", color = PocketSharkColors.ProtocolTcp)
            MetricRow(label = "UDP Packets", value = "${stats.udpCount}", color = PocketSharkColors.ProtocolUdp)
            MetricRow(label = "DNS Queries", value = "${stats.dnsCount}", color = PocketSharkColors.ProtocolDns)
            MetricRow(label = "ICMP Packets", value = "${stats.icmpCount}", color = PocketSharkColors.StatusWarning)
            MetricRow(label = "TLS/HTTPS Packets", value = "${stats.tlsCount}", color = PocketSharkColors.AccentCyan)
            MetricRow(label = "HTTP Packets", value = "${stats.httpCount}", color = PocketSharkColors.AccentLime)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Capture Info card ──────────────────────────────────────────
        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            delay = 200
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PocketSharkColors.AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Metrics are computed in real time from the active TUN interface capture stream.",
                    style = PocketSharkTypography.bodySmall,
                    color = PocketSharkColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    color: Color = PocketSharkColors.TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = PocketSharkTypography.bodyMedium,
            color = PocketSharkColors.TextSecondary
        )
        Text(
            text = value,
            style = MonoValueStyle.copy(fontSize = 15.sp),
            color = color
        )
    }
}
