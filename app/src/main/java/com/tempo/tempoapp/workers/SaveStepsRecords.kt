package com.tempo.tempoapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.installations.FirebaseInstallations
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.toStepsRecordToJson
import kotlinx.coroutines.tasks.await

class SaveStepsRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val TAG = javaClass.simpleName


    private val stepsRecordRepository =
        (appContext.applicationContext as TempoApplication).container.stepsRecordRepository

    private val databaseRef =
        (appContext.applicationContext as TempoApplication).database


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

        val stepsRecords = stepsRecordRepository.getAllDaySteps(
            isSent = false
        )


        val id = FirebaseInstallations.getInstance().id.await()

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