package com.pocketshark.app.capture

import com.pocketshark.app.data.model.CaptureStats
import com.pocketshark.app.data.model.Packet
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the contract for a packet capture backend.
 *
 * The primary implementation is [VpnCaptureService] which uses Android's
 * VpnService to intercept network traffic through a TUN interface.
 * This interface allows for alternative capture backends in the future
 * (e.g., root-based tcpdump, USB tethering capture).
 */
interface CaptureEngine {
    /** Current state of the capture engine */
    val state: StateFlow<CaptureState>

    /** Hot flow of captured and parsed packets */
    val packets: SharedFlow<Packet>

    /** Live aggregate statistics */
    val stats: StateFlow<CaptureStats>

    /** Start packet capture. May require VPN consent first. */
    suspend fun start()

    /** Stop packet capture and release resources. */
    suspend fun stop()
}
