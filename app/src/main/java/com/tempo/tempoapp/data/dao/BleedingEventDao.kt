package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.BleedingEvent
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) interface for performing CRUD operations on the `bleeding_event` table.
 * This interface extends the [LogbookDao] interface to inherit basic CRUD operations.
 */
@Dao
interface BleedingEventDao : LogbookDao<BleedingEvent> {

    /**
     * Retrieves all bleeding events from the database, ordered by timestamp in ascending order.
     *
     * @return A [Flow] emitting a list of [BleedingEvent] records.
     */
    @Query(
        """
        SELECT * 
        FROM bleeding_event
        ORDER BY timestamp ASC
    """
    )
    fun getAllBleeding(): Flow<List<BleedingEvent>>

    /**
     * Retrieves a specific bleeding event from the database by its unique identifier.
     *
     * @param itemId The unique identifier of the bleeding event.
     * @return A [Flow] emitting the [BleedingEvent] record with the specified ID.
     */
    @Query(
        """
            SELECT *
            FROM bleeding_event
            WHERE id = :itemId
        """
    )
    fun getEventFromId(itemId: Int): Flow<BleedingEvent>

    /**
     * Retrieves all bleeding events from the database that occurred on a specific date.
     *
     * @param date The date of the bleeding events in milliseconds.
     * @return A [Flow] emitting a list of [BleedingEvent] records for the specified date.
     */
    @Query(
        """
            SELECT *
            FROM bleeding_event WHERE date = :date 
        """
    )
    fun getAllDayBleeding(date: Long): Flow<List<BleedingEvent>>

    /**
     * Retrieves all bleeding events from the database with the specified `isSent` status.
     *
     * @param isSent The status of the `isSent` flag to filter records by.
     * @return A list of [BleedingEvent] records matching the specified `isSent` status.
     */
    @Query(
        """
            SELECT * 
            FROM bleeding_event WHERE is_sent = :isSent
        """
    )
    suspend fun getAll(isSent: Boolean): List<BleedingEvent>


    @Query("SELECT * FROM bleeding_event ORDER BY date DESC")
    suspend fun getAllBleedingEvents(): List<BleedingEvent>
}