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
import com.tempo.tempoapp.data.model.BloodGlucose
import com.tempo.tempoapp.data.model.BloodPressure
import com.tempo.tempoapp.data.model.BodyFat
import com.tempo.tempoapp.data.model.BodyWaterMass
import com.tempo.tempoapp.data.model.BoneMass
import com.tempo.tempoapp.data.model.Distance
import com.tempo.tempoapp.data.model.FloorsClimbed
import com.tempo.tempoapp.data.model.OxygenSaturation
import com.tempo.tempoapp.data.model.StepsRecordModel
import com.tempo.tempoapp.data.model.TotalCaloriesBurned
import com.tempo.tempoapp.data.model.Utils
import com.tempo.tempoapp.data.model.WeatherForecast
import com.tempo.tempoapp.data.model.Weight
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

        val totalCaloriesBurnedRepository =
            (this.applicationContext as TempoApplication).container.totalCaloriesBurnedRepository

        val bloodGlucoseRepository =
            (this.applicationContext as TempoApplication).container.bloodGlucoseRepository

        val bloodPressureRepository =
            (this.applicationContext as TempoApplication).container.bloodPressureRepository

        val bodyFatRepository =
            (this.applicationContext as TempoApplication).container.bodyFatRepository

        val bodyWaterMassRepository =
            (this.applicationContext as TempoApplication).container.bodyWaterMassRepository

        val boneMassRepository =
            (this.applicationContext as TempoApplication).container.boneMassRepository

        val distanceRepository =
            (this.applicationContext as TempoApplication).container.distanceRepository

        val elevationGainedRepository =
            (this.applicationContext as TempoApplication).container.elevationGainedRepository

        val floorsClimbedRepository =
            (this.applicationContext as TempoApplication).container.floorsClimbedRepository

        val oxygenSaturationRepository =
            (this.applicationContext as TempoApplication).container.oxygenSaturationRepository

        val respiratoryRateRepository =
            (this.applicationContext as TempoApplication).container.respiratoryRateRepository

        val sleepSessionRepository =
            (this.applicationContext as TempoApplication).container.sleepSessionRepository

        val weightRepository =
            (this.applicationContext as TempoApplication).container.weightRepository

        val weatherForecastRepository =
            (this.applicationContext as TempoApplication).container.weatherForecastRepository

        val heartRateRepository =
            (this.applicationContext as TempoApplication).container.heartRateRepository

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
                var instantStartTime = Instant.now().minusSeconds(43500)

                if (latestUpdate != null && latestUpdate < Instant.now().toEpochMilli()) {
                    instantStartTime = Instant.ofEpochMilli(latestUpdate)
                }
                println("start time $instantStartTime")
                val instantNow = Instant.now()
                println("end time $instantNow")


                val stepsRecords =
                    healthConnectManager.readSteps(instantStartTime, instantNow).toMutableList()


                // Total Calories Burned
                healthConnectManager.totalCaloriesBurned(instantStartTime, instantNow)
                    .toMutableList().also {
                        Log.d(TAG, "Fetched ${it.size} total calories burned data")
                    }.forEach {
                        Log.d(TAG, "Total calories burned: ${it.energy.inKilocalories}")
                        // Insert into database
                        totalCaloriesBurnedRepository.insertItem(
                            TotalCaloriesBurned(
                                recordId = it.metadata.id,
                                calories = it.energy.inCalories,
                                date = it.startTime.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.startTime.toEpochMilli(),
                                endTime = it.endTime.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.bloodGlucose(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} blood glucose data")
                    }.forEach {
                        Log.d(TAG, "Blood glucose: ${it.level.inMillimolesPerLiter}")

                        bloodGlucoseRepository.insertItem(
                            BloodGlucose(
                                recordId = it.metadata.id,
                                bloodGlucose = it.level.inMillimolesPerLiter,
                                date = it.time.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.time.toEpochMilli(),
                                endTime = it.time.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.bloodPressure(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} blood pressure data")
                    }.forEach {
                        Log.d(
                            TAG,
                            "Blood pressure: ${it.systolic.inMillimetersOfMercury}/${it.diastolic.inMillimetersOfMercury}"
                        )

                        val measurementLocation = when (it.measurementLocation) {
                            1 -> "MEASUREMENT_LOCATION_LEFT_WRIST"
                            2 -> "MEASUREMENT_LOCATION_RIGHT_WRIST"
                            3 -> "MEASUREMENT_LOCATION_LEFT_UPPER_ARM"
                            4 -> "MEASUREMENT_LOCATION_RIGHT_UPPER_ARM"
                            else -> "MEASUREMENT_LOCATION_UNKNOWN"
                        }

                        bloodPressureRepository.insertItem(
                            BloodPressure(
                                recordId = it.metadata.id,
                                systolic = it.systolic.inMillimetersOfMercury,
                                diastolic = it.diastolic.inMillimetersOfMercury,
                                measurementLocation = measurementLocation,
                                date = it.time.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.time.toEpochMilli(),
                                endTime = it.time.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.heartRate(instantStartTime, instantNow).toMutableList().also {
                    Log.d(TAG, "Fetched ${it.size} heart rate data")
                }.forEach { heartRateRecord ->

                    heartRateRecord.samples.forEach {
                        Log.d(TAG, "Heart rate: ${it.beatsPerMinute} bpm")
                        // Insert into database
                        heartRateRepository.insertItem(
                            com.tempo.tempoapp.data.model.HeartRate(
                                recordId = heartRateRecord.metadata.id,
                                heartRate = it.beatsPerMinute,
                                instant = it.time.toEpochMilli(),
                                date = it.time.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.time.toEpochMilli(),
                                endTime = it.time.toEpochMilli()
                            )
                        )
                    }
                }

                healthConnectManager.bodyFat(instantStartTime, instantNow).toMutableList().also {
                    Log.d(TAG, "Fetched ${it.size} body fat data")
                }.forEach {
                    Log.d(TAG, "Body fat: ${it.percentage.value}")
                    // Insert into database
                    bodyFatRepository.insertItem(
                        BodyFat(
                            recordId = it.metadata.id,
                            bodyFat = it.percentage.value,
                            date = it.time.toTimestamp(ChronoUnit.DAYS),
                            startTime = it.time.toEpochMilli(),
                            endTime = it.time.toEpochMilli()
                        )
                    )
                }


                healthConnectManager.bodyWaterMass(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} body water mass data")
                    }.forEach {
                        Log.d(TAG, "Body water mass: ${it.mass.inKilograms}")
                        // Insert into database
                        bodyWaterMassRepository.insertItem(
                            BodyWaterMass(
                                recordId = it.metadata.id,
                                bodyWaterMass = it.mass.inKilograms,
                                date = it.time.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.time.toEpochMilli(),
                                endTime = it.time.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.boneMass(instantStartTime, instantNow).toMutableList().also {
                    Log.d(TAG, "Fetched ${it.size} bone mass data")
                }.forEach {
                    Log.d(
                        TAG, "Bone mass: ${
                            it.mass.inKilograms
                        }"
                    )

                    boneMassRepository.insertItem(
                        BoneMass(
                            recordId = it.metadata.id,
                            boneMass = it.mass.inKilograms,
                            date = it.time.toTimestamp(ChronoUnit.DAYS),
                            startTime = it.time.toEpochMilli(),
                            endTime = it.time.toEpochMilli(
                            )
                        )
                    )
                }

                healthConnectManager.distanceRecords(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} distance data")
                    }.forEach {
                        Log.d(TAG, "Distance: ${it.distance.inMeters}")
                        distanceRepository.insertItem(
                            Distance(
                                recordId = it.metadata.id,
                                distance = it.distance.inMeters,
                                date = it.startTime.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.startTime.toEpochMilli(),
                                endTime = it.endTime.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.elevationGained(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} elevation gained data")
                    }.forEach {
                        Log.d(TAG, "Elevation gained: ${it.elevation.inMeters}")
                        elevationGainedRepository.insertItem(
                            com.tempo.tempoapp.data.model.ElevationGained(
                                recordId = it.metadata.id,
                                elevationGained = it.elevation.inMeters,
                                date = it.startTime.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.startTime.toEpochMilli(),
                                endTime = it.endTime.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.floorsClimbed(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} floors climbed data")
                    }.forEach {
                        Log.d(TAG, "Floors climbed: ${it.floors}")
                        // Insert into database
                        floorsClimbedRepository.insertItem(
                            FloorsClimbed(
                                recordId = it.metadata.id,
                                floorsClimbed = it.floors.toInt(),
                                date = it.startTime.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.startTime.toEpochMilli(),
                                endTime = it.endTime.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.oxygenSaturation(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} oxygen saturation data")
                    }.forEach {
                        Log.d(TAG, "Oxygen saturation: ${it.percentage.value}")
                        // Insert into database
                        oxygenSaturationRepository.insertItem(
                            OxygenSaturation(
                                recordId = it.metadata.id,
                                oxygenSaturation = it.percentage.value,
                                date = it.time.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.time.toEpochMilli(),
                                endTime = it.time.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.respiratoryRate(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} respiratory rate data")
                    }.forEach {
                        Log.d(TAG, "Respiratory rate: ${it.rate}")
                        // Insert into database
                        respiratoryRateRepository.insertItem(
                            com.tempo.tempoapp.data.model.RespiratoryRate(
                                recordId = it.metadata.id,
                                respiratoryRate = it.rate,
                                date = it.time.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.time.toEpochMilli(),
                                endTime = it.time.toEpochMilli()
                            )
                        )
                    }

                healthConnectManager.sleepSessions(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} sleep session data")
                    }.forEach {


                        sleepSessionRepository.insertItem(
                            com.tempo.tempoapp.data.model.SleepSession(
                                recordId = it.metadata.id,
                                startTime = it.startTime.toEpochMilli(),
                                endTime = it.endTime.toEpochMilli(),
                                duration = it.endTime.toEpochMilli() - it.startTime.toEpochMilli(),
                            )
                        )
                        // TODO store all data collected in the database
                    }

                healthConnectManager.weightRecord(instantStartTime, instantNow).toMutableList()
                    .also {
                        Log.d(TAG, "Fetched ${it.size} weight data")
                    }.forEach {
                        Log.d(TAG, "Weight: ${it.weight.inKilograms}")
                        // Insert into database
                        weightRepository.insertItem(
                            Weight(
                                recordsId = it.metadata.id,
                                weight = it.weight.inKilograms,
                                date = it.time.toTimestamp(ChronoUnit.DAYS),
                                startTime = it.time.toEpochMilli(),
                                endTime = it.time.toEpochMilli()
                            )
                        )
                    }


                if (stepsRecords.isNotEmpty() && (ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
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
                                        location.latitude.toString(), location.longitude.toString()
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

                Log.i(TAG, "Fetched ${stepsRecords.size} steps data")
                stepsRecords.forEach {
                    Log.d(TAG, "Steps metadata: ${it.metadata.id}")
                    stepsRecordRepository.insertItem(
                        StepsRecordModel(
                            recordId = it.metadata.id,
                            steps = it.count,
                            date = it.startTime.toTimestamp(ChronoUnit.DAYS),
                            startTime = it.startTime.toEpochMilli(),
                            endTime = it.endTime.toEpochMilli()
                        )
                    )
                }

                Log.d(TAG, "Updating latest update time")
                if (latestUpdate == null) utilsRepository.insertItem(
                    Utils(
                        latestUpdate = if (stepsRecords.isNotEmpty()) stepsRecords.last().endTime.toEpochMilli()
                        else instantStartTime.toEpochMilli()
                    )
                )
                else {
                    if (stepsRecords.isNotEmpty()) utilsRepository.updateItem(
                        Utils(
                            id = 1, latestUpdate = stepsRecords.last().endTime.toEpochMilli()
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
        } else startForeground(1, sendNotification(notificationManager))
    }

    private fun sendNotification(notificationManager: NotificationManager): Notification {
        val title = getString(R.string.steps_notification_channel_name)
        val notification = NotificationCompat.Builder(
            applicationContext, getString(R.string.steps_notification_channel_id)
        ).setContentTitle(title).setTicker(title).setContentText(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setOngoing(true).build()

        notificationManager.notify(1, notification)
        return notification
    }

    private fun stopForegroundService() {
        Log.d(TAG, "Stopping foreground service")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}