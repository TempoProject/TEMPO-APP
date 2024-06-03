package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.InfusionEvent
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) interface for performing CRUD operations on the `infusion_event` table.
 * This interface extends the [LogbookDao] interface to inherit basic CRUD operations.
 */
@Dao
interface InfusionEventDao : LogbookDao<InfusionEvent> {

    /**
     * Retrieves all infusion events from the database, ordered by timestamp in ascending order.
     *
     * @return A [Flow] emitting a list of [InfusionEvent] records.
     */
    @Query(
        """
        SELECT * 
        FROM infusion_event
        ORDER BY timestamp ASC
    """
    )
    fun getAllInfusion(): Flow<List<InfusionEvent>>

    /**
     * Retrieves a specific infusion event from the database by its unique identifier.
     *
     * @param itemId The unique identifier of the infusion event.
     * @return A [Flow] emitting the [InfusionEvent] record with the specified ID.
     */
    @Query(
        """
            SELECT *
            FROM infusion_event
            WHERE id = :itemId
        """
    )
    fun getEventFromId(itemId: Int): Flow<InfusionEvent>

    /**
     * Retrieves all infusion events from the database that occurred on a specific date.
     *
     * @param date The date of the infusion events in milliseconds.
     * @return A [Flow] emitting a list of [InfusionEvent] records for the specified date.
     */
    @Query(
        """
            SELECT *
            FROM infusion_event WHERE date = :date 
        """
    )
    fun getAllDayInfusion(date: Long): Flow<List<InfusionEvent>>

    /**
     * Retrieves all infusion events from the database with the specified `isSent` status.
     *
     * @param isSent The status of the `isSent` flag to filter records by.
     * @return A list of [InfusionEvent] records matching the specified `isSent` status.
     */
    @Query(
        """
            SELECT * 
            FROM infusion_event WHERE is_sent = :isSent
        """
    )
    suspend fun getAll(isSent: Boolean): List<InfusionEvent>
}