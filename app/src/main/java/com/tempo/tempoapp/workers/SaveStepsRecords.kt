package com.tempo.tempoapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.installations.FirebaseInstallations
import com.tempo.tempoapp.FirebaseRealtimeDatabase
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.toBloodGlucoseToJson
import com.tempo.tempoapp.data.model.toBloodPressureToJson
import com.tempo.tempoapp.data.model.toBodyFatToJson
import com.tempo.tempoapp.data.model.toBodyWaterMassToJson
import com.tempo.tempoapp.data.model.toBoneMassToJson
import com.tempo.tempoapp.data.model.toDistanceToJson
import com.tempo.tempoapp.data.model.toElevationGainedToJson
import com.tempo.tempoapp.data.model.toFloorsClimbedToJson
import com.tempo.tempoapp.data.model.toHeartRateToJson
import com.tempo.tempoapp.data.model.toOxygenSaturationToJson
import com.tempo.tempoapp.data.model.toRespiratoryRateToJson
import com.tempo.tempoapp.data.model.toSleepSessionToJson
import com.tempo.tempoapp.data.model.toStepsRecordToJson
import com.tempo.tempoapp.data.model.toTotalCaloriesBurnedToJson
import com.tempo.tempoapp.data.model.toWeatherForecastToJson
import com.tempo.tempoapp.data.model.toWeightToJson
import kotlinx.coroutines.tasks.await

/**
 * SaveStepsRecords is a Worker class responsible for saving steps records to Firebase.
 *
 * @param appContext The application context.
 * @param params The parameters to configure the worker.
 */
class SaveStepsRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {


    companion object {
        private val TAG = SaveStepsRecords::class.java.simpleName
    }

    private val context = appContext

    // Steps record repository to access steps records
    private val stepsRecordRepository =
        (appContext.applicationContext as TempoApplication).container.stepsRecordRepository
    private val weatherForecastRepository =
        (appContext.applicationContext as TempoApplication).container.weatherForecastRepository

    private val totalCaloriesBurnedRepository =
        (this.applicationContext as TempoApplication).container.totalCaloriesBurnedRepository

    private val bloodGlucoseRepository =
        (this.applicationContext as TempoApplication).container.bloodGlucoseRepository

    private val bloodPressureRepository =
        (this.applicationContext as TempoApplication).container.bloodPressureRepository

    private val heartRateRepository =
        (this.applicationContext as TempoApplication).container.heartRateRepository

    private val bodyFatRepository =
        (this.applicationContext as TempoApplication).container.bodyFatRepository

    private val bodyWaterMassRepository =
        (this.applicationContext as TempoApplication).container.bodyWaterMassRepository

    private val boneMassRepository =
        (this.applicationContext as TempoApplication).container.boneMassRepository

    private val distanceRepository =
        (this.applicationContext as TempoApplication).container.distanceRepository

    private val elevationGainedRepository =
        (this.applicationContext as TempoApplication).container.elevationGainedRepository

    private val floorsClimbedRepository =
        (this.applicationContext as TempoApplication).container.floorsClimbedRepository

    private val oxygenSaturationRepository =
        (this.applicationContext as TempoApplication).container.oxygenSaturationRepository

    private val respiratoryRateRepository =
        (this.applicationContext as TempoApplication).container.respiratoryRateRepository

    private val sleepSessionRepository =
        (this.applicationContext as TempoApplication).container.sleepSessionRepository

    private val weightRepository =
        (this.applicationContext as TempoApplication).container.weightRepository


    // Firebase database reference
    private val databaseRef =
        FirebaseRealtimeDatabase.instance


    /*
        private val healthConnectManager =
            (appContext.applicationContext as TempoApplication).healthConnectManager

        private val utilsRepository =
            (appContext.applicationContext as TempoApplication).container.utilsRepository

        private val permission = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )
        private val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager

     */

    /**
     * Performs the background work to save steps records to Firebase.
     *
     * @return The Result of the work.
     */
    override suspend fun doWork(): Result {


        /*        setForeground(createForegroundInfo(""))
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

         */

        // Get Firebase installation ID
        val id = FirebaseInstallations.getInstance().id.await()

        // Get weather forecasts to be sent
        val weatherForecasts = weatherForecastRepository.getAllUnsentWeatherForecasts()
        Log.d(TAG, "Weather forecasts to be sent: ${weatherForecasts.size}")

        // Save weather forecasts to Firebase
        weatherForecasts.forEach { forecast ->
            /* try {
                 val response = PostgresApi.retrofitService.postWeatherForecast(it.toWeatherForecastToJson(it.timestamp))
                 Log.d(TAG, response.toString())
             } catch (err: Exception) {
                 Log.e(TAG, err.message!!)
                 return Result.failure()
             }*/
            databaseRef.child("weather_forecast").child(id).child(forecast.timestamp.toString())
                .setValue(forecast.toWeatherForecastToJson(forecast.timestamp))
            weatherForecastRepository.updateItem(forecast.copy(isSent = true))
        }


        // Get steps records to be sent
        val stepsRecords = stepsRecordRepository.getAllDaySteps(
            isSent = false
        )

        Log.d(TAG, "Steps records to be sent: ${stepsRecords.size}")
        // Save steps records to Firebase
        stepsRecords.forEach { record ->
            /* try {
                 val response = PostgresApi.retrofitService.postSteps(it.toStepsRecordToJson(it.id))
                 Log.d(TAG, response.toString())
             } catch (err: Exception) {
                 Log.e(TAG, err.message!!)
                 return Result.failure()
             }*/
            databaseRef.child("steps").child(id).child(record.id.toString())
                .setValue(record.toStepsRecordToJson(record.id))
            stepsRecordRepository.updateItem(record.copy(isSent = true))
        }

        totalCaloriesBurnedRepository.getAllDayTotalCaloriesBurned(isSent = false)
            .forEach { record ->
                databaseRef.child("total_calories_burned").child(id).child(record.id.toString())
                    .setValue(record.toTotalCaloriesBurnedToJson(record.id))
                totalCaloriesBurnedRepository.updateItem(record.copy(isSent = true))
            }

        bloodGlucoseRepository.getAllDayBloodGlucose(isSent = false).forEach { record ->
            databaseRef.child("blood_glucose").child(id).child(record.id.toString())
                .setValue(record.toBloodGlucoseToJson(record.id))
            bloodGlucoseRepository.updateItem(record.copy(isSent = true))
        }

        bloodPressureRepository.getAllDayBloodPressure(isSent = false).forEach { record ->
            databaseRef.child("blood_pressure").child(id).child(record.id.toString())
                .setValue(record.toBloodPressureToJson(record.id))
            bloodPressureRepository.updateItem(record.copy(isSent = true))
        }

        bodyFatRepository.getAllDayBodyFat(isSent = false).forEach { record ->
            databaseRef.child("body_fat").child(id).child(record.id.toString())
                .setValue(record.toBodyFatToJson(record.id))
            bodyFatRepository.updateItem(record.copy(isSent = true))
        }

        bodyWaterMassRepository.getAllDayBodyWaterMass(isSent = false).forEach { record ->
            databaseRef.child("body_water_mass").child(id).child(record.id.toString())
                .setValue(record.toBodyWaterMassToJson(record.id))
            bodyWaterMassRepository.updateItem(record.copy(isSent = true))
        }

        boneMassRepository.getAllDayBoneMass(isSent = false).forEach { record ->
            databaseRef.child("bone_mass").child(id).child(record.id.toString())
                .setValue(record.toBoneMassToJson(record.id))
            boneMassRepository.updateItem(record.copy(isSent = true))
        }

        distanceRepository.getAllDayDistance(isSent = false).forEach { record ->
            databaseRef.child("distance").child(id).child(record.id.toString())
                .setValue(record.toDistanceToJson(record.id))
            distanceRepository.updateItem(record.copy(isSent = true))
        }

        elevationGainedRepository.getAllDayElevationGained(isSent = false).forEach { record ->
            databaseRef.child("elevation_gained").child(id).child(record.id.toString())
                .setValue(record.toElevationGainedToJson(record.id))
            elevationGainedRepository.updateItem(record.copy(isSent = true))
        }

        floorsClimbedRepository.getAllDayFloorsClimbed(isSent = false).forEach { record ->
            databaseRef.child("floors_climbed").child(id).child(record.id.toString())
                .setValue(record.toFloorsClimbedToJson(record.id))
            floorsClimbedRepository.updateItem(record.copy(isSent = true))
        }

        oxygenSaturationRepository.getAllDayOxygenSaturation(isSent = false).forEach { record ->
            databaseRef.child("oxygen_saturation").child(id).child(record.id.toString())
                .setValue(record.toOxygenSaturationToJson(record.id))
            oxygenSaturationRepository.updateItem(record.copy(isSent = true))
        }

        respiratoryRateRepository.getAllDayRespiratoryRate(isSent = false).forEach { record ->
            databaseRef.child("respiratory_rate").child(id).child(record.id.toString())
                .setValue(record.toRespiratoryRateToJson(record.id))
            respiratoryRateRepository.updateItem(record.copy(isSent = true))
        }

        sleepSessionRepository.getAllDaySleepSessions(isSent = false).forEach { record ->
            databaseRef.child("sleep_session").child(id).child(record.id.toString())
                .setValue(record.toSleepSessionToJson(record.id))
            sleepSessionRepository.updateItem(record.copy(isSent = true))
        }

        weightRepository.getAllDayWeight(isSent = false).forEach { record ->
            databaseRef.child("weight").child(id).child(record.id.toString())
                .setValue(record.toWeightToJson(record.id))
            weightRepository.updateItem(record.copy(isSent = true))
        }

        heartRateRepository.getAllDayHeartRate(isSent = false).forEach { record ->
            databaseRef.child("heart_rate").child(id).child(record.id.toString())
                .setValue(record.toHeartRateToJson(record.id))
            heartRateRepository.updateItem(record.copy(isSent = true))
        }

        return Result.success()
    }
    /*

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
     */
}