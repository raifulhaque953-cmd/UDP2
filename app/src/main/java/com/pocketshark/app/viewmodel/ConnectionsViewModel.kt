package com.pocketshark.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketshark.app.PocketSharkApp
import com.pocketshark.app.data.model.Connection
import com.pocketshark.app.data.model.Protocol
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Connections screen.
 * Provides filtered connection list by protocol tab.
 */
class ConnectionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PocketSharkApp).packetRepository

    /** Currently selected tab filter */
    private val _selectedTab = MutableStateFlow(ConnectionTab.ALL)
    val selectedTab: StateFlow<ConnectionTab> = _selectedTab.asStateFlow()

    /** Search query for IP/Port */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Filtered connections based on selected tab and search query */
    val connections: StateFlow<List<Connection>> = combine(
        repository.connections,
        _selectedTab,
        _searchQuery
    ) { allConnections, tab, query ->
        val byTab = when (tab) {
            ConnectionTab.ALL -> allConnections
            ConnectionTab.TCP -> allConnections.filter {
                it.protocol == Protocol.TCP || it.protocol == Protocol.TLS || it.protocol == Protocol.HTTP
            }
            ConnectionTab.UDP -> allConnections.filter {
                it.protocol == Protocol.UDP || it.protocol == Protocol.DNS
            }
            ConnectionTab.TLS -> allConnections.filter {
                it.protocol == Protocol.TLS
            }
        }

        if (query.isBlank()) {
            byTab
        } else {
            val q = query.lowercase().trim()
            byTab.filter {
                it.sourceAddress.lowercase().contains(q) ||
                        it.destinationAddress.lowercase().contains(q) ||
                        it.sourcePort.toString().contains(q) ||
                        it.destinationPort.toString().contains(q) ||
                        it.protocol.displayName.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Total connection count */
    val totalCount = repository.connections.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectTab(tab: ConnectionTab) {
        _selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

enum class ConnectionTab(val displayName: String) {
    ALL("All"),
    TCP("TCP"),
    UDP("UDP"),
    TLS("TLS")
}
