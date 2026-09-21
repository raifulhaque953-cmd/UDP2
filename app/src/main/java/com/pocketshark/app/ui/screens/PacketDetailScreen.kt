package com.pocketshark.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.pocketshark.app.capture.PacketParser
import com.pocketshark.app.data.model.*
import com.pocketshark.app.ui.components.ProtocolBadge
import com.pocketshark.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Packet detail screen showing parsed protocol headers in collapsible glass sections.
 */
@Composable
fun PacketDetailScreen(
    packet: Packet?,
    onBack: () -> Unit
) {
    if (packet == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(PocketSharkColors.BgPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text("Packet not found", color = PocketSharkColors.TextSecondary)
        }
        return
    }

    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 20.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = onBack,
                tint = PocketSharkColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Packet #${packet.id}",
                style = PocketSharkTypography.headlineSmall
            )
        }

        // ── Header Card ────────────────────────────────────────────────
        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProtocolBadge(protocol = packet.protocol)
                Text(
                    text = "${packet.length} bytes",
                    style = MonoStyle,
                    color = PocketSharkColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Source → Destination
            Text(
                text = buildString {
                    append(packet.sourceAddress)
                    packet.sourcePort?.let { append(":$it") }
                    append("  →  ")
                    append(packet.destinationAddress)
                    packet.destinationPort?.let { append(":$it") }
                },
                style = MonoStyle,
                color = PocketSharkColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = timeFormat.format(Date(packet.timestamp)),
                style = PocketSharkTypography.bodySmall,
                color = PocketSharkColors.TextTertiary
            )

            if (packet.info.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = packet.info,
                    style = PocketSharkTypography.bodySmall,
                    color = PocketSharkColors.AccentEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Packet Intelligence Card ───────────────────────────────────
        val isOutbound = packet.sourceAddress.startsWith("10.0.") || packet.sourceAddress == "127.0.0.1"
        val dstPort = packet.destinationPort
        val isCleartextPort = dstPort != null && dstPort in setOf(80, 21, 23, 25, 110, 143) && packet.protocol == Protocol.TCP
        val isLongDns = packet.dnsData?.queries?.any { it.name.length > 60 } == true

        CollapsibleSection(
            title = "⚡ Packet Intelligence",
            defaultExpanded = true,
            horizontalPadding = horizontalPadding
        ) {
            DetailField("Traffic Direction", if (isOutbound) "Outbound (Device → Network)" else "Inbound (Network → Device)")
            DetailField("Protocol Flow", "${packet.protocol.displayName} (${packet.sourceAddress} → ${packet.destinationAddress})")
            
            packet.sourcePort?.let { sp ->
                packet.destinationPort?.let { dp ->
                    DetailField("Port Mapping", "Src Port $sp  →  Dst Port $dp")
                }
            }

            packet.tcpHeader?.let { tcp ->
                val flagNames = tcp.flags.joinToString(", ") { it.name }
                DetailField("TCP Flags", if (flagNames.isNotEmpty()) flagNames else "None")
                DetailField("TCP Window", "${tcp.windowSize} bytes")
            }

            val headerLen = packet.ipHeader?.headerLength ?: 20
            val payloadLen = (packet.length - headerLen).coerceAtLeast(0)
            DetailField("Payload Structure", "${packet.length} bytes total ($headerLen B header, $payloadLen B payload)")

            // Threat Assessment based strictly on actual engine rules
            val threatText = when {
                isCleartextPort -> "⚠️ Medium Severity — Unencrypted protocol detected on port $dstPort"
                isLongDns -> "⚠️ High Severity — Suspiciously long DNS query domain (>60 chars)"
                else -> "✅ Clean — No security threat detected in packet headers"
            }
            DetailField("Security Status", threatText)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // IP Header
        packet.ipHeader?.let { ip ->
            CollapsibleSection(
                title = "IPv${ip.version} Header",
                defaultExpanded = true,
                horizontalPadding = horizontalPadding
            ) {
                DetailField("Version", "${ip.version}")
                DetailField("Header Length", "${ip.headerLength} bytes")
                DetailField("Total Length", "${ip.totalLength} bytes")
                DetailField("Identification", "0x${ip.identification.toString(16).uppercase()}")
                DetailField("Flags", "0x${ip.flags.toString(16).uppercase()}")
                DetailField("Fragment Offset", "${ip.fragmentOffset}")
                DetailField("TTL", "${ip.ttl}")
                DetailField("Protocol", "${ip.protocol}")
                DetailField("Header Checksum", "0x${ip.checksum.toString(16).uppercase()}")
                DetailField("Source", ip.sourceAddress)
                DetailField("Destination", ip.destinationAddress)
            }
        }

        // TCP Header
        packet.tcpHeader?.let { tcp ->
            CollapsibleSection(title = "TCP Header", horizontalPadding = horizontalPadding) {
                DetailField("Source Port", "${tcp.sourcePort}")
                DetailField("Destination Port", "${tcp.destinationPort}")
                DetailField("Sequence Number", "${tcp.sequenceNumber}")
                DetailField("Acknowledgment", "${tcp.acknowledgmentNumber}")
                DetailField("Data Offset", "${tcp.dataOffset} bytes")
                DetailField("Flags", tcp.flags.joinToString(", ") { it.name })
                DetailField("Window Size", "${tcp.windowSize}")
                DetailField("Checksum", "0x${tcp.checksum.toString(16).uppercase()}")
                DetailField("Urgent Pointer", "${tcp.urgentPointer}")
            }
        }

        // UDP Header
        packet.udpHeader?.let { udp ->
            CollapsibleSection(title = "UDP Header", horizontalPadding = horizontalPadding) {
                DetailField("Source Port", "${udp.sourcePort}")
                DetailField("Destination Port", "${udp.destinationPort}")
                DetailField("Length", "${udp.length} bytes")
                DetailField("Checksum", "0x${udp.checksum.toString(16).uppercase()}")
            }
        }

        // ICMP Header
        packet.icmpHeader?.let { icmp ->
            CollapsibleSection(title = "ICMP Header", horizontalPadding = horizontalPadding) {
                DetailField("Type", "${icmp.type}")
                DetailField("Code", "${icmp.code}")
                DetailField("Checksum", "0x${icmp.checksum.toString(16).uppercase()}")
                DetailField("Identifier", "${icmp.id}")
                DetailField("Sequence", "${icmp.sequence}")
            }
        }

        // DNS Data
        packet.dnsData?.let { dns ->
            CollapsibleSection(
                title = "DNS ${if (dns.isResponse) "Response" else "Query"}",
                horizontalPadding = horizontalPadding
            ) {
                DetailField("Transaction ID", "0x${dns.transactionId.toString(16).uppercase()}")
                DetailField("Questions", "${dns.questionCount}")
                DetailField("Answers", "${dns.answerCount}")
                dns.queries.forEach { query ->
                    DetailField("Query", "${query.name} (Type ${query.type})")
                }
            }
        }

        // Raw Payload (Hex Dump)
        CollapsibleSection(
            title = "Raw Data (${packet.rawData.size} bytes)",
            horizontalPadding = horizontalPadding
        ) {
            Text(
                text = PacketParser.toHexDump(packet.rawData),
                style = MonoStyle.copy(fontSize = MonoStyle.fontSize * 0.85f),
                color = PocketSharkColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ── Collapsible Glass Section ──────────────────────────────────────────

@Composable
private fun CollapsibleSection(
    title: String,
    defaultExpanded: Boolean = false,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 4.dp),
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = PocketSharkTypography.titleSmall,
                color = PocketSharkColors.AccentEmerald,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = PocketSharkColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 14.dp, end = 14.dp, bottom = 14.dp
                )
            ) {
                GlassDivider(
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = PocketSharkColors.GlassHighlight
                )
                content()
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = PocketSharkTypography.bodySmall,
            color = PocketSharkColors.TextTertiary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MonoStyle.copy(fontSize = MonoStyle.fontSize * 0.9f),
            color = PocketSharkColors.TextPrimary,
            modifier = Modifier.weight(0.6f)
        )
    }
}
