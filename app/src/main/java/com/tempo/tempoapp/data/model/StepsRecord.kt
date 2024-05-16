package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity(tableName = "steps")
data class StepsRecord(
    @PrimaryKey(true)
    val id: Int = 0,
    @ColumnInfo(name = "steps")
    val steps: Long,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "startTime")
    val startTime: Long,
    @ColumnInfo(name = "endTime")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class StepsRecordToJson(
    val id: Int = 0,
    val steps: Long,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
)

fun StepsRecord.toStepsRecordToJson(id: Int = 0): StepsRecordToJson =
    StepsRecordToJson(id, steps, date, startTime, endTime)

fun Instant.toTimestamp(unit: ChronoUnit) = this.truncatedTo(unit).toEpochMilli()