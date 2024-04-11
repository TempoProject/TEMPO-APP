package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

val events = setOf(
    "Infusione",
    "Visita di controllo",
    "Refill farmaco"
)

// TODO add worker-id

@Entity(tableName = "reminder")
data class ReminderEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("uuid_notification")
    val uuid: UUID,
    @ColumnInfo("event")
    val event: String,
    @ColumnInfo("timestamp")
    val timestamp: Long
)
