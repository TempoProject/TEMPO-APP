package com.tempo.tempoapp.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.Utils
import com.tempo.tempoapp.data.model.toTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit


class StepsService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val notificationManager =
        TempoApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val healthConnectManager =
            (TempoApplication.instance.applicationContext as TempoApplication).healthConnectManager

        val stepsRecordRepository =
            (TempoApplication.instance.applicationContext as TempoApplication).container.stepsRecordRepository

        val utilsRepository =
            (TempoApplication.instance.applicationContext as TempoApplication).container.utilsRepository

        val permission = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )

        startForegroundService()
        serviceScope.launch {
            println("in foreground service")
            if (healthConnectManager.hasAllPermissions(permission)) {

                val latestUpdate = utilsRepository.getLatestUpdate()
                //Log.d(TAG, latestUpdate.toString())

                var instantStartTime = Instant.now().minusSeconds(1800)
                //Log.d(TAG, "instant default: $instantStartTime")
                if (latestUpdate != null) {
                    instantStartTime = Instant.ofEpochMilli(latestUpdate)
                    //  Log.d(TAG, "instant update: $instantStartTime")
                }
                val instantNow = Instant.now()

                val list =
                    healthConnectManager.readSteps(instantStartTime, instantNow)
                        .toMutableList()
                //Log.d(TAG, "full list: $list")
                /*try {
                    if (list.last().startTime == instantThirtyMinutes)
                        list.removeLast()
                    Log.d(TAG, "list after removeLast(): $list")
                } catch (err: NoSuchElementException) {
                    Log.e(TAG, err.message!!)
                }*/
                /*if (list.isEmpty())
                    stepsRecordRepository.insertItem(
                        com.tempo.tempoapp.data.model.StepsRecord(
                            steps = 100,
                            date = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
                            startTime = Instant.now().toEpochMilli(),
                            endTime = Instant.now().toEpochMilli()
                        )
                    )*/
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
            }
            stopForegroundService()
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startForegroundService() {
        startForeground(1, sendNotification(""))
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

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}