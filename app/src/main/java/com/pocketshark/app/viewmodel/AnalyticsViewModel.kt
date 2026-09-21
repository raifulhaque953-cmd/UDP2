package com.pocketshark.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketshark.app.PocketSharkApp
import com.pocketshark.app.data.model.CaptureStats
import com.pocketshark.app.data.model.Protocol
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TopIpStat(val ip: String, val packetCount: Int, val byteCount: Long)
data class TopPortStat(val port: Int, val serviceName: String, val packetCount: Int)
data class DnsDomainStat(val domain: String, val queryCount: Int)

/**
 * ViewModel for the Analytics screen.
 * Aggregates time-series traffic data, protocol distribution, top IPs/ports, DNS activity, and top conversations.
 */
class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PocketSharkApp).packetRepository

    /** Live session stats */
    val stats: StateFlow<CaptureStats> = repository.stats

    /** Protocol distribution for donut chart */
    val protocolDistribution = repository.stats.map { stats ->
        stats.protocolDistribution
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Traffic data points (packets per second, sampled over time) */
    private val _trafficHistory = MutableStateFlow<List<Float>>(emptyList())
    val trafficHistory: StateFlow<List<Float>> = _trafficHistory.asStateFlow()

    /** Top conversations (source ↔ destination pairs by packet volume) */
    val topConversations = repository.connections.map { connections ->
        connections
            .sortedByDescending { it.packetCount }
            .take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Top Destination IPs */
    val topDestinationIps: StateFlow<List<TopIpStat>> = repository.packets.map { packets ->
        packets.groupBy { it.destinationAddress }
            .map { (ip, list) ->
                TopIpStat(
                    ip = ip,
                    packetCount = list.size,
                    byteCount = list.sumOf { it.length.toLong() }
                )
            }
            .sortedByDescending { it.packetCount }
            .take(6)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Top Destination Ports */
    val topDestinationPorts: StateFlow<List<TopPortStat>> = repository.packets.map { packets ->
        packets.mapNotNull { it.destinationPort }
            .groupBy { it }
            .map { (port, list) ->
                TopPortStat(
                    port = port,
                    serviceName = getPortServiceName(port),
                    packetCount = list.size
                )
            }
            .sortedByDescending { it.packetCount }
            .take(6)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** DNS Activity (Domain query counts) */
    val topDnsDomains: StateFlow<List<DnsDomainStat>> = repository.packets.map { packets ->
        packets.mapNotNull { it.dnsData }
            .flatMap { it.queries }
            .groupBy { it.name }
            .map { (domain, list) ->
                DnsDomainStat(domain = domain, queryCount = list.size)
            }
            .sortedByDescending { it.queryCount }
            .take(6)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Total bytes formatted */
    val totalBytesFormatted = repository.stats.map { it.formattedBytes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0 B")

    /** Packets per second */
    val packetsPerSecond = repository.stats.map {
        String.format("%.1f", it.packetsPerSecond)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0.0")

    init {
        // Sample traffic rate every 2 seconds for the graph
        viewModelScope.launch {
            repository.stats.collect { stats ->
                val current = _trafficHistory.value.toMutableList()
                current.add(stats.packetsPerSecond)
                if (current.size > 60) {
                    current.removeAt(0)
                }
                _trafficHistory.value = current
            }
        }
    }

    private fun getPortServiceName(port: Int): String {
        return when (port) {
            443 -> "HTTPS"
            80 -> "HTTP"
            53 -> "DNS"
            22 -> "SSH"
            21 -> "FTP"
            25 -> "SMTP"
            123 -> "NTP"
            445 -> "SMB"
            1883 -> "MQTT"
            8080 -> "HTTP-Alt"
            else -> "Port $port"
        }
    }
}
