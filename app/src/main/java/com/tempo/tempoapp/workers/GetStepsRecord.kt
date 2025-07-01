package com.tempo.tempoapp.workers

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.toTimestamp
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import java.time.Instant
import java.time.temporal.ChronoUnit


class GetStepsRecord(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val healthConnectManager = HealthConnectClient.getOrCreate(ctx)
    private val stepsRecordRepository = (ctx as TempoApplication).container.stepsRecordRepository

    override suspend fun doWork(): Result {

        val startTime = Instant.now().minus(720, ChronoUnit.MINUTES)
        val endTime = Instant.now()

        readStepsRecord(
            healthConnectManager,
            startTime,
            endTime,
            stepsRecordRepository
        )

        return Result.success()
    }

    private suspend fun readStepsRecord(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant,
        repository: StepsRecordRepository
    ) {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = client.readRecords(request)

        response.records.forEach { record ->
            repository.insertItem(
                com.tempo.tempoapp.data.model.StepsRecord(
                    recordId = record.metadata.id,
                    date = record.startTime.toTimestamp(ChronoUnit.DAYS),
                    startTime = record.startTime.toEpochMilli(),
                    endTime = record.endTime.toEpochMilli(),
                    steps = record.count,
                )
            )
        }
    }
}