package com.tempo.tempoapp

import AppPreferencesManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

    companion object {

        fun startHealthConnectWorkManager(context: Context) {
            Log.d("TempoApplication", "Starting GetStepsRecord WorkManager")
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "GetStepsRecord",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<GetStepsRecord>(15, TimeUnit.MINUTES).build()
            )
        }

        fun startFirebaseSyncWorkManagers(context: Context) {
            Log.d("TempoApplication", "Starting Firebase sync WorkManagers")

            val constraints = Constraints(
                requiresBatteryNotLow = true,
                requiredNetworkType = NetworkType.CONNECTED
            )

            val workManager = WorkManager.getInstance(context)

            workManager.enqueueUniquePeriodicWork(
                "StepsRecords",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<SaveStepsRecords>(30, TimeUnit.MINUTES)
                    .setConstraints(constraints).build()
            )

            workManager.enqueueUniquePeriodicWork(
                "BleedingRecords",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<SaveBleedingRecords>(30, TimeUnit.MINUTES)
                    .setConstraints(constraints).build()
            )

            workManager.enqueueUniquePeriodicWork(
                "InfusionRecords",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<SaveInfusionRecords>(30, TimeUnit.MINUTES)
                    .setConstraints(constraints).build()
            )

            workManager.enqueueUniquePeriodicWork(
                "AccelerometerRecords",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<MovesenseWorker>(30, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .setInputData(Data.Builder().putInt("state", 6).build()).build()
            )
        }

        fun stopHealthConnectWorkManager(context: Context) {
            Log.d("TempoApplication", "Stopping GetStepsRecord WorkManager")
            WorkManager.getInstance(context).cancelUniqueWork("GetStepsRecord")
        }
    }

    /**
     * Called when the application is starting.
     */
    override fun onCreate() {
        super.onCreate()
        notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as
                    NotificationManager

        initializeCrashlytics()

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
                if (!isFirstLaunch)
                    startHealthConnectWorkManager(this@TempoApplication)
                /*val constraints =
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

                 val saveAccelerometer =
                    PeriodicWorkRequestBuilder<MovesenseWorker>(
                        30,
                        TimeUnit.MINUTES
                    ).setConstraints(
                        constraints
                    ).setInputData(Data.Builder().putInt("state", 6).build()).build()

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
*/

                /*
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

             */
            }
        }

        configureUserIdIfLoggedIn()
    }

    private fun configureUserIdIfLoggedIn() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isLoggedIn = preferences.isLoggedIn.first()
                if (isLoggedIn) {
                    val userId = preferences.userId.first()
                    if (!userId.isNullOrEmpty()) {
                        setCrashlyticsUserId(userId)
                    }
                }
            } catch (e: Exception) {
                Log.e("TempoApplication", "Failed to configure Crashlytics userId", e)
            }
        }
    }

    private fun initializeCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true

            FirebaseCrashlytics.getInstance().setCustomKey("app_version", BuildConfig.VERSION_NAME)
            FirebaseCrashlytics.getInstance().setCustomKey("app_version_code", BuildConfig.VERSION_CODE)
            FirebaseCrashlytics.getInstance().setCustomKey("build_type", BuildConfig.BUILD_TYPE)

            FirebaseCrashlytics.getInstance().log("Tempo App initialized successfully")

        } catch (e: Exception) {
            Log.e("TempoApplication", "Error initializing Crashlytics: ${e.message}")
        }
    }

    fun setCrashlyticsUserId(userId: String) {
        try {
            FirebaseCrashlytics.getInstance().setUserId("tempo_user_$userId")
            FirebaseCrashlytics.getInstance().setCustomKey("user_logged_in", true)
            FirebaseCrashlytics.getInstance().log("User logged in with ID: $userId")

            Log.d("TempoApplication", "Crashlytics user ID set: $userId")
        } catch (e: Exception) {
            Log.e("TempoApplication", "Failed to set Crashlytics user ID", e)
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
