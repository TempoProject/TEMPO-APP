package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class StepsRecord(
    @PrimaryKey(true)
    val id: Int = 0,
    @ColumnInfo(name = "steps")
    val steps: Long,
    @ColumnInfo(name = "startTime")
    val startTime: String,
    @ColumnInfo(name = "endTime")
    val endTime: String,
)