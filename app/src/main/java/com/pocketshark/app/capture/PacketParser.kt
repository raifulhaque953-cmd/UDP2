package com.pocketshark.app.capture

import com.pocketshark.app.data.model.*
import java.nio.ByteBuffer

/**
 * Pure-Kotlin packet parser for raw IP packets read from the TUN interface.
 * Parses IPv4 headers and transport layer protocols: TCP, UDP, ICMP, DNS.
 */
object PacketParser {

    private var nextId: Long = 0L

    /**
     * Parse a raw IP packet byte array into a structured [Packet] object.
     * Returns null if the packet cannot be parsed (too short, unsupported version, etc.)
     */
    fun parse(rawData: ByteArray): Packet? {
        if (rawData.size < 20) return null

        val version = (rawData[0].toInt() and 0xF0) ushr 4
        if (version != 4) return null // IPv4 only for now

        val ihl = (rawData[0].toInt() and 0x0F) * 4
        if (rawData.size < ihl) return null

        val ipHeader = parseIpHeader(rawData, ihl)

        var tcpHeader: TcpHeader? = null
        var udpHeader: UdpHeader? = null
        var icmpHeader: IcmpHeader? = null
        var dnsData: DnsData? = null
        var srcPort: Int? = null
        var dstPort: Int? = null
        var info = ""

        when (ipHeader.protocol) {
            6 -> { // TCP
                if (rawData.size >= ihl + 20) {
                    tcpHeader = parseTcpHeader(rawData, ihl)
                    srcPort = tcpHeader.sourcePort
                    dstPort = tcpHeader.destinationPort
                    info = buildTcpInfo(tcpHeader)
                }
            }
            17 -> { // UDP
                if (rawData.size >= ihl + 8) {
                    udpHeader = parseUdpHeader(rawData, ihl)
                    srcPort = udpHeader.sourcePort
                    dstPort = udpHeader.destinationPort

                    if (srcPort == 53 || dstPort == 53) {
                        val dnsOffset = ihl + 8
                        if (rawData.size > dnsOffset + 12) {
                            dnsData = parseDns(rawData, dnsOffset)
                            info = buildDnsInfo(dnsData)
                        }
                    } else {
                        info = "$srcPort → $dstPort Len=${udpHeader.length - 8}"
                    }
                }
            }
            1 -> { // ICMP
                if (rawData.size >= ihl + 8) {
                    icmpHeader = parseIcmpHeader(rawData, ihl)
                    info = buildIcmpInfo(icmpHeader)
                }
            }
            else -> {
                info = "IP Protocol ${ipHeader.protocol}"
            }
        }

        val protocol = Protocol.fromIpProtocol(ipHeader.protocol, srcPort, dstPort)

        return Packet(
            id = nextId++,
            timestamp = System.currentTimeMillis(),
            protocol = protocol,
            length = rawData.size,
            sourceAddress = ipHeader.sourceAddress,
            destinationAddress = ipHeader.destinationAddress,
            sourcePort = srcPort,
            destinationPort = dstPort,
            ipHeader = ipHeader,
            tcpHeader = tcpHeader,
            udpHeader = udpHeader,
            icmpHeader = icmpHeader,
            dnsData = dnsData,
            rawData = rawData.copyOf(),
            info = info
        )
    }

    // ── IP Header ──────────────────────────────────────────────────────

    private fun parseIpHeader(data: ByteArray, ihl: Int): IpHeader {
        return IpHeader(
            version = (data[0].toInt() and 0xF0) ushr 4,
            headerLength = ihl,
            totalLength = readUInt16(data, 2),
            identification = readUInt16(data, 4),
            flags = (data[6].toInt() and 0xE0) ushr 5,
            fragmentOffset = readUInt16(data, 6) and 0x1FFF,
            ttl = data[8].toInt() and 0xFF,
            protocol = data[9].toInt() and 0xFF,
            checksum = readUInt16(data, 10),
            sourceAddress = formatIpv4(data, 12),
            destinationAddress = formatIpv4(data, 16)
        )
    }

    // ── TCP Header ─────────────────────────────────────────────────────

    private fun parseTcpHeader(data: ByteArray, offset: Int): TcpHeader {
        val flagsByte = data[offset + 13].toInt() and 0xFF
        val flags = mutableSetOf<TcpFlag>()
        if (flagsByte and 0x01 != 0) flags.add(TcpFlag.FIN)
        if (flagsByte and 0x02 != 0) flags.add(TcpFlag.SYN)
        if (flagsByte and 0x04 != 0) flags.add(TcpFlag.RST)
        if (flagsByte and 0x08 != 0) flags.add(TcpFlag.PSH)
        if (flagsByte and 0x10 != 0) flags.add(TcpFlag.ACK)
        if (flagsByte and 0x20 != 0) flags.add(TcpFlag.URG)
        if (flagsByte and 0x40 != 0) flags.add(TcpFlag.ECE)
        if (flagsByte and 0x80 != 0) flags.add(TcpFlag.CWR)

        val dataOffset = ((data[offset + 12].toInt() and 0xF0) ushr 4) * 4

        return TcpHeader(
            sourcePort = readUInt16(data, offset),
            destinationPort = readUInt16(data, offset + 2),
            sequenceNumber = readUInt32(data, offset + 4),
            acknowledgmentNumber = readUInt32(data, offset + 8),
            dataOffset = dataOffset,
            flags = flags,
            windowSize = readUInt16(data, offset + 14),
            checksum = readUInt16(data, offset + 16),
            urgentPointer = readUInt16(data, offset + 18)
        )
    }

    private fun buildTcpInfo(tcp: TcpHeader): String {
        val flagStr = tcp.flags.joinToString("") { it.abbreviation }
        return "${tcp.sourcePort} → ${tcp.destinationPort} [$flagStr] " +
                "Seq=${tcp.sequenceNumber} Ack=${tcp.acknowledgmentNumber} " +
                "Win=${tcp.windowSize}"
    }

    // ── UDP Header ─────────────────────────────────────────────────────

    private fun parseUdpHeader(data: ByteArray, offset: Int): UdpHeader {
        return UdpHeader(
            sourcePort = readUInt16(data, offset),
            destinationPort = readUInt16(data, offset + 2),
            length = readUInt16(data, offset + 4),
            checksum = readUInt16(data, offset + 6)
        )
    }

    // ── ICMP Header ────────────────────────────────────────────────────

    private fun parseIcmpHeader(data: ByteArray, offset: Int): IcmpHeader {
        return IcmpHeader(
            type = data[offset].toInt() and 0xFF,
            code = data[offset + 1].toInt() and 0xFF,
            checksum = readUInt16(data, offset + 2),
            id = readUInt16(data, offset + 4),
            sequence = readUInt16(data, offset + 6)
        )
    }

    private fun buildIcmpInfo(icmp: IcmpHeader): String {
        val typeName = when (icmp.type) {
            0 -> "Echo Reply"
            3 -> "Destination Unreachable"
            8 -> "Echo Request"
            11 -> "Time Exceeded"
            else -> "Type ${icmp.type}"
        }
        return "$typeName id=${icmp.id} seq=${icmp.sequence}"
    }

    // ── DNS Parser ─────────────────────────────────────────────────────

    private fun parseDns(data: ByteArray, offset: Int): DnsData? {
        return try {
            val buf = ByteBuffer.wrap(data, offset, data.size - offset)

            val txId = buf.short.toInt() and 0xFFFF
            val flags = buf.short.toInt() and 0xFFFF
            val isResponse = (flags and 0x8000) != 0
            val qdCount = buf.short.toInt() and 0xFFFF
            val anCount = buf.short.toInt() and 0xFFFF
            buf.short // nscount - skip
            buf.short // arcount - skip

            val queries = mutableListOf<DnsQuery>()
            for (i in 0 until minOf(qdCount, 4)) { // Limit to prevent parsing runaway
                val name = readDnsName(data, offset + buf.position() - (data.size - offset - buf.remaining()))
                    ?: break
                // Advance buffer past the name
                skipDnsName(buf)
                if (buf.remaining() < 4) break
                val qType = buf.short.toInt() and 0xFFFF
                val qClass = buf.short.toInt() and 0xFFFF
                queries.add(DnsQuery(name, qType, qClass))
            }

            DnsData(
                transactionId = txId,
                isResponse = isResponse,
                questionCount = qdCount,
                answerCount = anCount,
                queries = queries,
                answers = emptyList() // Answer parsing deferred for performance
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun readDnsName(data: ByteArray, startOffset: Int): String? {
        return try {
            val parts = mutableListOf<String>()
            var offset = startOffset
            var jumps = 0
            while (offset < data.size && jumps < 10) {
                val len = data[offset].toInt() and 0xFF
                if (len == 0) break
                if ((len and 0xC0) == 0xC0) {
                    // Pointer
                    if (offset + 1 >= data.size) break
                    offset = ((len and 0x3F) shl 8) or (data[offset + 1].toInt() and 0xFF)
                    jumps++
                    continue
                }
                offset++
                if (offset + len > data.size) break
                parts.add(String(data, offset, len))
                offset += len
            }
            if (parts.isEmpty()) null else parts.joinToString(".")
        } catch (_: Exception) {
            null
        }
    }

    private fun skipDnsName(buf: ByteBuffer) {
        while (buf.hasRemaining()) {
            val len = buf.get().toInt() and 0xFF
            if (len == 0) break
            if ((len and 0xC0) == 0xC0) {
                buf.get() // skip second byte of pointer
                break
            }
            if (buf.remaining() < len) break
            buf.position(buf.position() + len)
        }
    }

    private fun buildDnsInfo(dns: DnsData?): String {
        if (dns == null) return "DNS"
        val prefix = if (dns.isResponse) "Response" else "Query"
        val queryNames = dns.queries.joinToString(", ") { it.name }
        return "DNS $prefix: $queryNames"
    }

    // ── Utility ────────────────────────────────────────────────────────

    private fun readUInt16(data: ByteArray, offset: Int): Int {
        return ((data[offset].toInt() and 0xFF) shl 8) or
                (data[offset + 1].toInt() and 0xFF)
    }

    private fun readUInt32(data: ByteArray, offset: Int): Long {
        return ((data[offset].toLong() and 0xFF) shl 24) or
                ((data[offset + 1].toLong() and 0xFF) shl 16) or
                ((data[offset + 2].toLong() and 0xFF) shl 8) or
                (data[offset + 3].toLong() and 0xFF)
    }

    private fun formatIpv4(data: ByteArray, offset: Int): String {
        return "${data[offset].toInt() and 0xFF}." +
                "${data[offset + 1].toInt() and 0xFF}." +
                "${data[offset + 2].toInt() and 0xFF}." +
                "${data[offset + 3].toInt() and 0xFF}"
    }

    /** Convert raw bytes to hex dump string for display */
    fun toHexDump(data: ByteArray, maxBytes: Int = 256): String {
        val sb = StringBuilder()
        val limit = minOf(data.size, maxBytes)
        for (i in 0 until limit step 16) {
            // Offset
            sb.append(String.format("%04X  ", i))
            // Hex bytes
            for (j in 0 until 16) {
                if (i + j < limit) {
                    sb.append(String.format("%02X ", data[i + j].toInt() and 0xFF))
                } else {
                    sb.append("   ")
                }
                if (j == 7) sb.append(" ")
            }
            sb.append(" |")
            // ASCII
            for (j in 0 until 16) {
                if (i + j < limit) {
                    val c = data[i + j].toInt() and 0xFF
                    sb.append(if (c in 32..126) c.toChar() else '.')
                }
            }
            sb.append("|\n")
        }
        if (data.size > maxBytes) {
            sb.append("... (${data.size - maxBytes} more bytes)")
        }
        return sb.toString()
    }
}
