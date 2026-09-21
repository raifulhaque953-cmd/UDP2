package com.pocketshark.app.data.model

/**
 * Represents a security alert generated from packet analysis.
 */
data class SecurityAlert(
    val id: Long,
    val timestamp: Long,
    val severity: ThreatSeverity,
    val category: ThreatCategory,
    val title: String,
    val description: String,
    val relatedPacketIds: List<Long>,
    val sourceAddress: String?,
    val destinationAddress: String?
)

/** Threat severity levels */
enum class ThreatSeverity(val displayName: String, val weight: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

/** Categories of security threats detected during packet analysis */
enum class ThreatCategory(val displayName: String, val icon: String) {
    SUSPICIOUS_DNS("Suspicious DNS", "dns"),
    UNENCRYPTED_TRAFFIC("Unencrypted Traffic", "lock_open"),
    PORT_SCAN("Port Scanning", "radar"),
    UNUSUAL_PROTOCOL("Unusual Protocol", "warning"),
    KNOWN_MALICIOUS("Known Malicious", "dangerous"),
    DATA_EXFILTRATION("Data Exfiltration", "upload"),
    CLEARTEXT_CREDENTIALS("Cleartext Credentials", "key")
}
