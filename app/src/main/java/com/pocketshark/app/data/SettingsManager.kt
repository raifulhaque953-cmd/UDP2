package com.pocketshark.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages all application settings and preferences, persisting them to SharedPreferences.
 * Provides live StateFlow streams for reactive UI updates.
 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "pocketshark_settings"

        // Keys
        private const val KEY_MTU = "capture_mtu"
        private const val KEY_PRIMARY_DNS = "primary_dns"
        private const val KEY_SECONDARY_DNS = "secondary_dns"
        private const val KEY_SUBNET = "subnet_address"
        private const val KEY_PROMISCUOUS = "promiscuous_mode"
        private const val KEY_DNS_RESOLVE = "dns_resolve"
        private const val KEY_AUTO_SAVE = "auto_save_pcap"
        private const val KEY_GLOW_INTENSITY = "glow_intensity"
        private const val KEY_REFRESH_INTERVAL = "refresh_interval"
        private const val KEY_AUDIO_ALERTS = "audio_alerts"
        private const val KEY_NOTIFICATION_PRIORITY = "notification_priority"
        private const val KEY_CAPTURE_PROFILE = "capture_profile"
        private const val KEY_MIN_ALERT_SEVERITY = "min_alert_severity"
        private const val KEY_REDUCED_MOTION = "reduced_motion"

        // Defaults
        const val DEFAULT_MTU = 1500
        const val DEFAULT_PRIMARY_DNS = "8.8.8.8"
        const val DEFAULT_SECONDARY_DNS = "8.8.4.4"
        const val DEFAULT_SUBNET = "10.0.0.2"
        const val DEFAULT_PROMISCUOUS = true
        const val DEFAULT_DNS_RESOLVE = true
        const val DEFAULT_AUTO_SAVE = false
        const val DEFAULT_GLOW_INTENSITY = "High"
        const val DEFAULT_REFRESH_INTERVAL = 1000
        const val DEFAULT_AUDIO_ALERTS = false
        const val DEFAULT_NOTIFICATION_PRIORITY = "Low"
        const val DEFAULT_CAPTURE_PROFILE = "Balanced"
        const val DEFAULT_MIN_ALERT_SEVERITY = "Medium"
        const val DEFAULT_REDUCED_MOTION = false
    }

    // Live state streams
    private val _mtu = MutableStateFlow(prefs.getInt(KEY_MTU, DEFAULT_MTU))
    val mtu: StateFlow<Int> = _mtu.asStateFlow()

    private val _primaryDns = MutableStateFlow(prefs.getString(KEY_PRIMARY_DNS, DEFAULT_PRIMARY_DNS) ?: DEFAULT_PRIMARY_DNS)
    val primaryDns: StateFlow<String> = _primaryDns.asStateFlow()

    private val _secondaryDns = MutableStateFlow(prefs.getString(KEY_SECONDARY_DNS, DEFAULT_SECONDARY_DNS) ?: DEFAULT_SECONDARY_DNS)
    val secondaryDns: StateFlow<String> = _secondaryDns.asStateFlow()

    private val _subnet = MutableStateFlow(prefs.getString(KEY_SUBNET, DEFAULT_SUBNET) ?: DEFAULT_SUBNET)
    val subnet: StateFlow<String> = _subnet.asStateFlow()

    private val _promiscuous = MutableStateFlow(prefs.getBoolean(KEY_PROMISCUOUS, DEFAULT_PROMISCUOUS))
    val promiscuous: StateFlow<Boolean> = _promiscuous.asStateFlow()

    private val _dnsResolve = MutableStateFlow(prefs.getBoolean(KEY_DNS_RESOLVE, DEFAULT_DNS_RESOLVE))
    val dnsResolve: StateFlow<Boolean> = _dnsResolve.asStateFlow()

    private val _autoSave = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SAVE, DEFAULT_AUTO_SAVE))
    val autoSave: StateFlow<Boolean> = _autoSave.asStateFlow()

    private val _glowIntensity = MutableStateFlow(prefs.getString(KEY_GLOW_INTENSITY, DEFAULT_GLOW_INTENSITY) ?: DEFAULT_GLOW_INTENSITY)
    val glowIntensity: StateFlow<String> = _glowIntensity.asStateFlow()

    private val _refreshInterval = MutableStateFlow(prefs.getInt(KEY_REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL))
    val refreshInterval: StateFlow<Int> = _refreshInterval.asStateFlow()

    private val _audioAlerts = MutableStateFlow(prefs.getBoolean(KEY_AUDIO_ALERTS, DEFAULT_AUDIO_ALERTS))
    val audioAlerts: StateFlow<Boolean> = _audioAlerts.asStateFlow()

    private val _notificationPriority = MutableStateFlow(prefs.getString(KEY_NOTIFICATION_PRIORITY, DEFAULT_NOTIFICATION_PRIORITY) ?: DEFAULT_NOTIFICATION_PRIORITY)
    val notificationPriority: StateFlow<String> = _notificationPriority.asStateFlow()

    private val _captureProfile = MutableStateFlow(prefs.getString(KEY_CAPTURE_PROFILE, DEFAULT_CAPTURE_PROFILE) ?: DEFAULT_CAPTURE_PROFILE)
    val captureProfile: StateFlow<String> = _captureProfile.asStateFlow()

    private val _minAlertSeverity = MutableStateFlow(prefs.getString(KEY_MIN_ALERT_SEVERITY, DEFAULT_MIN_ALERT_SEVERITY) ?: DEFAULT_MIN_ALERT_SEVERITY)
    val minAlertSeverity: StateFlow<String> = _minAlertSeverity.asStateFlow()

    private val _reducedMotion = MutableStateFlow(prefs.getBoolean(KEY_REDUCED_MOTION, DEFAULT_REDUCED_MOTION))
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    fun updateMtu(value: Int) {
        prefs.edit().putInt(KEY_MTU, value).apply()
        _mtu.value = value
    }

    fun updatePrimaryDns(value: String) {
        prefs.edit().putString(KEY_PRIMARY_DNS, value).apply()
        _primaryDns.value = value
    }

    fun updateSecondaryDns(value: String) {
        prefs.edit().putString(KEY_SECONDARY_DNS, value).apply()
        _secondaryDns.value = value
    }

    fun updateSubnet(value: String) {
        prefs.edit().putString(KEY_SUBNET, value).apply()
        _subnet.value = value
    }

    fun updatePromiscuous(value: Boolean) {
        prefs.edit().putBoolean(KEY_PROMISCUOUS, value).apply()
        _promiscuous.value = value
    }

    fun updateDnsResolve(value: Boolean) {
        prefs.edit().putBoolean(KEY_DNS_RESOLVE, value).apply()
        _dnsResolve.value = value
    }

    fun updateAutoSave(value: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE, value).apply()
        _autoSave.value = value
    }

    fun updateGlowIntensity(value: String) {
        prefs.edit().putString(KEY_GLOW_INTENSITY, value).apply()
        _glowIntensity.value = value
    }

    fun updateRefreshInterval(value: Int) {
        prefs.edit().putInt(KEY_REFRESH_INTERVAL, value).apply()
        _refreshInterval.value = value
    }

    fun updateAudioAlerts(value: Boolean) {
        prefs.edit().putBoolean(KEY_AUDIO_ALERTS, value).apply()
        _audioAlerts.value = value
    }

    fun updateNotificationPriority(value: String) {
        prefs.edit().putString(KEY_NOTIFICATION_PRIORITY, value).apply()
        _notificationPriority.value = value
    }

    fun updateCaptureProfile(value: String) {
        prefs.edit().putString(KEY_CAPTURE_PROFILE, value).apply()
        _captureProfile.value = value
    }

    fun updateMinAlertSeverity(value: String) {
        prefs.edit().putString(KEY_MIN_ALERT_SEVERITY, value).apply()
        _minAlertSeverity.value = value
    }

    fun updateReducedMotion(value: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, value).apply()
        _reducedMotion.value = value
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _mtu.value = DEFAULT_MTU
        _primaryDns.value = DEFAULT_PRIMARY_DNS
        _secondaryDns.value = DEFAULT_SECONDARY_DNS
        _subnet.value = DEFAULT_SUBNET
        _promiscuous.value = DEFAULT_PROMISCUOUS
        _dnsResolve.value = DEFAULT_DNS_RESOLVE
        _autoSave.value = DEFAULT_AUTO_SAVE
        _glowIntensity.value = DEFAULT_GLOW_INTENSITY
        _refreshInterval.value = DEFAULT_REFRESH_INTERVAL
        _audioAlerts.value = DEFAULT_AUDIO_ALERTS
        _notificationPriority.value = DEFAULT_NOTIFICATION_PRIORITY
        _captureProfile.value = DEFAULT_CAPTURE_PROFILE
        _minAlertSeverity.value = DEFAULT_MIN_ALERT_SEVERITY
        _reducedMotion.value = DEFAULT_REDUCED_MOTION
    }
}
