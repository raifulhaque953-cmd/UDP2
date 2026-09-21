package com.pocketshark.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketshark.app.ui.components.*
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.AnalyticsViewModel

/**
 * Analytics screen with traffic graph, protocol donut chart, and top conversations.
 */
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val protocolDist by viewModel.protocolDistribution.collectAsStateWithLifecycle()
    val trafficHistory by viewModel.trafficHistory.collectAsStateWithLifecycle()
    val topConversations by viewModel.topConversations.collectAsStateWithLifecycle()
    val totalBytes by viewModel.totalBytesFormatted.collectAsStateWithLifecycle()
    val pps by viewModel.packetsPerSecond.collectAsStateWithLifecycle()
    val topIps by viewModel.topDestinationIps.collectAsStateWithLifecycle()
    val topPorts by viewModel.topDestinationPorts.collectAsStateWithLifecycle()
    val topDnsDomains by viewModel.topDnsDomains.collectAsStateWithLifecycle()

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)
    val cardSpacing = (screenWidthDp * 0.025f).coerceIn(6.dp, 12.dp)
    val donutChartSize = ((screenWidthDp - horizontalPadding * 2) * 0.38f)
        .coerceIn(100.dp, 170.dp)
        .value.toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 80.dp)
    ) {
        GlassTopBar(title = "Analytics")

        // ── Summary Stats ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing)
        ) {
            StatCard(
                label = "Total Data",
                value = totalBytes,
                icon = Icons.Default.DataUsage,
                iconTint = PocketSharkColors.AccentCyan,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Rate",
                value = "$pps/s",
                icon = Icons.Default.Speed,
                iconTint = PocketSharkColors.AccentLime,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Traffic Graph ──────────────────────────────────────────────
        GlassSectionHeader(title = "Traffic Over Time")

        if (trafficHistory.size >= 2) {
            AnimatedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                delay = 100
            ) {
                TrafficGraph(
                    dataPoints = trafficHistory,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            EmptyChartPlaceholder(
                text = "Capture traffic to see the graph",
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Protocol Distribution ──────────────────────────────────────
        GlassSectionHeader(title = "Protocol Distribution")

        if (protocolDist.isNotEmpty()) {
            AnimatedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                delay = 200
            ) {
                DonutChart(
                    protocolData = protocolDist,
                    chartSize = donutChartSize
                )
            }
        } else {
            EmptyChartPlaceholder(
                text = "No protocol data yet",
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Top Destination IPs ────────────────────────────────────────
        GlassSectionHeader(title = "Top Destination IPs")
        if (topIps.isNotEmpty()) {
            AnimatedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                delay = 250
            ) {
                topIps.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}",
                                style = PocketSharkTypography.labelSmall,
                                color = PocketSharkColors.TextTertiary,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                text = item.ip,
                                style = MonoStyle,
                                color = PocketSharkColors.TextPrimary
                            )
                        }
                        Text(
                            text = "${item.packetCount} pkts (${item.byteCount / 1024} KB)",
                            style = MonoStyle.copy(fontSize = 11.sp),
                            color = PocketSharkColors.TextSecondary
                        )
                    }
                    if (index < topIps.lastIndex) {
                        GlassDivider(color = PocketSharkColors.GlassHighlight)
                    }
                }
            }
        } else {
            EmptyChartPlaceholder(
                text = "No destination IPs captured yet",
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Top Destination Ports ──────────────────────────────────────
        GlassSectionHeader(title = "Top Service Ports")
        if (topPorts.isNotEmpty()) {
            AnimatedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                delay = 280
            ) {
                topPorts.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}",
                                style = PocketSharkTypography.labelSmall,
                                color = PocketSharkColors.TextTertiary,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                text = "${item.port} (${item.serviceName})",
                                style = MonoStyle,
                                color = PocketSharkColors.TextPrimary
                            )
                        }
                        Text(
                            text = "${item.packetCount} pkts",
                            style = MonoStyle.copy(fontSize = 11.sp),
                            color = PocketSharkColors.AccentEmerald
                        )
                    }
                    if (index < topPorts.lastIndex) {
                        GlassDivider(color = PocketSharkColors.GlassHighlight)
                    }
                }
            }
        } else {
            EmptyChartPlaceholder(
                text = "No port data captured yet",
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── DNS Domains Query Activity ─────────────────────────────────
        if (topDnsDomains.isNotEmpty()) {
            GlassSectionHeader(title = "Top DNS Queries")
            AnimatedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                delay = 300
            ) {
                topDnsDomains.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${index + 1}",
                                style = PocketSharkTypography.labelSmall,
                                color = PocketSharkColors.TextTertiary,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                text = item.domain,
                                style = MonoStyle.copy(fontSize = 11.sp),
                                color = PocketSharkColors.TextPrimary,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "${item.queryCount} queries",
                            style = MonoStyle.copy(fontSize = 11.sp),
                            color = PocketSharkColors.AccentCyan
                        )
                    }
                    if (index < topDnsDomains.lastIndex) {
                        GlassDivider(color = PocketSharkColors.GlassHighlight)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Top Conversations ──────────────────────────────────────────
        GlassSectionHeader(title = "Top Conversations")

        if (topConversations.isNotEmpty()) {
            AnimatedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                delay = 320
            ) {
                topConversations.forEachIndexed { index, conn ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = PocketSharkTypography.labelSmall,
                            color = PocketSharkColors.TextTertiary,
                            modifier = Modifier.width(20.dp)
                        )
                        ProtocolBadge(protocol = conn.protocol)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${conn.sourceAddress}:${conn.sourcePort}",
                                style = MonoStyle.copy(fontSize = MonoStyle.fontSize * 0.85f),
                                color = PocketSharkColors.TextPrimary
                            )
                            Text(
                                text = "↔ ${conn.destinationAddress}:${conn.destinationPort}",
                                style = MonoStyle.copy(fontSize = MonoStyle.fontSize * 0.85f),
                                color = PocketSharkColors.TextTertiary
                            )
                        }
                        Text(
                            text = "${conn.packetCount} pkts",
                            style = MonoStyle,
                            color = PocketSharkColors.AccentEmerald
                        )
                    }
                    if (index < topConversations.lastIndex) {
                        GlassDivider(color = PocketSharkColors.GlassHighlight)
                    }
                }
            }
        } else {
            EmptyChartPlaceholder(
                text = "No conversations tracked yet",
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun EmptyChartPlaceholder(text: String, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 14.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = PocketSharkTypography.bodySmall,
                color = PocketSharkColors.TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}
