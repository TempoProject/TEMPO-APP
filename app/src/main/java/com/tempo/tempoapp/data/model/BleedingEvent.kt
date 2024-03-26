package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BleedingCause {
    Spontaneo,
    Lesione,
    Intervento,
    Infortunio,
    Altro

}

enum class Severity {
    Lieve,
    Moderato,
    Grave
}

val bleedingSite: Set<String> = setOf(
    "Braccio destro",
    "Braccio sinistro",
    "Busto",
    "Gamba destra",
    "Gamba sinistra"
)


@Entity(tableName = "bleeding_event")
data class BleedingEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "bleeding_site")
    val bleedingSite: String,
    @ColumnInfo(name = "cause")
    val bleedingCause: String,
    @ColumnInfo(name = "severity")
    val severity: String,
    @ColumnInfo(name = "pain_scale")
    val painScale: String,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "note")
    val note: String?,

    // add photo
)
