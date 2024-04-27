package com.tempo.tempoapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.toInfusionEventJson
import com.tempo.tempoapp.utils.PostgresApi

class SaveInfusionRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val TAG = javaClass.simpleName
    private val infusionRepository =
        (appContext.applicationContext as TempoApplication).container.infusionRepository

    override suspend fun doWork(): Result {
        val infusionRecords = infusionRepository.getAllInfusionToSent(isSent = false)
        infusionRecords.forEach {
            try {
                val response =
                    PostgresApi.retrofitService.postInfusionEvent(it.toInfusionEventJson(it.id))
                Log.d(TAG, response.toString())
            } catch (err: Exception) {
                Log.e(TAG, err.message!!)
                return Result.failure()
            }
            infusionRepository.updateItem(it.copy(isSent = true))
        }
        return Result.success()
    }
}