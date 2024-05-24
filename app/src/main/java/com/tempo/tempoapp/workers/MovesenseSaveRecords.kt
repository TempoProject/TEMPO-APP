package com.tempo.tempoapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await

/**
 * MovesenseSaveRecords is a Worker class responsible for saving records related to Movesense data.
 *
 * @param appContext The application context.
 * @param params The parameters to configure the worker.
 */
class MovesenseSaveRecords(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    /**
     * Performs the background work to save Movesense records.
     *
     * @return The Result of the work.
     */
    override suspend fun doWork(): Result {
        val stopLogging =
            OneTimeWorkRequestBuilder<MovesenseWorker>().addTag("configureMovesense")
                .setInputData(Data.Builder().putInt("state", 2).build())
                .build()
        WorkManager.getInstance(applicationContext).enqueue(stopLogging).await()

        val configLogger = OneTimeWorkRequestBuilder<MovesenseWorker>().addTag("configureMovesense")
            .setInputData(Data.Builder().putInt("state", 1).build())
            .build()
        WorkManager.getInstance(applicationContext).enqueue(configLogger).await()

        val saveRecords = OneTimeWorkRequestBuilder<MovesenseWorker>().addTag("configureMovesense")
            .setInputData(Data.Builder().putInt("state", 4).build())
            .build()
        WorkManager.getInstance(applicationContext).enqueue(saveRecords).await()


        val startLogging =
            OneTimeWorkRequestBuilder<MovesenseWorker>().addTag("configureMovesense")
                .setInputData(Data.Builder().putInt("state", 3).build())
                .build()
        WorkManager.getInstance(applicationContext).enqueue(startLogging).await()


        return Result.success()
    }
}
