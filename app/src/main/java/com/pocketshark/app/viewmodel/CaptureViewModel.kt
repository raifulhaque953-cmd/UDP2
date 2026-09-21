package com.pocketshark.app.viewmodel

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketshark.app.PocketSharkApp
import com.pocketshark.app.capture.CaptureState
import com.pocketshark.app.capture.VpnCaptureService
import com.pocketshark.app.capture.ACTION_START
import com.pocketshark.app.capture.ACTION_STOP
import com.pocketshark.app.data.model.CaptureStats
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Capture Dashboard screen.
 * Manages VPN consent, capture start/stop, and live statistics.
 */
class CaptureViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PocketSharkApp).packetRepository

    /** Current capture engine state */
    val captureState: StateFlow<CaptureState> = VpnCaptureService.state

    /** Live capture statistics (single source of truth from repository) */
    val stats: StateFlow<CaptureStats> = repository.stats

    /** Repository-level stats */
    val repoStats = repository.stats

    /** Protocol distribution for donut chart */
    val protocolDistribution = repository.stats.map { stats ->
        stats.protocolDistribution
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Alert count for security badge */
    val alertCount = repository.alerts.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Traffic data points for hero card waveform graph (sampled every stats update, fixed 30 points window) */
    private val _trafficHistory = MutableStateFlow<List<Float>>(List(30) { 0f })
    val trafficHistory: StateFlow<List<Float>> = _trafficHistory.asStateFlow()

    /** Per-protocol sparkline data for stat cards */
    private val _packetSparkline = MutableStateFlow<List<Float>>(emptyList())
    val packetSparkline: StateFlow<List<Float>> = _packetSparkline.asStateFlow()

    private val _tcpSparkline = MutableStateFlow<List<Float>>(emptyList())
    val tcpSparkline: StateFlow<List<Float>> = _tcpSparkline.asStateFlow()

    private val _udpSparkline = MutableStateFlow<List<Float>>(emptyList())
    val udpSparkline: StateFlow<List<Float>> = _udpSparkline.asStateFlow()

    private val _dnsSparkline = MutableStateFlow<List<Float>>(emptyList())
    val dnsSparkline: StateFlow<List<Float>> = _dnsSparkline.asStateFlow()

    private val _alertSparkline = MutableStateFlow<List<Float>>(emptyList())
    val alertSparkline: StateFlow<List<Float>> = _alertSparkline.asStateFlow()

    private val _dataSparkline = MutableStateFlow<List<Float>>(emptyList())
    val dataSparkline: StateFlow<List<Float>> = _dataSparkline.asStateFlow()

    init {
        // Sample traffic rate on every stats update for hero graph & sparklines
        viewModelScope.launch {
            stats.collect { s ->
                _trafficHistory.update { list ->
                    (list + s.packetsPerSecond).takeLast(30)
                }
                _packetSparkline.update { list ->
                    (list + s.packetCount.toFloat()).takeLast(12)
                }
                _tcpSparkline.update { list ->
                    (list + s.tcpCount.toFloat()).takeLast(12)
                }
                _udpSparkline.update { list ->
                    (list + s.udpCount.toFloat()).takeLast(12)
                }
                _dnsSparkline.update { list ->
                    (list + s.dnsCount.toFloat()).takeLast(12)
                }
                _dataSparkline.update { list ->
                    (list + s.totalBytes.toFloat()).takeLast(12)
                }
            }
        }
        viewModelScope.launch {
            repository.alerts.collect { alerts ->
                _alertSparkline.update { list ->
                    (list + alerts.size.toFloat()).takeLast(12)
                }
            }
        }
    }

    /**
     * Prepare VPN consent. Returns the Intent to launch if consent is needed,
     * or null if already authorized.
     */
    fun prepareVpn(): Intent? {
        return VpnService.prepare(getApplication())
    }

    /**
     * Start the capture service after VPN consent is granted.
     */
    fun startCapture() {
        val context = getApplication<PocketSharkApp>()
        val intent = Intent(context, VpnCaptureService::class.java).apply {
            action = ACTION_START
        }
        context.startForegroundService(intent)
    }

    /**
     * Stop the capture service.
     */
    fun stopCapture() {
        val context = getApplication<PocketSharkApp>()
        val intent = Intent(context, VpnCaptureService::class.java).apply {
            action = ACTION_STOP
        }
        context.startService(intent)
    }

    /**
     * Toggle capture state: start if idle/stopped, stop if running.
     * Returns the VPN consent Intent if needed, null otherwise.
     */
    fun toggleCapture(): Intent? {
        return when (captureState.value) {
            is CaptureState.Running -> {
                stopCapture()
                null
            }
            else -> {
                val vpnIntent = prepareVpn()
                if (vpnIntent == null) {
                    startCapture()
                }
                vpnIntent
            }
        }
    }

    fun clearData() {
        repository.clear()
        VpnCaptureService.resetState()
        _trafficHistory.value = List(30) { 0f }
    }
}
