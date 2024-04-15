package com.tempo.tempoapp.workers

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.Utils
import com.tempo.tempoapp.data.model.toTimestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

class SaveStepsRecord(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val TAG = javaClass.simpleName

    private val healthConnectManager =
        (appContext.applicationContext as TempoApplication).healthConnectManager

    private val stepsRecordRepository =
        (appContext.applicationContext as TempoApplication).container.stepsRecordRepository

    private val utilsRepository =
        (appContext.applicationContext as TempoApplication).container.utilsRepository

    private val permission = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    override suspend fun doWork(): Result {

        setForeground(createForegroundInfo(""))
        if (healthConnectManager.hasAllPermissions(permission)) {

            val latestUpdate = utilsRepository.getLatestUpdate()
            Log.d(TAG, latestUpdate.toString())

            var instantStartTime = Instant.now().minusSeconds(1800)
            Log.d(TAG, "instant default: $instantStartTime")
            if (latestUpdate != null) {
                instantStartTime = Instant.ofEpochMilli(latestUpdate)
                Log.d(TAG, "instant update: $instantStartTime")
            }
            val instantNow = Instant.now()

            val list =
                healthConnectManager.readSteps(instantStartTime, instantNow)
                    .toMutableList()
            Log.d(TAG, "full list: $list")
            /*try {
                if (list.last().startTime == instantThirtyMinutes)
                    list.removeLast()
                Log.d(TAG, "list after removeLast(): $list")
            } catch (err: NoSuchElementException) {
                Log.e(TAG, err.message!!)
            }*/
            list.forEach {
                stepsRecordRepository.insertItem(
                    com.tempo.tempoapp.data.model.StepsRecord(
                        steps = it.count,
                        date = it.startTime.toTimestamp(ChronoUnit.DAYS),
                        startTime = it.startTime.toTimestamp(ChronoUnit.MILLIS),
                        endTime = it.endTime.toTimestamp(ChronoUnit.MILLIS)
                    )
                )
            }
            if (latestUpdate == null)
                utilsRepository.insertItem(
                    Utils(
                        latestUpdate = if (list.isNotEmpty())
                            list.last().endTime.toEpochMilli()
                        else
                            instantStartTime.toEpochMilli()
                    )
                )
            else {
                if (list.isNotEmpty())
                    utilsRepository.updateItem(
                        Utils(
                            id = 1,
                            latestUpdate = list.last().endTime.toEpochMilli()
                        )
                    )
            }

            return Result.success()
        }
        return Result.failure()
    }


    private fun createForegroundInfo(progress: String): ForegroundInfo {
        //applicationContext.getString(R.string.)
        //val cancel = applicationContext.getString(R.string.cancel_download)
        // This PendingIntent can be used to cancel the worker
        //val intent = WorkManager.getInstance(applicationContext)
        //  .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary

        return if (SDK_INT >= Q) {
            ForegroundInfo(1, sendNotification(progress), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else
            ForegroundInfo(1, sendNotification(progress))
    }

    private fun sendNotification(progress: String): Notification {
        val title = "Passi"
        val notification = NotificationCompat.Builder(applicationContext, "Passi")
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("Invio passi: $progress...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        notificationManager.notify(1, notification)
        return notification
    }
}