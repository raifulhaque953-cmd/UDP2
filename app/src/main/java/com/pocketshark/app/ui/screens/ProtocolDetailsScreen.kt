package com.pocketshark.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketshark.app.data.model.Protocol
import com.pocketshark.app.ui.components.DonutChart
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.CaptureViewModel

@Composable
fun ProtocolDetailsScreen(
    onBack: () -> Unit,
    viewModel: CaptureViewModel = viewModel()
) {
    val protocolDist by viewModel.protocolDistribution.collectAsStateWithLifecycle()
    val totalPackets = protocolDist.values.sum().toFloat().coerceAtLeast(1f)

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)

    val sortedProtocols = remember(protocolDist) {
        protocolDist.entries.sortedByDescending { it.value }
    }

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
                text = "Protocol Breakdown",
                style = PocketSharkTypography.headlineMedium,
                color = PocketSharkColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Visual Donut Chart Card ────────────────────────────────────
        GlassSectionHeader(title = "VISUAL RATIO CHART")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            delay = 50
        ) {
            if (protocolDist.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        protocolData = protocolDist,
                        chartSize = 180
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(PocketSharkColors.GlassBgLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No captured protocol data to display",
                        style = PocketSharkTypography.bodySmall,
                        color = PocketSharkColors.TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Detailed breakdown lists ────────────────────────────────────
        GlassSectionHeader(title = "DETAILED PROTOCOL BREAKDOWN")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (sortedProtocols.isNotEmpty()) {
                sortedProtocols.forEachIndexed { index, (protocol, count) ->
                    val percentage = (count / totalPackets * 100).toInt()
                    val delayVal = 100 + index * 50

                    AnimatedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        delay = delayVal,
                        glowColor = protocol.color
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Canvas(modifier = Modifier.size(12.dp)) {
                                    drawCircle(color = protocol.color)
                                }
                                Column {
                                    Text(
                                        text = protocol.displayName,
                                        style = PocketSharkTypography.titleSmall,
                                        color = PocketSharkColors.TextPrimary
                                    )
                                    Text(
                                        text = getProtocolDescription(protocol),
                                        style = PocketSharkTypography.bodySmall,
                                        color = PocketSharkColors.TextTertiary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$count Packets",
                                    style = MonoValueStyle.copy(fontSize = 14.sp),
                                    color = PocketSharkColors.TextPrimary
                                )
                                Text(
                                    text = "$percentage%",
                                    style = PocketSharkTypography.labelSmall,
                                    color = protocol.color
                                )
                            }
                        }
                    }
                }
            } else {
                AnimatedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    delay = 100
                ) {
                    Text(
                        text = "Capture traffic packets to display details.",
                        style = PocketSharkTypography.bodySmall,
                        color = PocketSharkColors.TextTertiary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun getProtocolDescription(protocol: Protocol): String {
    return when (protocol) {
        Protocol.TCP -> "Transmission Control (Port 80/443/etc.)"
        Protocol.UDP -> "User Datagram (Media/Discovery)"
        Protocol.DNS -> "Domain Name System (Port 53 Lookups)"
        Protocol.TLS -> "Transport Layer Security (Encrypted)"
        Protocol.ICMP -> "Internet Control Message (Ping/Errors)"
        Protocol.HTTP -> "Hypertext Transfer (Cleartext Web)"
        Protocol.UNKNOWN -> "Unknown Protocol"
    }
}
