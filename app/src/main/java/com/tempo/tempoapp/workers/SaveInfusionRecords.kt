package com.tempo.tempoapp.workers

import AppPreferencesManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.utils.StoreDataApi
import kotlinx.coroutines.flow.first

/**
 * SaveInfusionRecords is a Worker class responsible for saving infusion records to Firebase.
 *
 * @param appContext The application context.
 * @param params The parameters to configure the worker.
 */
class SaveInfusionRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        private val TAG = SaveInfusionRecords::class.java.simpleName
    }

    // Infusion repository to access infusion records
    private val infusionRepository =
        (appContext.applicationContext as TempoApplication).container.infusionRepository

    private val prophylaxisRepository =
        (appContext.applicationContext as TempoApplication).container.prophylaxisResponseRepository
    private val preferences = AppPreferencesManager(appContext)

    // Firebase database reference
    /*private val databaseRef =
        FirebaseRealtimeDatabase.instance*/


    /**
     * Performs the background work to save infusion records to Firebase.
     *
     * @return The Result of the work.
     */
    override suspend fun doWork(): Result {

        // Get Firebase installation ID
        // val id = FirebaseInstallations.getInstance().id.await()

        // Get infusion records to be sent
        val infusionRecords = infusionRepository.getAllInfusionToSent(isSent = false)
        val prophylaxisRecords =
            prophylaxisRepository.getAllToSent(isSent = false)

        val pid = preferences.userId.first() ?: return Result.failure()
        val sessionId = preferences.sessionId.first() ?: return Result.failure()


        Log.d(TAG, "Infusion records to be sent: ${infusionRecords.size}")
        // Save infusion records to Firebase
        infusionRecords.forEach { record ->

            StoreDataApi.retrofitService.postLogs(
                pid, sessionId,
                mapOf(
                    "type" to "InfusionEvent",
                    "id" to record.id.toString(),
                    "reason" to (record.reason ?: ""),
                    "drug_name" to (record.drugName ?: ""),
                    "dose_in_units" to (record.dose ?: ""),
                    "dosage_unit" to (record.dosageUnit ?: ""),
                    "batch_number" to (record.batchNumber ?: ""),
                    "note" to (record.note ?: ""),
                    "timestamp" to record.timestamp,
                    "date" to record.date
                )
            ).let { response ->
                if (response.isSuccessful) {
                    Log.d(
                        "SaveInfusionRecords",
                        "Record ${record.id} sent successfully"
                    )
                } else {
                    Log.e(
                        "SaveInfusionRecords",
                        "Failed to send record ${record.id}: ${response.errorBody()}"
                    )
                    return Result.failure()
                }
            }

            /*databaseRef.child("infusions").child(id).child(record.id.toString())
                .setValue(record)

            try {
                val response =
                    PostgresApi.retrofitService.postInfusionEvent(it.toInfusionEventJson(it.id))
                Log.d(TAG, response.toString())
            } catch (err: Exception) {
                Log.e(TAG, err.message!!)
                return Result.failure()
            }

             */

            // Update record status to indicate it has been sent
            infusionRepository.updateItem(record.copy(isSent = true))
        }

        Log.d(TAG, "Prophylaxis records to be sent: ${prophylaxisRecords.size}")

        prophylaxisRecords.forEach { record ->

            StoreDataApi.retrofitService.postLogs(
                pid, sessionId,
                mapOf(
                    "type" to "ProphylaxisEvent",
                    "id" to record.id.toString(),
                    "reminder_date_time" to record.reminderDateTime,
                    "responded" to record.responded.toString(),
                    "response_date_time" to record.responseDateTime,
                    "date" to record.date,
                    "reminder_type" to record.reminderType,
                    "drug_name" to record.drugName,
                    "dosage" to record.dosage,
                    "dosage_unit" to record.dosageUnit,
                    "postponed_alarm_id" to record.postponedAlarmId.toString(),
                )
            ).let { response ->
                if (response.isSuccessful) {
                    Log.d(
                        "SaveInfusionRecords",
                        "Record ${record.id} sent successfully"
                    )
                } else {
                    Log.e(
                        "SaveInfusionRecords",
                        "Failed to send record ${record.id}: ${response.errorBody()}"
                    )
                    return Result.failure()
                }
            }

            prophylaxisRepository.updateItem(record.copy(isSent = true))
        }


        return Result.success()
    }
}