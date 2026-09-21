package com.pocketshark.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketshark.app.ui.components.*
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.ConnectionTab
import com.pocketshark.app.viewmodel.ConnectionsViewModel

/**
 * Connections screen with tab bar and connection list.
 */
@Composable
fun ConnectionsScreen(
    viewModel: ConnectionsViewModel = viewModel()
) {
    val connections by viewModel.connections.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            .statusBarsPadding()
    ) {
        GlassTopBar(title = "Connections")

        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

        // Filter bar
        FilterBar(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.padding(horizontal = horizontalPadding),
            placeholder = "Search connection by IP, port…"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tab bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConnectionTab.entries.forEach { tab ->
                FilterChip(
                    label = tab.displayName,
                    selected = tab == selectedTab,
                    onClick = { viewModel.selectTab(tab) },
                    color = PocketSharkColors.AccentEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$totalCount total connections",
            style = PocketSharkTypography.labelSmall,
            color = PocketSharkColors.TextTertiary,
            modifier = Modifier.padding(horizontal = horizontalPadding)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (connections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔗",
                        fontSize = androidx.compose.ui.unit.TextUnit(48f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No connections tracked",
                        style = PocketSharkTypography.titleSmall,
                        color = PocketSharkColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Start capturing to monitor connections",
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
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = 100.dp + WindowInsets.navigationBars
                        .asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = connections,
                    key = { it.id }
                ) { connection ->
                    ConnectionRow(connection = connection)
                }
            }
        }
    }
}
