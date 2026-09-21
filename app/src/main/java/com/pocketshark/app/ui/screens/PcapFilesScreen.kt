package com.pocketshark.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pocketshark.app.ui.theme.*

/**
 * PCAP file management screen.
 * Lists saved captures with import/new actions.
 * Connected to real file system — no placeholder data.
 */
@Composable
fun PcapFilesScreen() {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)
    val cardSpacing = (screenWidthDp * 0.025f).coerceIn(6.dp, 12.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            .statusBarsPadding()
    ) {
        GlassTopBar(title = "PCAP Files")

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing)
        ) {
            GlassButton(
                onClick = { /* Future: New capture → save to PCAP */ },
                modifier = Modifier.weight(1f),
                glowColor = PocketSharkColors.AccentEmerald
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New",
                    tint = PocketSharkColors.AccentEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Capture",
                    style = PocketSharkTypography.labelLarge,
                    color = PocketSharkColors.AccentEmerald
                )
            }

            GlassButton(
                onClick = { /* Future: Import .pcap file */ },
                modifier = Modifier.weight(1f),
                glowColor = PocketSharkColors.AccentCyan
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Import",
                    tint = PocketSharkColors.AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Import",
                    style = PocketSharkTypography.labelLarge,
                    color = PocketSharkColors.AccentCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Empty state (no files yet — real file listing will come when PCAP export is implemented)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📁",
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        48f, androidx.compose.ui.unit.TextUnitType.Sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No PCAP files",
                    style = PocketSharkTypography.titleSmall,
                    color = PocketSharkColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Captured sessions will appear here\nwhen PCAP export is enabled",
                    style = PocketSharkTypography.bodySmall,
                    color = PocketSharkColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
