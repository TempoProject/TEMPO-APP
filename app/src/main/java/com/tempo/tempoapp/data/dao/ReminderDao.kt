package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.ReminderEvent
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ReminderDao : LogbookDao<ReminderEvent> {
    @Query(
        """
        SELECT * 
        FROM reminder
        WHERE is_periodic = :isPeriodic OR timestamp >= :now
        ORDER BY timestamp ASC
    """
    )
    fun getAllReminder(
        isPeriodic: Boolean = true,
        now: Long = Instant.now().toEpochMilli()
    ): Flow<List<ReminderEvent>>
}