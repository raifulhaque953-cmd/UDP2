package com.pocketshark.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pocketshark.app.data.model.SecurityAlert
import com.pocketshark.app.data.model.ThreatCategory
import com.pocketshark.app.data.model.ThreatSeverity
import com.pocketshark.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Security alert/threat display card with severity indicator and category icon.
 */
@Composable
fun SecurityCard(
    alert: SecurityAlert,
    modifier: Modifier = Modifier
) {
    val severityColor = when (alert.severity) {
        ThreatSeverity.LOW -> PocketSharkColors.SeverityLow
        ThreatSeverity.MEDIUM -> PocketSharkColors.SeverityMedium
        ThreatSeverity.HIGH -> PocketSharkColors.SeverityHigh
        ThreatSeverity.CRITICAL -> PocketSharkColors.SeverityCritical
    }

    val categoryIcon = when (alert.category) {
        ThreatCategory.SUSPICIOUS_DNS -> Icons.Default.Dns
        ThreatCategory.UNENCRYPTED_TRAFFIC -> Icons.Default.LockOpen
        ThreatCategory.PORT_SCAN -> Icons.Default.Radar
        ThreatCategory.UNUSUAL_PROTOCOL -> Icons.Default.Warning
        ThreatCategory.KNOWN_MALICIOUS -> Icons.Default.Dangerous
        ThreatCategory.DATA_EXFILTRATION -> Icons.Default.Upload
        ThreatCategory.CLEARTEXT_CREDENTIALS -> Icons.Default.Key
    }

    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        glowColor = severityColor,
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        // Top row: Icon + Title + Severity
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = alert.category.displayName,
                tint = severityColor,
                modifier = Modifier.size(22.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = PocketSharkTypography.titleSmall,
                    color = PocketSharkColors.TextPrimary
                )
                Text(
                    text = alert.category.displayName,
                    style = PocketSharkTypography.labelSmall,
                    color = PocketSharkColors.TextTertiary
                )
            }

            // Severity badge
            SeverityBadge(severity = alert.severity, color = severityColor)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = alert.description,
            style = PocketSharkTypography.bodySmall,
            color = PocketSharkColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Footer: addresses + timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val addrText = buildString {
                alert.sourceAddress?.let { append(it) }
                alert.destinationAddress?.let { append(" → $it") }
            }
            if (addrText.isNotEmpty()) {
                Text(
                    text = addrText,
                    style = MonoStyle.copy(fontSize = MonoStyle.fontSize * 0.85f),
                    color = PocketSharkColors.TextTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = timeFormat.format(Date(alert.timestamp)),
                style = PocketSharkTypography.labelSmall,
                color = PocketSharkColors.TextTertiary
            )
        }
    }
}

@Composable
private fun SeverityBadge(severity: ThreatSeverity, color: Color) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = severity.displayName.uppercase(),
            style = PocketSharkTypography.labelSmall,
            color = color
        )
    }
}


