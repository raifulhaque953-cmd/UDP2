package com.pocketshark.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pocketshark.app.PocketSharkApp
import com.pocketshark.app.data.SettingsManager

/**
 * ViewModel for the Settings screen.
 * Exposes observable states from SettingsManager and coordinates saves and validation.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager: SettingsManager = (application as PocketSharkApp).settingsManager

    // Expose all preference settings
    val mtu = settingsManager.mtu
    val primaryDns = settingsManager.primaryDns
    val secondaryDns = settingsManager.secondaryDns
    val subnet = settingsManager.subnet
    val promiscuous = settingsManager.promiscuous
    val dnsResolve = settingsManager.dnsResolve
    val autoSave = settingsManager.autoSave
    val glowIntensity = settingsManager.glowIntensity
    val refreshInterval = settingsManager.refreshInterval
    val audioAlerts = settingsManager.audioAlerts
    val notificationPriority = settingsManager.notificationPriority
    val captureProfile = settingsManager.captureProfile
    val minAlertSeverity = settingsManager.minAlertSeverity
    val reducedMotion = settingsManager.reducedMotion

    fun setMtu(value: Int) {
        settingsManager.updateMtu(value)
    }

    fun setPrimaryDns(value: String) {
        settingsManager.updatePrimaryDns(value)
    }

    fun setSecondaryDns(value: String) {
        settingsManager.updateSecondaryDns(value)
    }

    fun setSubnet(value: String) {
        settingsManager.updateSubnet(value)
    }

    fun setPromiscuous(value: Boolean) {
        settingsManager.updatePromiscuous(value)
    }

    fun setDnsResolve(value: Boolean) {
        settingsManager.updateDnsResolve(value)
    }

    fun setAutoSave(value: Boolean) {
        settingsManager.updateAutoSave(value)
    }

    fun setGlowIntensity(value: String) {
        settingsManager.updateGlowIntensity(value)
    }

    fun setRefreshInterval(value: Int) {
        settingsManager.updateRefreshInterval(value)
    }

    fun setAudioAlerts(value: Boolean) {
        settingsManager.updateAudioAlerts(value)
    }

    fun setNotificationPriority(value: String) {
        settingsManager.updateNotificationPriority(value)
    }

    fun setCaptureProfile(value: String) {
        settingsManager.updateCaptureProfile(value)
    }

    fun setMinAlertSeverity(value: String) {
        settingsManager.updateMinAlertSeverity(value)
    }

    fun setReducedMotion(value: Boolean) {
        settingsManager.updateReducedMotion(value)
    }

    fun resetToDefaults() {
        settingsManager.resetToDefaults()
    }
}
