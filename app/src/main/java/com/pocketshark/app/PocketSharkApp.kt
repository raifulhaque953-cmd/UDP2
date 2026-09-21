package com.pocketshark.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.pocketshark.app.data.SettingsManager
import com.pocketshark.app.data.repository.PacketRepository
import com.pocketshark.app.data.repository.PacketRepositoryImpl

class PocketSharkApp : Application() {

    val packetRepository: PacketRepository by lazy { PacketRepositoryImpl() }
    val settingsManager: SettingsManager by lazy { SettingsManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val captureChannel = NotificationChannel(
                CAPTURE_CHANNEL_ID,
                "Packet Capture",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when packet capture is active"
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Security threat notifications"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(captureChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CAPTURE_CHANNEL_ID = "pocketshark_capture"
        const val ALERT_CHANNEL_ID = "pocketshark_alerts"

        lateinit var instance: PocketSharkApp
            private set
    }
}
