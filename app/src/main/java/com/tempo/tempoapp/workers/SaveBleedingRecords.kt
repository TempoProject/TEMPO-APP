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
 * SaveBleedingRecords is a Worker class responsible for saving bleeding records to Firebase.
 *
 * @param appContext The application context.
 * @param params The parameters to configure the worker.
 */
class SaveBleedingRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    companion object {
        private val TAG = SaveBleedingRecords::class.java.simpleName
    }

    // Bleeding repository to access bleeding records
    private val bleedingRepository =
        (appContext.applicationContext as TempoApplication).container.bleedingRepository

    private val preferences = AppPreferencesManager(appContext)

    // Firebase database reference
    //private val databaseRef =
    //  FirebaseRealtimeDatabase.instance


    /**
     * Performs the background work to save bleeding records to Firebase.
     *
     * @return The Result of the work.
     */
    override suspend fun doWork(): Result {


        // Get Firebase installation ID
        //val id = FirebaseInstallations.getInstance().id.await()

        // Get bleeding records to be sent
        val bleedingRecords = bleedingRepository.getAllBleedingToSent(isSent = false)

        val pid = preferences.userId.first() ?: return Result.failure()
        val sessionId = preferences.sessionId.first() ?: return Result.failure()

        Log.d(TAG, "Bleeding records to be sent: ${bleedingRecords.size}")
        bleedingRecords.forEach { record ->

            StoreDataApi.retrofitService.postLogs(
                pid, sessionId,
                mapOf(
                    "type" to "Event",
                    "id" to record.id.toString(),
                    "bleeding_site" to record.bleedingSite,
                    "cause" to record.bleedingCause,
                    "pain_scale" to record.painScale,
                    "event_type" to (record.eventType ?: ""),
                    "medication_type" to (record.medicationType ?: ""),
                    "dose" to (record.dose ?: ""),
                    "dosage_unit" to (record.dosageUnit ?: ""),
                    "lot_number" to (record.lotNumber ?: ""),
                    "treatment" to record.treatment,
                    "date" to record.date,
                    "timestamp" to record.timestamp,
                    "note" to (record.note ?: ""),


                    )
            ).let { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "Record ${record.id} sent successfully")
                } else {
                    Log.e(TAG, "Failed to send record ${record.id}: ${response.errorBody()}")
                    return Result.failure()
                }
            }
            // Save bleeding record to Firebase
            //databaseRef.child("bleedings").child(id).child(record.id.toString())
            //  .setValue(record)
            /*
            try {
                Log.d(TAG, it.toBleedingEventJson(it.id).toString())
                val response =
                    PostgresApi.retrofitService.postBleedingEvent(it.toBleedingEventJson(it.id))
                Log.d(TAG, response.toString())
            } catch (err: Exception) {
                Log.e(TAG, err.message!!)
                return Result.failure()
            }*/

            // Update record status to indicate it has been sent
            bleedingRepository.updateItem(record.copy(isSent = true))

        }
        return Result.success()
    }
}