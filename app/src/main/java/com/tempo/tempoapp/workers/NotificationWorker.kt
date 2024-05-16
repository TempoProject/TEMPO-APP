package com.tempo.tempoapp.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tempo.tempoapp.R
import kotlin.random.Random

class NotificationWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {
        val inputData = inputData.getString("EVENT")
        val title = "Reminder"
        val notification = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.channel_reminder)
        )
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(inputData)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
            .build()

        notificationManager.notify(Random.Default.nextInt(), notification)
        return Result.success()
    }
}