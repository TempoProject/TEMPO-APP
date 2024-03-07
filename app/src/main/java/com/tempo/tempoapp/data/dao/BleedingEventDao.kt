package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.BleedingEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface BleedingEventDao : LogbookDao<BleedingEvent> {

    @Query(
        """
        SELECT * 
        FROM bleeding_event
        ORDER BY timestamp ASC
    """
    )
    fun getAllBleeding(): Flow<List<BleedingEvent>>
}