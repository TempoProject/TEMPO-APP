package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bleeding_event")
data class BleedingEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "bleeding_site")
    val bleedingSite: String,
    @ColumnInfo(name = "severity")
    val severity: String,
    @ColumnInfo(name = "pain_scale")
    val painScale: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Int,
    @ColumnInfo(name = "note")
    val note: String,

    // TODO add photo
)
