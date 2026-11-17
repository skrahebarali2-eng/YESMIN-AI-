package com.yesmin.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.yesmin.ai.database.YesminDatabase
import com.yesmin.ai.settings.PreferenceManager

/**
 * YesminApplication manages application-level singletons and initializes
 * Android notification channels for ongoing voice assistant operations.
 */
class YesminApplication : Application() {

    lateinit var database: YesminDatabase
        private set

    lateinit var preferenceManager: PreferenceManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize local encrypted Room database
        database = YesminDatabase.getDatabase(this)

        // Initialize Keystore-backed PreferenceManager
        preferenceManager = PreferenceManager(this)

        // Create foreground service notification channel
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val voiceChannel = NotificationChannel(
                CHANNEL_VOICE_SERVICE,
                getString(R.string.channel_voice_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_voice_desc)
                setShowBadge(false)
            }

            val assistantChannel = NotificationChannel(
                CHANNEL_ASSISTANT_ALERTS,
                getString(R.string.channel_alerts_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_alerts_desc)
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(voiceChannel)
            manager?.createNotificationChannel(assistantChannel)
        }
    }

    companion object {
        const val CHANNEL_VOICE_SERVICE = "yesmin_voice_channel"
        const val CHANNEL_ASSISTANT_ALERTS = "yesmin_alerts_channel"

        lateinit var instance: YesminApplication
            private set
    }
}
