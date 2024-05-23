package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.ReminderEvent
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * DAO (Data Access Object) interface for performing CRUD operations on the `reminder` table.
 * This interface extends the [LogbookDao] interface to inherit basic CRUD operations.
 */
@Dao
interface ReminderDao : LogbookDao<ReminderEvent> {

    /**
     * Retrieves all reminder events from the database.
     * If [isPeriodic] is true, retrieves all periodic reminders.
     * If [isPeriodic] is false, retrieves all non-periodic reminders that have not occurred yet or are ongoing.
     * Reminders are ordered by timestamp in ascending order.
     *
     * @param isPeriodic Boolean indicating whether to retrieve periodic reminders.
     * @param now The current timestamp, defaults to the current time.
     * @return A [Flow] emitting a list of [ReminderEvent] records.
     */
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