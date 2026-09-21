package com.pocketshark.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketshark.app.PocketSharkApp
import com.pocketshark.app.data.model.Packet
import com.pocketshark.app.data.model.Protocol
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Packets screen.
 * Handles text filtering, protocol filtering, and provides the filtered packet list.
 */
class PacketsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PocketSharkApp).packetRepository

    /** Text filter query */
    private val _filterText = MutableStateFlow("")
    val filterText: StateFlow<String> = _filterText.asStateFlow()

    /** Selected protocol filters (empty = show all) */
    private val _selectedProtocols = MutableStateFlow<Set<Protocol>>(emptySet())
    val selectedProtocols: StateFlow<Set<Protocol>> = _selectedProtocols.asStateFlow()

    /** Sort order for packet list */
    private val _sortOrder = MutableStateFlow(PacketSortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<PacketSortOrder> = _sortOrder.asStateFlow()

    /** Filtered and sorted packet list */
    val filteredPackets: StateFlow<List<Packet>> = combine(
        repository.packets,
        _filterText,
        _selectedProtocols,
        _sortOrder
    ) { packets, text, protocols, sort ->
        val filtered = packets.filter { packet ->
            // Protocol filter
            val passesProtocol = protocols.isEmpty() || packet.protocol in protocols

            // Text filter (IP, port, protocol, info, DNS domain name)
            val passesText = text.isEmpty() || run {
                val query = text.lowercase().trim()
                packet.sourceAddress.lowercase().contains(query) ||
                        packet.destinationAddress.lowercase().contains(query) ||
                        packet.protocol.displayName.lowercase().contains(query) ||
                        packet.info.lowercase().contains(query) ||
                        (packet.sourcePort?.toString()?.contains(query) == true) ||
                        (packet.destinationPort?.toString()?.contains(query) == true) ||
                        (packet.dnsData?.queries?.any { it.name.lowercase().contains(query) } == true)
            }

            passesProtocol && passesText
        }

        when (sort) {
            PacketSortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.timestamp }
            PacketSortOrder.OLDEST_FIRST -> filtered.sortedBy { it.timestamp }
            PacketSortOrder.SIZE_DESC -> filtered.sortedByDescending { it.length }
            PacketSortOrder.PROTOCOL -> filtered.sortedBy { it.protocol.displayName }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Total unfiltered packet count for active session */
    val totalPacketCount = repository.stats.map { it.packetCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun updateFilterText(text: String) {
        _filterText.value = text
    }

    fun toggleProtocolFilter(protocol: Protocol) {
        _selectedProtocols.value = _selectedProtocols.value.let { current ->
            if (protocol in current) current - protocol else current + protocol
        }
    }

    fun setSortOrder(order: PacketSortOrder) {
        _sortOrder.value = order
    }

    fun clearFilters() {
        _filterText.value = ""
        _selectedProtocols.value = emptySet()
        _sortOrder.value = PacketSortOrder.NEWEST_FIRST
    }

    fun getPacketById(id: Long): Packet? {
        return repository.getPacketById(id)
    }
}

enum class PacketSortOrder(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    SIZE_DESC("Largest Size"),
    PROTOCOL("Protocol")
}
