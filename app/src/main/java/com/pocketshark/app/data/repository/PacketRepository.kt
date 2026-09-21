package com.pocketshark.app.data.repository

import com.pocketshark.app.data.model.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for all captured packet data.
 * Collects packets from the capture engine and exposes reactive flows
 * for the UI layer (ViewModels → Screens).
 */
interface PacketRepository {

    /** All captured packets (newest first), capped at buffer limit */
    val packets: StateFlow<List<Packet>>

    /** Active network connections derived from captured packets */
    val connections: StateFlow<List<Connection>>

    /** Security alerts generated from packet analysis */
    val alerts: StateFlow<List<SecurityAlert>>

    /** Live capture statistics (single source of truth for the active session) */
    val stats: StateFlow<CaptureStats>

    /** Start or reset the active capture session stats */
    fun startSession()

    /** Stop and finalize the active capture session stats */
    fun stopSession()

    /** Add a newly captured packet */
    fun addPacket(packet: Packet)

    /** Clear all captured data */
    fun clear()

    /** Get a specific packet by ID */
    fun getPacketById(id: Long): Packet?
}
