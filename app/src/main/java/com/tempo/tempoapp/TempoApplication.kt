package com.tempo.tempoapp

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.tempo.tempoapp.data.AppContainer
import com.tempo.tempoapp.data.AppDataContainer
import com.tempo.tempoapp.data.healthconnect.HealthConnectManager
import com.tempo.tempoapp.workers.MovesenseWorker
import com.tempo.tempoapp.workers.SaveBleedingRecords
import com.tempo.tempoapp.workers.SaveInfusionRecords
import com.tempo.tempoapp.workers.SaveStepsRecords
import java.util.concurrent.TimeUnit

class TempoApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    lateinit var healthConnectManager: HealthConnectManager
    lateinit var workManager: WorkManager
    private lateinit var notificationManager: NotificationManager
    lateinit var database: DatabaseReference
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
        val notificationChannelMovesense = NotificationChannel(
            "Movesense",
            "Movesense",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannels(
            listOf(
                notificationChannelSendSteps,
                notificationChannelReminder,
                notificationChannelMovesense
            )
        )
        container = AppDataContainer(this)
        healthConnectManager = HealthConnectManager(this)
        workManager = WorkManager.getInstance(this)
        alarm = getSystemService(ALARM_SERVICE) as AlarmManager
        database =
            Firebase.database("https://tempo-app-94a6c-default-rtdb.europe-west1.firebasedatabase.app/").reference

        val constraints =
            Constraints(requiresBatteryNotLow = true, requiredNetworkType = NetworkType.CONNECTED)
        val stepsRecords =
            PeriodicWorkRequestBuilder<SaveStepsRecords>(30, TimeUnit.MINUTES).setConstraints(
                constraints
            ).build()

        val bleedingRecords =
            PeriodicWorkRequestBuilder<SaveBleedingRecords>(30, TimeUnit.MINUTES).setConstraints(
                constraints
            ).build()

        val infusionRecords =
            PeriodicWorkRequestBuilder<SaveInfusionRecords>(30, TimeUnit.MINUTES).setConstraints(
                constraints
            ).build()

        val saveAccelerometer =
            PeriodicWorkRequestBuilder<MovesenseWorker>(30, TimeUnit.MINUTES).setConstraints(
                constraints
            ).setInputData(Data.Builder().putInt("state", 6).build()).build()

        workManager.enqueueUniquePeriodicWork(
            "StepsRecords",
            ExistingPeriodicWorkPolicy.KEEP,
            stepsRecords
        )

        workManager.enqueueUniquePeriodicWork(
            "BleedingRecords",
            ExistingPeriodicWorkPolicy.KEEP,
            bleedingRecords
        )

        workManager.enqueueUniquePeriodicWork(
            "InfusionRecords",
            ExistingPeriodicWorkPolicy.KEEP,
            infusionRecords
        )

        workManager.enqueueUniquePeriodicWork(
            "AccelerometerRecords",
            ExistingPeriodicWorkPolicy.KEEP,
            saveAccelerometer
        )
    }

    companion object {
        lateinit var instance: TempoApplication
            private set
    }
}