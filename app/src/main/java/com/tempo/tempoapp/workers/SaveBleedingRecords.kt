package com.tempo.tempoapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.installations.FirebaseInstallations
import com.tempo.tempoapp.TempoApplication
import kotlinx.coroutines.tasks.await

class SaveBleedingRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val TAG = javaClass.simpleName
    private val bleedingRepository =
        (appContext.applicationContext as TempoApplication).container.bleedingRepository

    private val databaseRef =
        (appContext.applicationContext as TempoApplication).database


    override suspend fun doWork(): Result {

        val id = FirebaseInstallations.getInstance().id.await()

        val bleedingRecords = bleedingRepository.getAllBleedingToSent(isSent = false)
        bleedingRecords.forEach { record ->

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
            bleedingRepository.updateItem(record.copy(isSent = true))

        }
        return Result.success()
    }
}