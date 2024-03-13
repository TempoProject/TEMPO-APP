package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.InfusionEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface InfusionEventDao : LogbookDao<InfusionEvent> {
    @Query(
        """
        SELECT * 
        FROM infusion_event
        ORDER BY timestamp ASC
    """
    )
    fun getAllInfusion(): Flow<List<InfusionEvent>>

    @Query(
        """
            SELECT *
            FROM infusion_event
            WHERE id = :itemId
        """
    )
    fun getEventFromId(itemId: Int): Flow<InfusionEvent>
}