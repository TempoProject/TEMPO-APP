package com.tempo.tempoapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.WorkManager
import com.tempo.tempoapp.data.AppContainer
import com.tempo.tempoapp.data.AppDataContainer
import com.tempo.tempoapp.data.healthconnect.HealthConnectManager

class TempoApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    lateinit var healthConnectManager: HealthConnectManager
    lateinit var workManager: WorkManager
    private lateinit var notificationManager: NotificationManager
    override fun onCreate() {
        super.onCreate()
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        val notificationChannelSendSteps = NotificationChannel(
            getString(R.string.channel_steps),
            getString(R.string.channel_name_steps),
            NotificationManager.IMPORTANCE_MIN
        )
        val notificationChannelReminder = NotificationChannel(
            getString(R.string.channel_reminder),
            getString(R.string.channel_name_reminder),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannels(
            listOf(
                notificationChannelSendSteps,
                notificationChannelReminder
            )
        )
        container = AppDataContainer(this)
        healthConnectManager = HealthConnectManager(this)
        workManager = WorkManager.getInstance(this)
    }
}