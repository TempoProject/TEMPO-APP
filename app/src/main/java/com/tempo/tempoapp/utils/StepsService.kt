package com.tempo.tempoapp.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.google.android.gms.location.LocationServices
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.StepsRecordModel
import com.tempo.tempoapp.data.model.Utils
import com.tempo.tempoapp.data.model.WeatherForecast
import com.tempo.tempoapp.data.model.toTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit


/**
 * Service class to handle periodic fetching and storing of steps data from HealthConnect.
 * This service runs in the foreground to ensure it's not killed by the system.
 */
class StepsService : Service() {

    companion object {
        private val TAG = StepsService::class.java.simpleName
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val healthConnectManager =
            (this.applicationContext as TempoApplication).healthConnectManager

        val stepsRecordRepository =
            (this.applicationContext as TempoApplication).container.stepsRecordRepository

        val weatherForecastRepository =
            (this.applicationContext as TempoApplication).container.weatherForecastRepository

        val utilsRepository =
            (this.applicationContext as TempoApplication).container.utilsRepository

        val permission = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )

        Log.d(TAG, "Starting foreground service")
        startForegroundService(notificationManager)
        CoroutineScope(Dispatchers.IO).launch {

            Log.d(TAG, "Fetching steps data")
            if (healthConnectManager.hasAllPermissions(permission)) {

                val latestUpdate = utilsRepository.getLatestUpdate()
                var instantStartTime = Instant.now().minusSeconds(1800)

                if (latestUpdate != null && latestUpdate < Instant.now().toEpochMilli()) {
                    instantStartTime = Instant.ofEpochMilli(latestUpdate)
                }
                println("start time $instantStartTime")
                val instantNow = Instant.now()
                println("end time $instantNow")

                val list =
                    healthConnectManager.readSteps(instantStartTime, instantNow)
                        .toMutableList()

                if (list.isNotEmpty() && (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    LocationServices.getFusedLocationProviderClient(applicationContext).lastLocation.addOnSuccessListener { location ->
                        CoroutineScope(Dispatchers.IO).launch {
                            if (location != null) {
                                location.latitude
                                location.longitude

                                Log.d(TAG, "Location: ${location.latitude}, ${location.longitude}")


                                val response =
                                    WeatherForecastApi.retrofitService.getWeatherForecast(
                                        location.latitude.toString(),
                                        location.longitude.toString()
                                    )

                                response.body()?.let { data ->
                                    val weatherForecast = WeatherForecast(
                                        Instant.now().epochSecond,
                                        data.weather[0].main,
                                        data.weather[0].description,
                                        data.main.temp,
                                        data.main.feels_like,
                                        data.main.temp_min,
                                        data.main.temp_max,
                                        data.main.pressure,
                                        data.main.humidity,
                                        data.wind.speed,
                                        data.wind.deg,
                                        data.wind.gust
                                    )
                                    weatherForecastRepository.insertItem(weatherForecast)
                                }
                            }
                        }
                    }
                }

                Log.i(TAG, "Fetched ${list.size} steps data")
                list.forEach {
                    stepsRecordRepository.insertItem(
                        StepsRecordModel(
                            steps = it.count,
                            date = it.startTime.toTimestamp(ChronoUnit.DAYS),
                            startTime = it.startTime.toEpochMilli(),
                            endTime = it.endTime.toEpochMilli()
                        )
                    )
                }

                Log.d(TAG, "Updating latest update time")
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
        }.invokeOnCompletion { stopForegroundService() }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startForegroundService(notificationManager: NotificationManager) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                sendNotification(notificationManager),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else
            startForeground(1, sendNotification(notificationManager))
    }

    private fun sendNotification(notificationManager: NotificationManager): Notification {
        val title = getString(R.string.steps_notification_channel_name)
        val notification = NotificationCompat.Builder(
            applicationContext,
            getString(R.string.steps_notification_channel_id)
        )
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        notificationManager.notify(1, notification)
        return notification
    }

    private fun stopForegroundService() {
        Log.d(TAG, "Stopping foreground service")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}