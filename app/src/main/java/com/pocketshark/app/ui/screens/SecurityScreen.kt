package com.pocketshark.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketshark.app.data.model.ThreatCategory
import com.pocketshark.app.data.model.ThreatSeverity
import com.pocketshark.app.ui.components.*
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.SecurityViewModel

/**
 * Security dashboard with risk overview, threat categories, and alert list.
 */
@Composable
fun SecurityScreen(
    viewModel: SecurityViewModel = viewModel()
) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val severityCounts by viewModel.severityCounts.collectAsStateWithLifecycle()
    val categoryCounts by viewModel.categoryCounts.collectAsStateWithLifecycle()
    val totalAlerts by viewModel.totalAlerts.collectAsStateWithLifecycle()
    val highestSeverity by viewModel.highestSeverity.collectAsStateWithLifecycle()

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidthDp * 0.05f).coerceIn(12.dp, 24.dp)
    val cardSpacing = (screenWidthDp * 0.02f).coerceIn(6.dp, 10.dp)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            bottom = 100.dp + WindowInsets.navigationBars
                .asPaddingValues().calculateBottomPadding()
        )
    ) {
        item {
            GlassTopBar(title = "Security")
        }

        // ── Risk Overview Card ─────────────────────────────────────────
        item {
            val overviewGlow = when (highestSeverity) {
                ThreatSeverity.CRITICAL -> PocketSharkColors.SeverityCritical
                ThreatSeverity.HIGH -> PocketSharkColors.SeverityHigh
                ThreatSeverity.MEDIUM -> PocketSharkColors.SeverityMedium
                else -> PocketSharkColors.AccentDeepGreen
            }

            AnimatedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                glowColor = overviewGlow
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (totalAlerts == 0) "No Threats Detected" else "$totalAlerts Threat Alerts",
                            style = PocketSharkTypography.headlineSmall,
                            color = PocketSharkColors.TextPrimary
                        )
                        Text(
                            text = if (totalAlerts == 0) "Heuristic Security Engine Active • All traffic monitored"
                            else "Review detected security alerts below",
                            style = PocketSharkTypography.bodySmall,
                            color = PocketSharkColors.TextSecondary
                        )
                    }

                    Icon(
                        imageVector = if (totalAlerts == 0) Icons.Default.VerifiedUser
                        else Icons.Default.GppMaybe,
                        contentDescription = null,
                        tint = overviewGlow,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                // Severity counts grid (always visible)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ThreatSeverity.values().reversed().forEach { severity ->
                        val count = severityCounts[severity] ?: 0
                        val color = when (severity) {
                            ThreatSeverity.CRITICAL -> PocketSharkColors.SeverityCritical
                            ThreatSeverity.HIGH -> PocketSharkColors.SeverityHigh
                            ThreatSeverity.MEDIUM -> PocketSharkColors.SeverityMedium
                            ThreatSeverity.LOW -> PocketSharkColors.SeverityLow
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$count",
                                style = MonoValueStyle.copy(
                                    fontSize = MonoValueStyle.fontSize * 0.85f
                                ),
                                color = if (count > 0) color else PocketSharkColors.TextTertiary
                            )
                            Text(
                                text = severity.displayName,
                                style = PocketSharkTypography.labelSmall,
                                color = if (count > 0) color else PocketSharkColors.TextTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Threat Categories ──────────────────────────────────────────
        if (categoryCounts.isNotEmpty()) {
            item {
                GlassSectionHeader(title = "Threat Categories")
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(cardSpacing)
                ) {
                    categoryCounts.entries.take(3).forEach { (category, count) ->
                        val catIcon = when (category) {
                            ThreatCategory.SUSPICIOUS_DNS -> Icons.Default.Dns
                            ThreatCategory.UNENCRYPTED_TRAFFIC -> Icons.Default.LockOpen
                            ThreatCategory.PORT_SCAN -> Icons.Default.Radar
                            ThreatCategory.UNUSUAL_PROTOCOL -> Icons.Default.Warning
                            ThreatCategory.KNOWN_MALICIOUS -> Icons.Default.Dangerous
                            ThreatCategory.DATA_EXFILTRATION -> Icons.Default.Upload
                            ThreatCategory.CLEARTEXT_CREDENTIALS -> Icons.Default.Key
                        }

                        GlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 14.dp,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = category.displayName,
                                tint = PocketSharkColors.StatusWarning,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$count",
                                style = MonoValueStyle.copy(
                                    fontSize = MonoValueStyle.fontSize * 0.8f
                                ),
                                color = PocketSharkColors.TextPrimary
                            )
                            Text(
                                text = category.displayName,
                                style = PocketSharkTypography.labelSmall,
                                color = PocketSharkColors.TextTertiary,
                                maxLines = 2
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Alert List ─────────────────────────────────────────────────
        if (alerts.isNotEmpty()) {
            item {
                GlassSectionHeader(title = "Recent Alerts")
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(items = alerts, key = { it.id }) { alert ->
                SecurityCard(
                    alert = alert,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp)
                )
            }
        }

        // ── Empty State ────────────────────────────────────────────────
        if (totalAlerts == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🛡️",
                            fontSize = androidx.compose.ui.unit.TextUnit(
                                48f, androidx.compose.ui.unit.TextUnitType.Sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All clear",
                            style = PocketSharkTypography.titleSmall,
                            color = PocketSharkColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Security analysis runs during capture",
                            style = PocketSharkTypography.bodySmall,
                            color = PocketSharkColors.TextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
