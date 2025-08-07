package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "infusion_event")
data class InfusionEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "reason")
    val reason: String?,
    @ColumnInfo(name = "drug_name")
    val drugName: String?,
    @ColumnInfo(name = "dose_in_units")
    val dose: String?,
    @ColumnInfo(name = "dosage_unit")
    val dosageUnit: String?,
    @ColumnInfo(name = "batch_number")
    val batchNumber: String?,
    @ColumnInfo(name = "note")
    val note: String?,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)