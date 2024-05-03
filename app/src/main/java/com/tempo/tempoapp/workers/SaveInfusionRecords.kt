package com.tempo.tempoapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.installations.FirebaseInstallations
import com.tempo.tempoapp.TempoApplication
import kotlinx.coroutines.tasks.await

class SaveInfusionRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val TAG = javaClass.simpleName
    private val infusionRepository =
        (appContext.applicationContext as TempoApplication).container.infusionRepository
    private val databaseRef =
        (appContext.applicationContext as TempoApplication).database


    override suspend fun doWork(): Result {
        val infusionRecords = infusionRepository.getAllInfusionToSent(isSent = false)

        val id = FirebaseInstallations.getInstance().id.await()



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
            infusionRepository.updateItem(record.copy(isSent = true))
        }
        return Result.success()
    }
}