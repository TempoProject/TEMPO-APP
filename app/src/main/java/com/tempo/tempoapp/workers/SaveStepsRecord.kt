package com.tempo.tempoapp.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_MIN
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import androidx.core.app.NotificationCompat
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import java.time.Instant

class SaveStepsRecord(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    private val healthConnectManager =
        (appContext.applicationContext as TempoApplication).healthConnectManager

    private val stepsRecordRepository =
        (appContext.applicationContext as TempoApplication).container.stepsRecordRepository

    private val permission = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo(""))
        if (healthConnectManager.hasAllPermissions(permission)) {
            val list =
                healthConnectManager.readSteps(Instant.now().minusSeconds(1800), Instant.now())
            list.forEach {
                stepsRecordRepository.insertItem(
                    com.tempo.tempoapp.data.model.StepsRecord(
                        steps = it.count,
                        startTime = it.startTime.toString(),
                        endTime = it.endTime.toString()
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
        val id = applicationContext.getString(R.string.app_name)
        val title = "Passi"
        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("Invio passi: $progress...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            //.addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        val channel = NotificationChannel(id, title, IMPORTANCE_MIN)
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(1, notification)
        return notification
    }
}