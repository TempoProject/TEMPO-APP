package com.tempo.tempoapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.toBleedingEventJson
import com.tempo.tempoapp.utils.PostgresApi

class SaveBleedingRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val TAG = javaClass.simpleName
    private val bleedingRepository =
        (appContext.applicationContext as TempoApplication).container.bleedingRepository

    override suspend fun doWork(): Result {
        val bleedingRecords = bleedingRepository.getAllBleedingToSent(isSent = false)
        bleedingRecords.forEach {
            try {
                Log.d(TAG, it.toBleedingEventJson(it.id).toString())
                val response =
                    PostgresApi.retrofitService.postBleedingEvent(it.toBleedingEventJson(it.id))
                Log.d(TAG, response.toString())
            } catch (err: Exception) {
                Log.e(TAG, err.message!!)
                return Result.failure()
            }
            bleedingRepository.updateItem(it.copy(isSent = true))

        }
        return Result.success()
    }
}