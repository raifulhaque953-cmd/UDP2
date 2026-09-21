package com.pocketshark.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pocketshark.app.data.model.Protocol
import com.pocketshark.app.ui.theme.PocketSharkTypography

/**
 * Colored protocol tag badge (TCP, UDP, DNS, TLS, ICMP, HTTP).
 */
@Composable
fun ProtocolBadge(
    protocol: Protocol,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(protocol.color.copy(alpha = 0.15f))
            .border(1.dp, protocol.color.copy(alpha = 0.3f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = protocol.displayName,
            style = PocketSharkTypography.labelSmall,
            color = protocol.color
        )
    }
}
