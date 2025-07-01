package com.tempo.tempoapp

import AppPreferencesManager
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
import com.tempo.tempoapp.workers.GetStepsRecord
import com.tempo.tempoapp.workers.MovesenseWorker
import com.tempo.tempoapp.workers.SaveBleedingRecords
import com.tempo.tempoapp.workers.SaveInfusionRecords
import com.tempo.tempoapp.workers.SaveStepsRecords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Application class for Tempo app.
 */
class TempoApplication : Application() {
    lateinit var container: AppContainer

    val preferences: AppPreferencesManager by lazy {
        AppPreferencesManager(this)
    }

    private lateinit var workManager: WorkManager
    private lateinit var notificationManager: NotificationManager

    /**
     * Called when the application is starting.
     */
    override fun onCreate() {
        super.onCreate()
        notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as
                    NotificationManager

        /**
         * Create notification channels.
         */
        val notificationChannelSendSteps = NotificationChannel(
            getString(R.string.steps_notification_channel_id),
            getString(R.string.steps_notification_channel_name),
            NotificationManager.IMPORTANCE_NONE
        )
        val notificationChannelReminder = NotificationChannel(
            getString(R.string.reminder_notification_channel_id),
            getString(R.string.reminder_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationChannelMovesense = NotificationChannel(
            getString(R.string.movesense_notification_channel_id),
            getString(R.string.movesense_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationChannelProphylaxis = NotificationChannel(
            getString(R.string.prophylaxis_notification_channel_id),
            getString(R.string.prophylaxis_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannels(
            listOf(
                notificationChannelSendSteps,
                notificationChannelReminder,
                notificationChannelMovesense,
                notificationChannelProphylaxis
            )
        )
        container = AppDataContainer(this)
        workManager = WorkManager.getInstance(this)

        CoroutineScope(Dispatchers.IO).launch {
            val isFirstLaunch = preferences.isFirstLaunch.first()
            //preferences = AppPreferencesManager(this)
            /**
             * Schedule periodic work for saving records.
             */
            withContext(Dispatchers.Main) {
                val constraints =
                    Constraints(
                        requiresBatteryNotLow = true,
                        requiredNetworkType = NetworkType.CONNECTED
                    )
                val stepsRecords =
                    PeriodicWorkRequestBuilder<SaveStepsRecords>(
                        30,
                        TimeUnit.MINUTES
                    ).setConstraints(
                        constraints
                    ).build()

                val bleedingRecords =
                    PeriodicWorkRequestBuilder<SaveBleedingRecords>(
                        30,
                        TimeUnit.MINUTES
                    ).setConstraints(
                        constraints
                    ).build()

                val infusionRecords =
                    PeriodicWorkRequestBuilder<SaveInfusionRecords>(
                        30,
                        TimeUnit.MINUTES
                    ).setConstraints(
                        constraints
                    ).build()

                val saveAccelerometer =
                    PeriodicWorkRequestBuilder<MovesenseWorker>(
                        30,
                        TimeUnit.MINUTES
                    ).setConstraints(
                        constraints
                    ).setInputData(Data.Builder().putInt("state", 6).build()).build()


                if (!isFirstLaunch)
                    workManager.enqueueUniquePeriodicWork(
                        "GetStepsRecord",
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                        PeriodicWorkRequestBuilder<GetStepsRecord>(15, TimeUnit.MINUTES)
                            .build()
                    )




                workManager.enqueueUniquePeriodicWork(
                    "StepsRecords",
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    stepsRecords
                )

                workManager.enqueueUniquePeriodicWork(
                    "BleedingRecords",
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    bleedingRecords
                )

                workManager.enqueueUniquePeriodicWork(
                    "InfusionRecords",
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    infusionRecords
                )

                workManager.enqueueUniquePeriodicWork(
                    "AccelerometerRecords",
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    saveAccelerometer
                )
            }
        }
    }

}

/**
 * Firebase Realtime Database object.
 */
object FirebaseRealtimeDatabase {

    val instance: DatabaseReference
        get() = Firebase.database(BuildConfig.FIREBASE_URL).reference
}

val Context.preferences: AppPreferencesManager
    get() = (applicationContext as TempoApplication).preferences
