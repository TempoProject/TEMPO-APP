package com.tempo.tempoapp.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.movesense.mds.Mds
import com.movesense.mds.MdsException
import com.movesense.mds.MdsNotificationListener
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.workers.MovesenseSaveRecords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MovesenseService : Service() {

    private val mds: Mds = Mds.builder().build(TempoApplication.instance.applicationContext)
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val notificationManager =
        TempoApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val movesenseRepository =
            (TempoApplication.instance.applicationContext as TempoApplication).container.movesenseRepository
        var deviceInfo = Movesense("", "", false)
        serviceScope.launch {
            deviceInfo = serviceScope.async { movesenseRepository.getDeviceInfo() }.await()

        }
        startForegroundService()
        mds.subscribe(
            Mds.URI_EVENTLISTENER, """{"Uri": "${Mds.URI_CONNECTEDDEVICES}"}""",
            object : MdsNotificationListener {
                override fun onNotification(p0: String?) {
                    println("onNotification: $p0")
                    println("deviceInfo: $deviceInfo")

                    val map = Gson().fromJson(p0, Map::class.java)


                    if (map["Method"] == "DEL") {
                        println("disconnected")
                        sendNotification("Movesense scollegato")
                        serviceScope.launch {
                            movesenseRepository.updateItem(
                                deviceInfo.copy(
                                    isConnected = false
                                )
                            )
                        }/*.invokeOnCompletion {

                            WorkManager.getInstance(applicationContext)
                                .cancelAllWorkByTag("onConfigure")
                        }*/
                    } else {
                        sendNotification("Movesense collegato")
                        println("connected")
                        serviceScope.launch {
                            movesenseRepository.updateItem(
                                deviceInfo.copy(
                                    isConnected = true
                                )
                            )
                        }/*.invokeOnCompletion {
                            // start worker
                            val flushData = PeriodicWorkRequestBuilder<MovesenseSaveRecords>(
                                repeatInterval = 20,
                                TimeUnit.MINUTES
                            ).addTag("onConfigure").build()
                            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                                "onConfigure",
                                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                                flushData
                            )
                        }*/
                    }
                }


                override fun onError(p0: MdsException?) {
                    println("data listener onError $p0")
                }

            })

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Toast.makeText(this, "Service done", Toast.LENGTH_SHORT).show()
        stopForegroundService()

    }


    private fun startForegroundService() {
        startForeground(1, sendNotification("Movesense collegato"))
    }

    private fun sendNotification(content: String): Notification {
        val title = "Movesense"
        val notification = NotificationCompat.Builder(applicationContext, "Movesense")
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        notificationManager.notify(2, notification)
        return notification
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}
