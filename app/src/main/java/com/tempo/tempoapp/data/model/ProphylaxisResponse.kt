package com.tempo.tempoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prophylaxis_responses")
data class ProphylaxisResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val reminderDateTime: Long,
    val responded: Int = -1,
    val responseDateTime: Long = -1,
    val date: Long = -1,
    val reminderType: String,
    val drugName: String,
    val dosage: String,
    val dosageUnit: String,
    val postponedAlarmId: Int = -1,
    val isSent: Boolean = false
)