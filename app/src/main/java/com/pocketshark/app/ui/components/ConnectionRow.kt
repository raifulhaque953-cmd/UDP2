package com.pocketshark.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pocketshark.app.data.model.Connection
import com.pocketshark.app.data.model.ConnectionState
import com.pocketshark.app.ui.theme.*

/**
 * Connection info card showing endpoints, protocol, state, and traffic volume.
 */
@Composable
fun ConnectionRow(
    connection: Connection,
    modifier: Modifier = Modifier
) {
    val stateColor = when (connection.state) {
        ConnectionState.ESTABLISHED, ConnectionState.ACTIVE -> PocketSharkColors.ConnEstablished
        ConnectionState.SYN_SENT, ConnectionState.SYN_RECEIVED -> PocketSharkColors.ConnSyn
        ConnectionState.FIN_WAIT, ConnectionState.CLOSE_WAIT -> PocketSharkColors.ConnFin
        ConnectionState.CLOSED -> PocketSharkColors.ConnClosed
        ConnectionState.UNKNOWN -> PocketSharkColors.TextTertiary
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        // Top: Protocol + State
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProtocolBadge(protocol = connection.protocol)
            StatusIndicator(
                label = connection.state.displayName,
                color = stateColor,
                pulsing = connection.state == ConnectionState.ESTABLISHED ||
                        connection.state == ConnectionState.ACTIVE
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Middle: Source ↔ Destination
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${connection.sourceAddress}:${connection.sourcePort}",
                style = MonoStyle,
                color = PocketSharkColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = PocketSharkColors.TextTertiary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${connection.destinationAddress}:${connection.destinationPort}",
                style = MonoStyle,
                color = PocketSharkColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom: Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${connection.packetCount} packets",
                style = PocketSharkTypography.labelSmall,
                color = PocketSharkColors.TextTertiary
            )
            Text(
                text = formatBytes(connection.bytesOut + connection.bytesIn),
                style = PocketSharkTypography.labelSmall,
                color = PocketSharkColors.TextTertiary
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}
