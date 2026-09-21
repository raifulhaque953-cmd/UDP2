package com.pocketshark.app.data.model

import androidx.compose.ui.graphics.Color

/**
 * Network protocols identified by the packet parser.
 * Each protocol has a display name and an associated UI color for badges and charts.
 */
enum class Protocol(
    val displayName: String,
    val color: Color
) {
    TCP("TCP", Color(0xFF34D399)),
    UDP("UDP", Color(0xFFA78BFA)),
    DNS("DNS", Color(0xFF22D3EE)),
    TLS("TLS", Color(0xFF60A5FA)),
    ICMP("ICMP", Color(0xFFFBBF24)),
    HTTP("HTTP", Color(0xFFF87171)),
    UNKNOWN("???", Color(0xFF6B7280));

    companion object {
        /** Map IP protocol number to Protocol enum */
        fun fromIpProtocol(protocolNumber: Int, srcPort: Int?, dstPort: Int?): Protocol {
            return when (protocolNumber) {
                1 -> ICMP
                6 -> {
                    when {
                        srcPort == 443 || dstPort == 443 -> TLS
                        srcPort == 80 || dstPort == 80 -> HTTP
                        else -> TCP
                    }
                }
                17 -> {
                    when {
                        srcPort == 53 || dstPort == 53 -> DNS
                        else -> UDP
                    }
                }
                else -> UNKNOWN
            }
        }
    }
}

/** TCP connection flags */
enum class TcpFlag(val abbreviation: String) {
    SYN("S"), ACK("A"), FIN("F"), RST("R"),
    PSH("P"), URG("U"), ECE("E"), CWR("W")
}

/** IPv4 header fields */
data class IpHeader(
    val version: Int,
    val headerLength: Int,
    val totalLength: Int,
    val identification: Int,
    val flags: Int,
    val fragmentOffset: Int,
    val ttl: Int,
    val protocol: Int,
    val checksum: Int,
    val sourceAddress: String,
    val destinationAddress: String
)

/** TCP header fields */
data class TcpHeader(
    val sourcePort: Int,
    val destinationPort: Int,
    val sequenceNumber: Long,
    val acknowledgmentNumber: Long,
    val dataOffset: Int,
    val flags: Set<TcpFlag>,
    val windowSize: Int,
    val checksum: Int,
    val urgentPointer: Int
)

/** UDP header fields */
data class UdpHeader(
    val sourcePort: Int,
    val destinationPort: Int,
    val length: Int,
    val checksum: Int
)

/** ICMP header fields */
data class IcmpHeader(
    val type: Int,
    val code: Int,
    val checksum: Int,
    val id: Int,
    val sequence: Int
)

/** DNS query record */
data class DnsQuery(
    val name: String,
    val type: Int,
    val classCode: Int
)

/** DNS answer record */
data class DnsAnswer(
    val name: String,
    val type: Int,
    val classCode: Int,
    val ttl: Int,
    val data: String
)

/** Parsed DNS data */
data class DnsData(
    val transactionId: Int,
    val isResponse: Boolean,
    val questionCount: Int,
    val answerCount: Int,
    val queries: List<DnsQuery>,
    val answers: List<DnsAnswer>
)

/**
 * A captured network packet with parsed protocol headers.
 * Raw bytes are preserved for hex dump display and PCAP export.
 */
data class Packet(
    val id: Long,
    val timestamp: Long,
    val protocol: Protocol,
    val length: Int,
    val sourceAddress: String,
    val destinationAddress: String,
    val sourcePort: Int?,
    val destinationPort: Int?,
    val ipHeader: IpHeader?,
    val tcpHeader: TcpHeader?,
    val udpHeader: UdpHeader?,
    val icmpHeader: IcmpHeader?,
    val dnsData: DnsData?,
    val rawData: ByteArray,
    val info: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Packet) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
