package com.pocketshark.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pocketshark.app.data.model.Protocol
import com.pocketshark.app.ui.components.*
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.PacketsViewModel

/**
 * Packet list screen with text filter, protocol chips, and scrollable packet list.
 */
@Composable
fun PacketsScreen(
    viewModel: PacketsViewModel = viewModel(),
    onPacketClick: (Long) -> Unit
) {
    val packets by viewModel.filteredPackets.collectAsStateWithLifecycle()
    val filterText by viewModel.filterText.collectAsStateWithLifecycle()
    val selectedProtocols by viewModel.selectedProtocols.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalPacketCount.collectAsStateWithLifecycle()

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            .statusBarsPadding()
    ) {
        GlassTopBar(
            title = "Packets",
            actions = {
                if (selectedProtocols.isNotEmpty() || filterText.isNotEmpty()) {
                    GlassIconButton(
                        icon = Icons.Default.FilterListOff,
                        onClick = { viewModel.clearFilters() },
                        tint = PocketSharkColors.AccentEmerald
                    )
                }
            }
        )

        // Filter bar
        FilterBar(
            value = filterText,
            onValueChange = { viewModel.updateFilterText(it) },
            modifier = Modifier.padding(horizontal = horizontalPadding),
            placeholder = "Filter by IP, port, protocol…"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Protocol filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val protocols = listOf(
                Protocol.TCP, Protocol.UDP, Protocol.DNS,
                Protocol.TLS, Protocol.ICMP, Protocol.HTTP
            )
            items(protocols) { protocol ->
                FilterChip(
                    label = protocol.displayName,
                    selected = protocol in selectedProtocols,
                    onClick = { viewModel.toggleProtocolFilter(protocol) },
                    color = protocol.color
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Count indicator + Sort controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${packets.size} of $totalCount packets",
                style = PocketSharkTypography.labelSmall,
                color = PocketSharkColors.TextTertiary
            )

            // Sort dropdown chips
            val currentSort by viewModel.sortOrder.collectAsStateWithLifecycle()
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                com.pocketshark.app.viewmodel.PacketSortOrder.values().forEach { order ->
                    val isSelected = order == currentSort
                    Text(
                        text = when (order) {
                            com.pocketshark.app.viewmodel.PacketSortOrder.NEWEST_FIRST -> "Newest"
                            com.pocketshark.app.viewmodel.PacketSortOrder.OLDEST_FIRST -> "Oldest"
                            com.pocketshark.app.viewmodel.PacketSortOrder.SIZE_DESC -> "Size"
                            com.pocketshark.app.viewmodel.PacketSortOrder.PROTOCOL -> "Proto"
                        },
                        style = PocketSharkTypography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) PocketSharkColors.AccentEmerald else PocketSharkColors.TextTertiary,
                        modifier = Modifier
                            .background(
                                color = if (isSelected) PocketSharkColors.GlassBorderActive else androidx.compose.ui.graphics.Color.Transparent,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            )
                            .clickable { viewModel.setSortOrder(order) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        GlassDivider(modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp))

        // Packet list
        if (packets.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📡",
                        fontSize = androidx.compose.ui.unit.TextUnit(48f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (totalCount == 0L) "No packets captured"
                        else "No packets match your filters",
                        style = PocketSharkTypography.titleSmall,
                        color = PocketSharkColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (totalCount == 0L) "Start a capture to see network traffic"
                        else "Try adjusting your filter criteria",
                        style = PocketSharkTypography.bodySmall,
                        color = PocketSharkColors.TextTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 100.dp + WindowInsets.navigationBars
                        .asPaddingValues().calculateBottomPadding()
                )
            ) {
                items(
                    items = packets,
                    key = { it.id }
                ) { packet ->
                    PacketRow(
                        packet = packet,
                        onClick = { onPacketClick(packet.id) }
                    )
                    GlassDivider(
                        modifier = Modifier.padding(horizontal = horizontalPadding),
                        color = PocketSharkColors.GlassHighlight
                    )
                }
            }
        }
    }
}
