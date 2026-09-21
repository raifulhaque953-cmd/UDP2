package com.pocketshark.app.data.model

/**
 * Statistics for the current or past capture session.
 */
data class CaptureStats(
    val packetCount: Long = 0,
    val totalBytes: Long = 0,
    val tcpCount: Long = 0,
    val udpCount: Long = 0,
    val dnsCount: Long = 0,
    val icmpCount: Long = 0,
    val tlsCount: Long = 0,
    val httpCount: Long = 0,
    val errorCount: Long = 0,
    val startTime: Long = 0L,
    val packetsPerSecond: Float = 0f,
    val bytesPerSecond: Float = 0f
) {
    /** Duration of capture in milliseconds */
    val durationMs: Long
        get() = if (startTime > 0) System.currentTimeMillis() - startTime else 0

    /** Formatted total bytes as human-readable string */
    val formattedBytes: String
        get() = when {
            totalBytes >= 1_073_741_824 -> String.format("%.1f GB", totalBytes / 1_073_741_824.0)
            totalBytes >= 1_048_576 -> String.format("%.1f MB", totalBytes / 1_048_576.0)
            totalBytes >= 1_024 -> String.format("%.1f KB", totalBytes / 1_024.0)
            else -> "$totalBytes B"
        }

    /** Formatted duration as HH:MM:SS */
    val formattedDuration: String
        get() {
            val seconds = durationMs / 1000
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
            else String.format("%02d:%02d", m, s)
        }

    /** Protocol distribution map for charts */
    val protocolDistribution: Map<Protocol, Long>
        get() = mapOf(
            Protocol.TCP to tcpCount,
            Protocol.UDP to udpCount,
            Protocol.DNS to dnsCount,
            Protocol.TLS to tlsCount,
            Protocol.ICMP to icmpCount,
            Protocol.HTTP to httpCount
        ).filter { it.value > 0 }
}

/**
 * Metadata for a saved PCAP file.
 */
data class PcapFile(
    val id: String,
    val name: String,
    val path: String,
    val createdAt: Long,
    val sizeBytes: Long,
    val packetCount: Int,
    val durationMs: Long
) {
    /** Formatted file size */
    val formattedSize: String
        get() = when {
            sizeBytes >= 1_048_576 -> String.format("%.1f MB", sizeBytes / 1_048_576.0)
            sizeBytes >= 1_024 -> String.format("%.1f KB", sizeBytes / 1_024.0)
            else -> "$sizeBytes B"
        }
}
