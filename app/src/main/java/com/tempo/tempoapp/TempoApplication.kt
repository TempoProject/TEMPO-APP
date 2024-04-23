package com.tempo.tempoapp

import android.app.AlarmManager
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

    lateinit var alarm: AlarmManager
    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        val notificationChannelSendSteps = NotificationChannel(
            "Passi",
            "Invio passi",
            NotificationManager.IMPORTANCE_NONE
        )
        val notificationChannelReminder = NotificationChannel(
            "Reminder",
            "Promemoria",
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
        alarm = getSystemService(ALARM_SERVICE) as AlarmManager
    }

    companion object {
        lateinit var instance: TempoApplication
            private set
    }
}