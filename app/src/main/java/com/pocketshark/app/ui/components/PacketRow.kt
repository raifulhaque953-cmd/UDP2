package com.pocketshark.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketshark.app.data.model.Packet
import com.pocketshark.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Compact packet list item showing timestamp, protocol badge, addresses, and info summary.
 */
@Composable
fun PacketRow(
    packet: Packet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val isOutbound = packet.sourceAddress.startsWith("10.0.") || packet.sourceAddress == "127.0.0.1"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Direction badge (OUT / IN)
        Text(
            text = if (isOutbound) "OUT" else "IN ",
            style = MonoStyle.copy(
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = if (isOutbound) PocketSharkColors.AccentCyan else PocketSharkColors.AccentEmerald,
            modifier = Modifier
                .background(
                    color = (if (isOutbound) PocketSharkColors.AccentCyan else PocketSharkColors.AccentEmerald).copy(alpha = 0.12f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )

        // Timestamp
        Text(
            text = timeFormat.format(Date(packet.timestamp)),
            style = MonoStyle.copy(fontSize = 11.sp),
            color = PocketSharkColors.TextTertiary,
            modifier = Modifier.width(82.dp)
        )

        // Protocol badge
        ProtocolBadge(protocol = packet.protocol)

        // Source → Destination + Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = buildAddressString(packet),
                style = MonoStyle,
                color = PocketSharkColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (packet.info.isNotEmpty()) {
                Text(
                    text = packet.info,
                    style = PocketSharkTypography.bodySmall,
                    color = PocketSharkColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Packet size
        Text(
            text = "${packet.length} B",
            style = MonoStyle.copy(fontSize = 11.sp),
            color = PocketSharkColors.TextSecondary
        )
    }
}

private fun buildAddressString(packet: Packet): String {
    val src = if (packet.sourcePort != null) "${packet.sourceAddress}:${packet.sourcePort}"
    else packet.sourceAddress
    val dst = if (packet.destinationPort != null) "${packet.destinationAddress}:${packet.destinationPort}"
    else packet.destinationAddress
    return "$src → $dst"
}


