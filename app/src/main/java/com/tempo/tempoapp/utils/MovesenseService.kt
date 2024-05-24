package com.tempo.tempoapp.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.movesense.mds.Mds
import com.movesense.mds.MdsException
import com.movesense.mds.MdsNotificationListener
import com.movesense.mds.MdsSubscription
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.Movesense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * MovesenseService is a Service responsible for managing Movesense device connectivity.
 * It subscribes to Movesense events and handles notifications for device connection status changes.
 *
 * @property mds The Movesense instance used for communication with Movesense devices.
 * @property mdsSub The Movesense subscription used for receiving device connection events.
 * @property serviceScope The CoroutineScope used for launching asynchronous tasks.
 * @property notificationManager The NotificationManager instance used for displaying notifications.
 */
class MovesenseService : Service() {

    private val mds: Mds = Mds.builder().build(TempoApplication.instance.applicationContext)

    private var mdsSub: MdsSubscription? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val notificationManager =
        TempoApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    /**
     * Starts the MovesenseService and handles incoming intents.
     *
     * @param intent The Intent supplied to the service.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The type of service handling requested, either START_NOT_STICKY, START_STICKY, START_REDELIVER_INTENT.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val movesenseRepository =
            (TempoApplication.instance.applicationContext as TempoApplication).container.movesenseRepository
        var deviceInfo = Movesense("", "", false)
        serviceScope.launch {
            deviceInfo = serviceScope.async { movesenseRepository.getDeviceInfo() }.await()

        }
        startForegroundService()
        mdsSub?.unsubscribe()
        mdsSub = mds.subscribe(
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

    /**
     * Stops the MovesenseService and cleans up resources.
     */
    override fun onDestroy() {
        Toast.makeText(this, "Service done", Toast.LENGTH_SHORT).show()
        stopForegroundService()

    }

    /**
     * Starts the service in the foreground.
     */
    private fun startForegroundService() {
        startForeground(2, sendNotification("Movesense collegato"))
    }

    /**
     * Creates and sends a notification.
     *
     * @param content The content of the notification.
     * @return The notification that was sent.
     */
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

    /**
     * Stops the service from running in the foreground.
     */
    private fun stopForegroundService() {
        Log.d(javaClass.simpleName, "unsubscribe")
        mdsSub?.unsubscribe()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}
