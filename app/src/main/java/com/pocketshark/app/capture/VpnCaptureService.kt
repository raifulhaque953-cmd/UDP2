package com.pocketshark.app.capture

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import com.pocketshark.app.data.SettingsManager
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pocketshark.app.MainActivity
import com.pocketshark.app.PocketSharkApp
import com.pocketshark.app.R
import com.pocketshark.app.data.model.CaptureStats
import com.pocketshark.app.data.model.Packet
import com.pocketshark.app.data.model.Protocol
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

/**
 * Android VpnService implementation that captures all device network traffic
 * through a TUN interface, parses IP packets, and forwards them to maintain
 * device connectivity.
 *
 * Architecture:
 * 1. Establishes a TUN interface via VpnService.Builder
 * 2. Reads raw IP packets from the TUN file descriptor
 * 3. Parses each packet using [PacketParser]
 * 4. Emits parsed packets to [PacketRepository] via flows
 * 5. Forwards packets to real destinations via protected sockets
 */
class VpnCaptureService : VpnService() {

    companion object {
        private const val TAG = "VpnCaptureService"
        private const val NOTIFICATION_ID = 1001
        private const val MAX_PACKET_SIZE = 32767
        private const val TUN_MTU = 1500
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"

        // Shared state accessible from UI
        private val _state = MutableStateFlow<CaptureState>(CaptureState.Idle)
        val state: StateFlow<CaptureState> = _state.asStateFlow()

        private val _packets = MutableSharedFlow<Packet>(
            replay = 0,
            extraBufferCapacity = 1024
        )
        val packets: SharedFlow<Packet> = _packets.asSharedFlow()

        private val _stats = MutableStateFlow(CaptureStats())
        val stats: StateFlow<CaptureStats> = _stats.asStateFlow()

        fun resetState() {
            _state.value = CaptureState.Idle
            _stats.value = CaptureStats()
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var activeVpnAddress = "10.0.0.2"

    // UDP session tracking for forwarding
    private val udpSessions = ConcurrentHashMap<String, DatagramSocket>()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VpnCaptureService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_START -> {
                val settings = (application as PocketSharkApp).settingsManager
                startForeground(NOTIFICATION_ID, buildNotification(settings))
                startCapture(settings)
                START_STICKY
            }
            ACTION_STOP -> {
                stopCapture()
                START_NOT_STICKY
            }
            else -> START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        stopCapture()
        super.onDestroy()
    }

    // ── Capture Lifecycle ──────────────────────────────────────────────

    private fun startCapture(settings: SettingsManager) {
        if (_state.value == CaptureState.Running) return

        _state.value = CaptureState.Preparing
        _stats.value = CaptureStats(startTime = System.currentTimeMillis())

        try {
            val vpnAddress = settings.subnet.value
            activeVpnAddress = vpnAddress
            val primaryDns = settings.primaryDns.value
            val secondaryDns = settings.secondaryDns.value
            val mtu = settings.mtu.value

            val builder = Builder()
                .setSession("PocketShark")
                .addAddress(vpnAddress, 32)
                .addRoute(VPN_ROUTE, 0)
                .addDnsServer(primaryDns)
                .addDnsServer(secondaryDns)
                .setMtu(mtu)
                .setBlocking(true)

            vpnInterface = builder.establish()

            if (vpnInterface == null) {
                _state.value = CaptureState.Error("Failed to establish VPN interface")
                return
            }

            _state.value = CaptureState.Running
            Log.i(TAG, "Capture started — TUN interface established")

            // Initialize single source of truth session stats in repository
            (application as? PocketSharkApp)?.packetRepository?.startSession()

            // Launch packet processing loops
            serviceScope.launch { readFromTun() }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start capture", e)
            _state.value = CaptureState.Error(e.message ?: "Unknown error")
        }
    }

    private fun startCapture() {
        startCapture((application as PocketSharkApp).settingsManager)
    }

    private fun stopCapture() {
        Log.i(TAG, "Stopping capture")

        serviceScope.cancel()

        // Finalize capture session stats
        (application as? PocketSharkApp)?.packetRepository?.stopSession()

        // Close UDP sessions
        udpSessions.values.forEach { socket ->
            try { socket.close() } catch (_: Exception) {}
        }
        udpSessions.clear()

        // Close TUN interface
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}
        vpnInterface = null

        _state.value = CaptureState.Stopped
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Packet Processing ──────────────────────────────────────────────

    private suspend fun readFromTun() {
        val buffer = ByteBuffer.allocate(MAX_PACKET_SIZE)
        val fd = vpnInterface?.fileDescriptor ?: return
        val inputStream = FileInputStream(fd)
        val outputStream = FileOutputStream(fd)

        try {
            while (_state.value == CaptureState.Running) {
                buffer.clear()
                val length = inputStream.read(buffer.array())

                if (length > 0) {
                    val rawData = ByteArray(length)
                    System.arraycopy(buffer.array(), 0, rawData, 0, length)

                    // Parse the packet
                    val packet = PacketParser.parse(rawData)

                    if (packet != null) {
                        // Emit to observers
                        _packets.tryEmit(packet)

                        // Push to single source of truth repository (updates packets, connections, alerts, and stats)
                        val app = application as? PocketSharkApp
                        app?.packetRepository?.addPacket(packet)
                    }

                    // Forward the packet to maintain connectivity
                    serviceScope.launch {
                        forwardPacket(rawData, outputStream)
                    }

                } else if (length < 0) {
                    // TUN fd closed
                    break
                }
            }
        } catch (e: Exception) {
            if (_state.value == CaptureState.Running) {
                Log.e(TAG, "Error reading from TUN", e)
                _state.value = CaptureState.Error("Read error: ${e.message}")
            }
        } finally {
            inputStream.close()
        }
    }

    private fun updateStats(packet: Packet) {
        val current = _stats.value
        val elapsed = (System.currentTimeMillis() - current.startTime).coerceAtLeast(1)
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
            packetsPerSecond = newCount * 1000f / elapsed,
            bytesPerSecond = newBytes * 1000f / elapsed
        )
    }

    // ── Packet Forwarding ──────────────────────────────────────────────

    /**
     * Forward a captured packet to its real destination.
     * Uses protected UDP sockets for UDP traffic.
     * TCP forwarding is architecturally stubbed for future implementation
     * with a userspace TCP stack (e.g., lwIP or tun2socks).
     */
    private suspend fun forwardPacket(rawData: ByteArray, tunOutput: FileOutputStream) {
        if (rawData.size < 20) return

        val ipProtocol = rawData[9].toInt() and 0xFF
        val ihl = (rawData[0].toInt() and 0x0F) * 4

        when (ipProtocol) {
            17 -> forwardUdp(rawData, ihl, tunOutput)
            // TCP forwarding requires a userspace TCP stack.
            // For production, integrate tun2socks or lwIP via JNI.
            // Packets are still captured and parsed above.
        }
    }

    private suspend fun forwardUdp(rawData: ByteArray, ihl: Int, tunOutput: FileOutputStream) {
        try {
            if (rawData.size < ihl + 8) return

            val srcPort = ((rawData[ihl].toInt() and 0xFF) shl 8) or (rawData[ihl + 1].toInt() and 0xFF)
            val dstPort = ((rawData[ihl + 2].toInt() and 0xFF) shl 8) or (rawData[ihl + 3].toInt() and 0xFF)
            val udpLen = ((rawData[ihl + 4].toInt() and 0xFF) shl 8) or (rawData[ihl + 5].toInt() and 0xFF)

            val dstIp = "${rawData[16].toInt() and 0xFF}.${rawData[17].toInt() and 0xFF}." +
                    "${rawData[18].toInt() and 0xFF}.${rawData[19].toInt() and 0xFF}"

            val payloadOffset = ihl + 8
            val payloadLength = minOf(udpLen - 8, rawData.size - payloadOffset)
            if (payloadLength <= 0) return

            val sessionKey = "$srcPort:$dstIp:$dstPort"

            val socket = udpSessions.getOrPut(sessionKey) {
                DatagramSocket().also { ds ->
                    protect(ds) // Prevent routing loop through TUN
                }
            }

            val destAddr = InetAddress.getByName(dstIp)
            val payload = ByteArray(payloadLength)
            System.arraycopy(rawData, payloadOffset, payload, 0, payloadLength)

            withContext(Dispatchers.IO) {
                val outPacket = DatagramPacket(payload, payload.size, destAddr, dstPort)
                socket.send(outPacket)

                // Attempt to receive response (with timeout)
                socket.soTimeout = 500
                try {
                    val respBuf = ByteArray(MAX_PACKET_SIZE)
                    val respPacket = DatagramPacket(respBuf, respBuf.size)
                    socket.receive(respPacket)

                    val responseIp = buildUdpResponse(
                        srcIp = dstIp,
                        dstIp = activeVpnAddress,
                        srcPort = dstPort,
                        dstPort = srcPort,
                        payload = respBuf.copyOf(respPacket.length)
                    )

                    synchronized(tunOutput) {
                        tunOutput.write(responseIp)
                        tunOutput.flush()
                    }

                    // Parse and emit the response packet too
                    val responseParsed = PacketParser.parse(responseIp)
                    if (responseParsed != null) {
                        _packets.tryEmit(responseParsed)
                        updateStats(responseParsed)
                        (application as? PocketSharkApp)?.packetRepository?.addPacket(responseParsed)
                    }
                } catch (_: Exception) {
                    // Timeout or no response — normal for many UDP flows
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "UDP forward error: ${e.message}")
        }
    }

    /**
     * Construct a raw IPv4+UDP packet for injection back into the TUN interface.
     */
    private fun buildUdpResponse(
        srcIp: String,
        dstIp: String,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val udpLength = 8 + payload.size
        val totalLength = 20 + udpLength
        val packet = ByteArray(totalLength)

        // IP header
        packet[0] = 0x45.toByte() // Version 4, IHL 5
        packet[1] = 0
        packet[2] = (totalLength shr 8).toByte()
        packet[3] = (totalLength and 0xFF).toByte()
        // identification, flags, fragment offset = 0
        packet[8] = 64 // TTL
        packet[9] = 17 // UDP protocol
        // checksum computed later

        val srcParts = srcIp.split(".")
        val dstParts = dstIp.split(".")
        for (i in 0..3) {
            packet[12 + i] = srcParts[i].toInt().toByte()
            packet[16 + i] = dstParts[i].toInt().toByte()
        }

        // IP header checksum
        var sum = 0L
        for (i in 0 until 20 step 2) {
            sum += ((packet[i].toInt() and 0xFF) shl 8) or (packet[i + 1].toInt() and 0xFF)
        }
        while (sum shr 16 != 0L) sum = (sum and 0xFFFF) + (sum shr 16)
        val checksum = sum.toInt().inv() and 0xFFFF
        packet[10] = (checksum shr 8).toByte()
        packet[11] = (checksum and 0xFF).toByte()

        // UDP header
        packet[20] = (srcPort shr 8).toByte()
        packet[21] = (srcPort and 0xFF).toByte()
        packet[22] = (dstPort shr 8).toByte()
        packet[23] = (dstPort and 0xFF).toByte()
        packet[24] = (udpLength shr 8).toByte()
        packet[25] = (udpLength and 0xFF).toByte()
        // UDP checksum = 0 (optional for IPv4)

        System.arraycopy(payload, 0, packet, 28, payload.size)

        return packet
    }

    // ── Notification ───────────────────────────────────────────────────

    private fun buildNotification(settings: SettingsManager): android.app.Notification {
        val priority = if (settings.notificationPriority.value == "High") {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_LOW
        }

        return NotificationCompat.Builder(this, PocketSharkApp.CAPTURE_CHANNEL_ID)
            .setContentTitle("PocketShark")
            .setContentText("Capturing network traffic…")
            .setSmallIcon(R.drawable.ic_capture)
            .setOngoing(true)
            .setPriority(priority)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                R.drawable.ic_stop,
                "Stop",
                PendingIntent.getService(
                    this, 1,
                    Intent(this, VpnCaptureService::class.java).apply { action = ACTION_STOP },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }
}

// Action constants for the service intents
const val ACTION_START = "com.pocketshark.action.START_CAPTURE"
const val ACTION_STOP = "com.pocketshark.action.STOP_CAPTURE"
