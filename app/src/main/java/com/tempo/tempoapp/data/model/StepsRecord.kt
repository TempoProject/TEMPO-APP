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
)

fun Instant.toTimestamp(unit: ChronoUnit) = this.truncatedTo(unit).toEpochMilli()