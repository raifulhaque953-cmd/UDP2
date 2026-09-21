package com.pocketshark.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketshark.app.ui.components.*
import com.pocketshark.app.ui.theme.*
import com.pocketshark.app.viewmodel.SettingsViewModel

/**
 * Production-grade Settings screen for PocketShark Network Analyzer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observe persisted states
    val mtu by viewModel.mtu.collectAsStateWithLifecycle()
    val primaryDns by viewModel.primaryDns.collectAsStateWithLifecycle()
    val secondaryDns by viewModel.secondaryDns.collectAsStateWithLifecycle()
    val subnet by viewModel.subnet.collectAsStateWithLifecycle()
    val promiscuous by viewModel.promiscuous.collectAsStateWithLifecycle()
    val dnsResolve by viewModel.dnsResolve.collectAsStateWithLifecycle()
    val autoSave by viewModel.autoSave.collectAsStateWithLifecycle()
    val glowIntensity by viewModel.glowIntensity.collectAsStateWithLifecycle()
    val refreshInterval by viewModel.refreshInterval.collectAsStateWithLifecycle()
    val audioAlerts by viewModel.audioAlerts.collectAsStateWithLifecycle()
    val notificationPriority by viewModel.notificationPriority.collectAsStateWithLifecycle()
    val captureProfile by viewModel.captureProfile.collectAsStateWithLifecycle()
    val minAlertSeverity by viewModel.minAlertSeverity.collectAsStateWithLifecycle()
    val reducedMotion by viewModel.reducedMotion.collectAsStateWithLifecycle()

    // Temporary inputs for IP/Text fields validation
    var primaryDnsInput by remember(primaryDns) { mutableStateOf(primaryDns) }
    var secondaryDnsInput by remember(secondaryDns) { mutableStateOf(secondaryDns) }
    var subnetInput by remember(subnet) { mutableStateOf(subnet) }
    var mtuInput by remember(mtu) { mutableStateOf(mtu.toString()) }

    // Toggle & Dropdown local editing states
    var localPromiscuous by remember(promiscuous) { mutableStateOf(promiscuous) }
    var localDnsResolve by remember(dnsResolve) { mutableStateOf(dnsResolve) }
    var localAutoSave by remember(autoSave) { mutableStateOf(autoSave) }
    var localGlowIntensity by remember(glowIntensity) { mutableStateOf(glowIntensity) }
    var localRefreshInterval by remember(refreshInterval) { mutableStateOf(refreshInterval) }
    var localAudioAlerts by remember(audioAlerts) { mutableStateOf(audioAlerts) }
    var localNotificationPriority by remember(notificationPriority) { mutableStateOf(notificationPriority) }
    var localCaptureProfile by remember(captureProfile) { mutableStateOf(captureProfile) }
    var localMinAlertSeverity by remember(minAlertSeverity) { mutableStateOf(minAlertSeverity) }
    var localReducedMotion by remember(reducedMotion) { mutableStateOf(reducedMotion) }

    // Validation error states
    var primaryDnsError by remember { mutableStateOf<String?>(null) }
    var secondaryDnsError by remember { mutableStateOf<String?>(null) }
    var subnetError by remember { mutableStateOf<String?>(null) }
    var mtuError by remember { mutableStateOf<String?>(null) }

    // Dialog state management
    var showResetDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Compute dirty state (has unsaved modifications)
    val hasUnsavedChanges = primaryDnsInput != primaryDns ||
            secondaryDnsInput != secondaryDns ||
            subnetInput != subnet ||
            mtuInput != mtu.toString() ||
            localPromiscuous != promiscuous ||
            localDnsResolve != dnsResolve ||
            localAutoSave != autoSave ||
            localGlowIntensity != glowIntensity ||
            localRefreshInterval != refreshInterval ||
            localAudioAlerts != audioAlerts ||
            localNotificationPriority != notificationPriority ||
            localCaptureProfile != captureProfile ||
            localMinAlertSeverity != minAlertSeverity ||
            localReducedMotion != reducedMotion

    // Intercept back navigation when unsaved changes exist
    val handleBackNavigation = {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    fun validateAndSave(): Boolean {
        var isValid = true

        // Validate Primary DNS
        if (!isValidIp(primaryDnsInput)) {
            primaryDnsError = "Enter a valid DNS server address."
            isValid = false
        } else {
            primaryDnsError = null
        }

        // Validate Secondary DNS
        if (!isValidIp(secondaryDnsInput)) {
            secondaryDnsError = "Enter a valid DNS server address."
            isValid = false
        } else {
            secondaryDnsError = null
        }

        // Validate Subnet Address
        if (!isValidIp(subnetInput)) {
            subnetError = "Enter a valid IPv4 address."
            isValid = false
        } else {
            subnetError = null
        }

        // Validate MTU
        val mtuVal = mtuInput.toIntOrNull()
        if (mtuVal == null || mtuVal !in 576..9000) {
            mtuError = "MTU must be within 576–9000 bytes."
            isValid = false
        } else {
            mtuError = null
        }

        if (isValid) {
            viewModel.setSubnet(subnetInput)
            viewModel.setPrimaryDns(primaryDnsInput)
            viewModel.setSecondaryDns(secondaryDnsInput)
            viewModel.setMtu(mtuVal!!)
            viewModel.setPromiscuous(localPromiscuous)
            viewModel.setDnsResolve(localDnsResolve)
            viewModel.setAutoSave(localAutoSave)
            viewModel.setGlowIntensity(localGlowIntensity)
            viewModel.setRefreshInterval(localRefreshInterval)
            viewModel.setAudioAlerts(localAudioAlerts)
            viewModel.setNotificationPriority(localNotificationPriority)
            viewModel.setCaptureProfile(localCaptureProfile)
            viewModel.setMinAlertSeverity(localMinAlertSeverity)
            viewModel.setReducedMotion(localReducedMotion)
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketSharkColors.BgPrimary)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // ── Top Header Bar ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = handleBackNavigation,
                tint = PocketSharkColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Settings",
                    style = PocketSharkTypography.headlineMedium,
                    color = PocketSharkColors.TextPrimary
                )
            }
            if (hasUnsavedChanges) {
                Text(
                    text = "● Unsaved",
                    style = PocketSharkTypography.labelSmall,
                    color = PocketSharkColors.StatusWarning,
                    modifier = Modifier
                        .background(
                            color = PocketSharkColors.StatusWarning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Section A: NETWORK CONFIGURATION ─────────────────────────────
        GlassSectionHeader(title = "NETWORK CONFIGURATION")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            delay = 50
        ) {
            GlassTextField(
                label = "Subnet Gateway IP Address",
                value = subnetInput,
                onValueChange = { subnetInput = it; subnetError = null },
                errorMessage = subnetError,
                helperText = "Default TUN IPv4 gateway for routing captured traffic"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(
                        label = "Primary DNS Server",
                        value = primaryDnsInput,
                        onValueChange = { primaryDnsInput = it; primaryDnsError = null },
                        errorMessage = primaryDnsError
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(
                        label = "Secondary DNS Server",
                        value = secondaryDnsInput,
                        onValueChange = { secondaryDnsInput = it; secondaryDnsError = null },
                        errorMessage = secondaryDnsError
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                label = "TUN MTU Size",
                value = mtuInput,
                onValueChange = { mtuInput = it; mtuError = null },
                errorMessage = mtuError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                helperText = "Supported range: 576 – 9000 bytes (Default: 1500)"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Restart notice
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PocketSharkColors.AccentCyan.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PocketSharkColors.AccentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Changing network IP/DNS values requires restarting packet capture.",
                    style = PocketSharkTypography.bodySmall.copy(fontSize = 11.sp),
                    color = PocketSharkColors.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Section B: CAPTURE OPTIONS ──────────────────────────────────
        GlassSectionHeader(title = "CAPTURE OPTIONS")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            delay = 100
        ) {
            GlassSwitchRow(
                label = "Promiscuous Mode",
                subtitle = "Capture packets visible to the selected network interface",
                checked = localPromiscuous,
                onCheckedChange = { localPromiscuous = it }
            )

            GlassDivider(modifier = Modifier.padding(vertical = 12.dp), color = PocketSharkColors.GlassHighlight)

            GlassSwitchRow(
                label = "DNS Resolution",
                subtitle = "Resolve IP addresses to hostnames when possible",
                checked = localDnsResolve,
                onCheckedChange = { localDnsResolve = it }
            )

            GlassDivider(modifier = Modifier.padding(vertical = 12.dp), color = PocketSharkColors.GlassHighlight)

            GlassSwitchRow(
                label = "Auto-save PCAP",
                subtitle = "Automatically save captured packets when a capture session ends",
                checked = localAutoSave,
                onCheckedChange = { localAutoSave = it }
            )

            if (localAutoSave) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PocketSharkColors.GlassBgLight,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = PocketSharkColors.AccentEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PCAP Storage Location",
                            style = PocketSharkTypography.labelSmall,
                            color = PocketSharkColors.TextTertiary
                        )
                        Text(
                            text = "PocketShark / Captures",
                            style = MonoStyle.copy(fontSize = 12.sp),
                            color = PocketSharkColors.AccentEmerald
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Section C: PERFORMANCE ──────────────────────────────────────
        GlassSectionHeader(title = "PERFORMANCE")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            delay = 150
        ) {
            GlassDropdownRow(
                label = "Capture Profile",
                subtitle = "Select operational profile for packet parsing depth",
                selectedValue = when (localCaptureProfile) {
                    "Basic" -> "Basic (Low Overhead)"
                    "Deep Analysis" -> "Deep Analysis (High Detail)"
                    else -> "Balanced (Recommended)"
                },
                options = listOf(
                    "Basic (Low Overhead)",
                    "Balanced (Recommended)",
                    "Deep Analysis (High Detail)"
                ),
                onOptionSelected = { selected ->
                    localCaptureProfile = when {
                        selected.startsWith("Basic") -> "Basic"
                        selected.startsWith("Deep") -> "Deep Analysis"
                        else -> "Balanced"
                    }
                }
            )

            GlassDivider(modifier = Modifier.padding(vertical = 12.dp), color = PocketSharkColors.GlassHighlight)

            GlassDropdownRow(
                label = "UI Refresh Frequency",
                subtitle = "Controls how frequently live statistics and UI metrics are refreshed",
                selectedValue = when (localRefreshInterval) {
                    250 -> "250 ms — Very Fast"
                    500 -> "500 ms — Fast"
                    2000 -> "2 sec — Low Power"
                    5000 -> "5 sec — Battery Saver"
                    else -> "1 sec — Balanced"
                },
                options = listOf(
                    "250 ms — Very Fast",
                    "500 ms — Fast",
                    "1 sec — Balanced",
                    "2 sec — Low Power",
                    "5 sec — Battery Saver"
                ),
                onOptionSelected = { option ->
                    localRefreshInterval = when {
                        option.startsWith("250") -> 250
                        option.startsWith("500") -> 500
                        option.startsWith("2 sec") -> 2000
                        option.startsWith("5 sec") -> 5000
                        else -> 1000
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Section D: SECURITY ALERTS ──────────────────────────────────
        GlassSectionHeader(title = "SECURITY ALERTS")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            delay = 200
        ) {
            GlassSwitchRow(
                label = "Audio Threat Warning",
                subtitle = "Play audio alert tone when a threat analysis rule triggers",
                checked = localAudioAlerts,
                onCheckedChange = { localAudioAlerts = it }
            )

            GlassDivider(modifier = Modifier.padding(vertical = 12.dp), color = PocketSharkColors.GlassHighlight)

            GlassDropdownRow(
                label = "Service Notification Priority",
                subtitle = "Determines system status bar visibility during capture",
                selectedValue = localNotificationPriority,
                options = listOf("High", "Low"),
                onOptionSelected = { localNotificationPriority = it }
            )

            GlassDivider(modifier = Modifier.padding(vertical = 12.dp), color = PocketSharkColors.GlassHighlight)

            GlassDropdownRow(
                label = "Alert Severity Threshold",
                subtitle = "Minimum threat severity level required to fire alert notifications",
                selectedValue = localMinAlertSeverity,
                options = listOf("Critical", "High", "Medium", "Low"),
                onOptionSelected = { localMinAlertSeverity = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Section E: APPEARANCE ───────────────────────────────────────
        GlassSectionHeader(title = "APPEARANCE")

        AnimatedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            delay = 250
        ) {
            GlassDropdownRow(
                label = "Glow Effect Level",
                subtitle = "Controls neon glass border glow intensity",
                selectedValue = localGlowIntensity,
                options = listOf("High", "Medium", "Low", "None"),
                onOptionSelected = { localGlowIntensity = it }
            )

            GlassDivider(modifier = Modifier.padding(vertical = 12.dp), color = PocketSharkColors.GlassHighlight)

            GlassSwitchRow(
                label = "Reduced Motion",
                subtitle = "Minimize particle effects and graphic transitions",
                checked = localReducedMotion,
                onCheckedChange = { localReducedMotion = it }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Row: Save / Reset Actions ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                onClick = {
                    if (validateAndSave()) {
                        Toast.makeText(context, "✓ Settings saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Unable to save settings. Check highlighted fields and try again.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.weight(1.3f),
                glowColor = PocketSharkColors.AccentEmerald
            ) {
                Text(
                    text = "Save Settings",
                    style = PocketSharkTypography.labelLarge,
                    color = PocketSharkColors.AccentEmerald
                )
            }

            GlassButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.weight(1f),
                glowColor = PocketSharkColors.StatusDanger
            ) {
                Text(
                    text = "Reset",
                    style = PocketSharkTypography.labelLarge,
                    color = PocketSharkColors.StatusDanger
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Section F: ABOUT / BRAND FOOTER ────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedGlassCard(
                modifier = Modifier.fillMaxWidth(),
                delay = 300
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val R = size.minDimension * 0.45f

                        // Outer Metallic Ring
                        drawCircle(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF22D3EE),
                                    PocketSharkColors.AccentEmerald,
                                    Color(0xFF0A303A)
                                )
                            ),
                            radius = R,
                            center = c,
                            style = Stroke(width = R * 0.14f)
                        )

                        // Central Shark Fin Logo
                        val finPath = Path().apply {
                            moveTo(c.x - R * 0.35f, c.y + R * 0.3f)
                            lineTo(c.x + R * 0.35f, c.y + R * 0.3f)
                            cubicTo(
                                c.x + R * 0.35f, c.y + R * 0.3f,
                                c.x + R * 0.25f, c.y + R * 0.05f,
                                c.x + R * 0.2f, c.y - R * 0.4f
                            )
                            cubicTo(
                                c.x - R * 0.1f, c.y - R * 0.4f,
                                c.x - R * 0.32f, c.y - R * 0.1f,
                                c.x - R * 0.35f, c.y + R * 0.3f
                            )
                            close()
                        }
                        drawPath(
                            path = finPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF22D3EE),
                                    PocketSharkColors.AccentEmerald,
                                    Color(0xFF0F766E)
                                )
                            )
                        )
                    }

                    Text(
                        text = "PocketShark",
                        style = PocketSharkTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = PocketSharkColors.TextPrimary
                    )

                    Text(
                        text = "Network Analyzer",
                        style = PocketSharkTypography.labelSmall,
                        color = PocketSharkColors.AccentCyan
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Version 1.0.0 • Release Build",
                        style = PocketSharkTypography.bodySmall,
                        color = PocketSharkColors.TextTertiary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Secure network capture & analysis engine.",
                        style = PocketSharkTypography.bodySmall.copy(fontSize = 11.sp),
                        color = PocketSharkColors.TextTertiary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // ── Destructive Reset Dialog ───────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset all settings?",
                    style = PocketSharkTypography.titleMedium,
                    color = PocketSharkColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "This will restore PocketShark's default configuration.",
                    style = PocketSharkTypography.bodyMedium,
                    color = PocketSharkColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                        Toast.makeText(context, "✓ Settings restored to defaults", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "Reset",
                        color = PocketSharkColors.StatusDanger,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "Cancel", color = PocketSharkColors.TextSecondary)
                }
            },
            containerColor = PocketSharkColors.BgSecondary,
            titleContentColor = PocketSharkColors.TextPrimary,
            textContentColor = PocketSharkColors.TextSecondary
        )
    }

    // ── Unsaved Changes Confirmation Dialog ───────────────────────────
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = {
                Text(
                    text = "Unsaved changes",
                    style = PocketSharkTypography.titleMedium,
                    color = PocketSharkColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "You have changes that haven't been saved.",
                    style = PocketSharkTypography.bodyMedium,
                    color = PocketSharkColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        onBack()
                    }
                ) {
                    Text(text = "Discard", color = PocketSharkColors.StatusDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text(text = "Stay", color = PocketSharkColors.AccentEmerald)
                }
            },
            containerColor = PocketSharkColors.BgSecondary,
            titleContentColor = PocketSharkColors.TextPrimary,
            textContentColor = PocketSharkColors.TextSecondary
        )
    }
}

// ── Form Components ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    helperText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = PocketSharkTypography.labelSmall,
            color = PocketSharkColors.TextTertiary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MonoStyle.copy(fontSize = 14.sp),
            singleLine = true,
            isError = errorMessage != null,
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PocketSharkColors.TextPrimary,
                unfocusedTextColor = PocketSharkColors.TextPrimary,
                focusedContainerColor = PocketSharkColors.GlassBgLight,
                unfocusedContainerColor = PocketSharkColors.GlassBgLight,
                focusedBorderColor = PocketSharkColors.AccentCyan.copy(alpha = 0.5f),
                unfocusedBorderColor = PocketSharkColors.GlassBorder.copy(alpha = 0.3f),
                errorBorderColor = PocketSharkColors.StatusDanger,
                cursorColor = PocketSharkColors.AccentCyan
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = PocketSharkColors.StatusDanger,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        } else if (helperText != null) {
            Text(
                text = helperText,
                color = PocketSharkColors.TextTertiary.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun GlassSwitchRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = PocketSharkTypography.titleSmall,
                color = PocketSharkColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = PocketSharkTypography.bodySmall,
                color = PocketSharkColors.TextTertiary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PocketSharkColors.BgPrimary,
                checkedTrackColor = PocketSharkColors.AccentEmerald,
                uncheckedThumbColor = PocketSharkColors.TextTertiary,
                uncheckedTrackColor = PocketSharkColors.GlassBgLight
            )
        )
    }
}

@Composable
private fun GlassDropdownRow(
    label: String,
    subtitle: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = PocketSharkTypography.titleSmall,
                color = PocketSharkColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = PocketSharkTypography.bodySmall,
                color = PocketSharkColors.TextTertiary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PocketSharkColors.GlassBgLight)
                    .border(1.dp, PocketSharkColors.GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue,
                    style = PocketSharkTypography.labelMedium,
                    color = PocketSharkColors.AccentCyan
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = PocketSharkColors.TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(PocketSharkColors.BgSecondary)
                    .border(1.dp, PocketSharkColors.GlassBorder, RoundedCornerShape(8.dp))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = PocketSharkTypography.bodyMedium,
                                color = PocketSharkColors.TextPrimary
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun isValidIp(ip: String): Boolean {
    val parts = ip.split(".")
    if (parts.size != 4) return false
    return parts.all { part ->
        val num = part.toIntOrNull()
        num != null && num in 0..255
    }
}
