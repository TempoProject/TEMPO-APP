package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "infusion_event")
data class InfusionEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "treatment")
    val treatment: String,
    @ColumnInfo(name = "infusion_site")
    val infusionSite: String,
    @ColumnInfo(name = "dose_in_units")
    val doseUnits: Int,
    @ColumnInfo(name = "lot_number")
    val lotNumber: Int,
    @ColumnInfo(name = "note")
    val note: String?,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)


data class InfusionEventJson(
    val id: Int = 0,
    val treatment: String,
    val infusion_site: String,
    val dose_in_units: Int,
    val lot_number: Int,
    val note: String?,
    val timestamp: Long,
    val date: Long,
)

fun InfusionEvent.toInfusionEventJson(id: Int = 0): InfusionEventJson =
    InfusionEventJson(id, treatment, infusionSite, doseUnits, lotNumber, note, timestamp, date)