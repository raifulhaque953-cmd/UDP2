package com.pocketshark.app.data.repository

import com.pocketshark.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of [PacketRepository].
 * Maintains a bounded packet buffer, tracks connections, and generates security alerts
 * based on heuristic analysis of captured traffic.
 */
class PacketRepositoryImpl(
    private val maxPackets: Int = 10_000
) : PacketRepository {

    private val packetBuffer = mutableListOf<Packet>()
    private val connectionMap = ConcurrentHashMap<String, Connection>()
    private val alertBuffer = mutableListOf<SecurityAlert>()
    private var alertIdCounter = 0L

    // Tracking for security analysis
    private val portScanTracker = ConcurrentHashMap<String, MutableSet<Int>>()
    private val cleartextPorts = setOf(80, 21, 23, 25, 110, 143)

    private val _packets = MutableStateFlow<List<Packet>>(emptyList())
    override val packets: StateFlow<List<Packet>> = _packets.asStateFlow()

    private val _connections = MutableStateFlow<List<Connection>>(emptyList())
    override val connections: StateFlow<List<Connection>> = _connections.asStateFlow()

    private val _alerts = MutableStateFlow<List<SecurityAlert>>(emptyList())
    override val alerts: StateFlow<List<SecurityAlert>> = _alerts.asStateFlow()

    private val _stats = MutableStateFlow(CaptureStats())
    override val stats: StateFlow<CaptureStats> = _stats.asStateFlow()

    override fun startSession() {
        synchronized(packetBuffer) {
            packetBuffer.clear()
            _packets.value = emptyList()
        }
        connectionMap.clear()
        _connections.value = emptyList()
        alertBuffer.clear()
        _alerts.value = emptyList()
        portScanTracker.clear()
        _stats.value = CaptureStats(startTime = System.currentTimeMillis())
    }

    override fun stopSession() {
        val current = _stats.value
        _stats.value = current.copy(
            packetsPerSecond = 0f,
            bytesPerSecond = 0f
        )
    }

    override fun addPacket(packet: Packet) {
        synchronized(packetBuffer) {
            packetBuffer.add(0, packet) // Prepend (newest first)
            if (packetBuffer.size > maxPackets) {
                packetBuffer.removeAt(packetBuffer.lastIndex)
            }
            _packets.value = packetBuffer.toList()
        }

        // Update connection tracking
        updateConnections(packet)

        // Run security heuristics
        analyzeForThreats(packet)

        // Update stats
        updateStats(packet)
    }

    override fun clear() {
        synchronized(packetBuffer) {
            packetBuffer.clear()
            _packets.value = emptyList()
        }
        connectionMap.clear()
        _connections.value = emptyList()
        alertBuffer.clear()
        _alerts.value = emptyList()
        portScanTracker.clear()
        _stats.value = CaptureStats()
    }

    override fun getPacketById(id: Long): Packet? {
        return synchronized(packetBuffer) {
            packetBuffer.find { it.id == id }
        }
    }

    // ── Connection Tracking ────────────────────────────────────────────

    private fun updateConnections(packet: Packet) {
        val srcPort = packet.sourcePort ?: return
        val dstPort = packet.destinationPort ?: return

        val connId = buildConnectionId(
            packet.sourceAddress, srcPort,
            packet.destinationAddress, dstPort,
            packet.protocol
        )

        val existing = connectionMap[connId]
        val now = System.currentTimeMillis()

        if (existing != null) {
            connectionMap[connId] = existing.copy(
                packetCount = existing.packetCount + 1,
                bytesOut = existing.bytesOut + packet.length,
                lastActivity = now,
                state = inferConnectionState(packet, existing.state)
            )
        } else {
            connectionMap[connId] = Connection(
                id = connId,
                protocol = packet.protocol,
                sourceAddress = packet.sourceAddress,
                sourcePort = srcPort,
                destinationAddress = packet.destinationAddress,
                destinationPort = dstPort,
                state = inferInitialState(packet),
                bytesIn = 0,
                bytesOut = packet.length.toLong(),
                packetCount = 1,
                startTime = now,
                lastActivity = now
            )
        }

        _connections.value = connectionMap.values.sortedByDescending { it.lastActivity }
    }

    private fun buildConnectionId(
        srcIp: String, srcPort: Int,
        dstIp: String, dstPort: Int,
        protocol: Protocol
    ): String {
        // Normalize: always use lower IP:port as "source" for bidirectional matching
        return if ("$srcIp:$srcPort" < "$dstIp:$dstPort") {
            "${protocol.name}:$srcIp:$srcPort↔$dstIp:$dstPort"
        } else {
            "${protocol.name}:$dstIp:$dstPort↔$srcIp:$srcPort"
        }
    }

    private fun inferInitialState(packet: Packet): ConnectionState {
        return when (packet.protocol) {
            Protocol.TCP -> {
                val flags = packet.tcpHeader?.flags ?: emptySet()
                when {
                    TcpFlag.SYN in flags && TcpFlag.ACK !in flags -> ConnectionState.SYN_SENT
                    TcpFlag.SYN in flags && TcpFlag.ACK in flags -> ConnectionState.SYN_RECEIVED
                    else -> ConnectionState.ESTABLISHED
                }
            }
            Protocol.UDP, Protocol.DNS -> ConnectionState.ACTIVE
            else -> ConnectionState.UNKNOWN
        }
    }

    private fun inferConnectionState(packet: Packet, current: ConnectionState): ConnectionState {
        if (packet.protocol != Protocol.TCP) return current
        val flags = packet.tcpHeader?.flags ?: return current

        return when {
            TcpFlag.RST in flags -> ConnectionState.CLOSED
            TcpFlag.FIN in flags -> ConnectionState.FIN_WAIT
            TcpFlag.SYN in flags && TcpFlag.ACK in flags -> ConnectionState.ESTABLISHED
            else -> if (current == ConnectionState.SYN_SENT) ConnectionState.ESTABLISHED else current
        }
    }

    // ── Security Analysis ──────────────────────────────────────────────

    private fun analyzeForThreats(packet: Packet) {
        // Detect cleartext traffic on known unencrypted ports
        val dstPort = packet.destinationPort
        if (dstPort != null && dstPort in cleartextPorts && packet.protocol == Protocol.TCP) {
            addAlert(
                severity = ThreatSeverity.MEDIUM,
                category = ThreatCategory.UNENCRYPTED_TRAFFIC,
                title = "Unencrypted Connection",
                description = "Traffic to ${packet.destinationAddress}:$dstPort uses an unencrypted protocol.",
                packetId = packet.id,
                srcAddr = packet.sourceAddress,
                dstAddr = packet.destinationAddress
            )
        }

        // Detect potential port scanning (many different dst ports from same source)
        if (packet.sourcePort != null && dstPort != null) {
            val scanKey = "${packet.sourceAddress}→${packet.destinationAddress}"
            val ports = portScanTracker.getOrPut(scanKey) { mutableSetOf() }
            ports.add(dstPort)
            if (ports.size >= 20) {
                addAlert(
                    severity = ThreatSeverity.HIGH,
                    category = ThreatCategory.PORT_SCAN,
                    title = "Port Scan Detected",
                    description = "${packet.sourceAddress} has probed ${ports.size} ports on ${packet.destinationAddress}.",
                    packetId = packet.id,
                    srcAddr = packet.sourceAddress,
                    dstAddr = packet.destinationAddress
                )
                ports.clear() // Reset after alert
            }
        }

        // Detect suspicious DNS queries (very long domain names)
        if (packet.dnsData != null) {
            for (query in packet.dnsData.queries) {
                if (query.name.length > 60) {
                    addAlert(
                        severity = ThreatSeverity.HIGH,
                        category = ThreatCategory.SUSPICIOUS_DNS,
                        title = "Suspicious DNS Query",
                        description = "Unusually long domain name: ${query.name.take(40)}…",
                        packetId = packet.id,
                        srcAddr = packet.sourceAddress,
                        dstAddr = packet.destinationAddress
                    )
                }
            }
        }
    }

    private fun addAlert(
        severity: ThreatSeverity,
        category: ThreatCategory,
        title: String,
        description: String,
        packetId: Long,
        srcAddr: String?,
        dstAddr: String?
    ) {
        val alert = SecurityAlert(
            id = alertIdCounter++,
            timestamp = System.currentTimeMillis(),
            severity = severity,
            category = category,
            title = title,
            description = description,
            relatedPacketIds = listOf(packetId),
            sourceAddress = srcAddr,
            destinationAddress = dstAddr
        )
        synchronized(alertBuffer) {
            alertBuffer.add(0, alert)
            if (alertBuffer.size > 500) alertBuffer.removeAt(alertBuffer.lastIndex)
            _alerts.value = alertBuffer.toList()
        }
    }

    // ── Stats ──────────────────────────────────────────────────────────

    private fun updateStats(packet: Packet) {
        val current = _stats.value
        val now = System.currentTimeMillis()
        val startTime = if (current.startTime == 0L) now else current.startTime
        val elapsed = (now - startTime).coerceAtLeast(1)
        val newCount = current.packetCount + 1
        val newBytes = current.totalBytes + packet.length

        _stats.value = current.copy(
            packetCount = newCount,
            totalBytes = newBytes,
            tcpCount = current.tcpCount + if (packet.protocol == Protocol.TCP) 1 else 0,
            udpCount = current.udpCount + if (packet.protocol == Protocol.UDP) 1 else 0,
            dnsCount = current.dnsCount + if (packet.protocol == Protocol.DNS) 1 else 0,
            icmpCount = current.icmpCount + if (packet.protocol == Protocol.ICMP) 1 else 0,
            tlsCount = current.tlsCount + if (packet.protocol == Protocol.TLS) 1 else 0,
            httpCount = current.httpCount + if (packet.protocol == Protocol.HTTP) 1 else 0,
            startTime = startTime,
            packetsPerSecond = newCount * 1000f / elapsed,
            bytesPerSecond = newBytes * 1000f / elapsed
        )
    }
}
