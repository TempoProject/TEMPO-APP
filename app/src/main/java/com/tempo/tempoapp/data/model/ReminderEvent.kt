package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

val events = setOf(
    "Infusione",
    "Visita di controllo",
    "Refill farmaco"
)

@Entity(tableName = "reminder")
data class ReminderEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("id_calendar")
    val idCalendar: Long,
    @ColumnInfo("event")
    val event: String,
    @ColumnInfo("timestamp")
    val timestamp: Long,
    @ColumnInfo("is_periodic")
    val isPeriodic: Boolean,
    @ColumnInfo("period")
    val period: Long,
    @ColumnInfo("time_unit")
    val timeUnit: String
) : Serializable
