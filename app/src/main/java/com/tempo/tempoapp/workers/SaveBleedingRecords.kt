package com.tempo.tempoapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.installations.FirebaseInstallations
import com.tempo.tempoapp.FirebaseRealtimeDatabase
import com.tempo.tempoapp.TempoApplication
import kotlinx.coroutines.tasks.await

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

    // Firebase database reference
    private val databaseRef =
        FirebaseRealtimeDatabase.instance


    /**
     * Performs the background work to save bleeding records to Firebase.
     *
     * @return The Result of the work.
     */
    override suspend fun doWork(): Result {

        // Get Firebase installation ID
        val id = FirebaseInstallations.getInstance().id.await()

        // Get bleeding records to be sent
        val bleedingRecords = bleedingRepository.getAllBleedingToSent(isSent = false)

        Log.d(TAG, "Bleeding records to be sent: ${bleedingRecords.size}")
        bleedingRecords.forEach { record ->
            // Save bleeding record to Firebase
            databaseRef.child("bleedings").child(id).child(record.id.toString())
                .setValue(record)
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