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

    // Firebase database reference
    private val databaseRef =
        FirebaseRealtimeDatabase.instance


    /**
     * Performs the background work to save infusion records to Firebase.
     *
     * @return The Result of the work.
     */
    override suspend fun doWork(): Result {

        // Get Firebase installation ID
        val id = FirebaseInstallations.getInstance().id.await()

        // Get infusion records to be sent
        val infusionRecords = infusionRepository.getAllInfusionToSent(isSent = false)


        Log.d(TAG, "Infusion records to be sent: ${infusionRecords.size}")
        // Save infusion records to Firebase
        infusionRecords.forEach { record ->

            databaseRef.child("infusions").child(id).child(record.id.toString())
                .setValue(record)
            /*
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
        return Result.success()
    }
}