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


data class InfusionEventJson(
    val id: Int = 0,
    val reason: String?,
    val drugName: String?,
    val dose: String?,
    val batchNumber: String?,
    val note: String?,
    val timestamp: Long,
    val date: Long,
)


/**
 * Extension function to convert an [InfusionEvent] object to an [InfusionEventJson] object.
 *
 * @param id The unique identifier of the infusion event, default is 0.
 * @return The [InfusionEventJson] representation of the infusion event.
 */
fun InfusionEvent.toInfusionEventJson(id: Int = 0): InfusionEventJson =
    InfusionEventJson(id, reason, drugName, dose, batchNumber, note, timestamp, date)