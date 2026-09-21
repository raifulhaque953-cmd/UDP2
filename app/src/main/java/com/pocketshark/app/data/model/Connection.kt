package com.pocketshark.app.data.model

/**
 * Represents a tracked network connection between two endpoints.
 */
data class Connection(
    val id: String,
    val protocol: Protocol,
    val sourceAddress: String,
    val sourcePort: Int,
    val destinationAddress: String,
    val destinationPort: Int,
    val state: ConnectionState,
    val bytesIn: Long,
    val bytesOut: Long,
    val packetCount: Int,
    val startTime: Long,
    val lastActivity: Long
) {
    /** Human-readable connection key */
    val key: String
        get() = "$sourceAddress:$sourcePort ↔ $destinationAddress:$destinationPort"
}

/** TCP/UDP connection lifecycle states */
enum class ConnectionState(val displayName: String) {
    SYN_SENT("SYN Sent"),
    SYN_RECEIVED("SYN Received"),
    ESTABLISHED("Established"),
    FIN_WAIT("FIN Wait"),
    CLOSE_WAIT("Close Wait"),
    CLOSED("Closed"),
    ACTIVE("Active"),
    UNKNOWN("Unknown")
}
