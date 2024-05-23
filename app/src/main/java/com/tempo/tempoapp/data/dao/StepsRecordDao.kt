package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.StepsRecord
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) interface for performing CRUD operations on the `steps` table.
 * This interface extends the [LogbookDao] interface to inherit basic CRUD operations.
 */
@Dao
interface StepsRecordDao : LogbookDao<StepsRecord> {

    /**
     * Retrieves all steps records from the database.
     *
     * @return A [Flow] emitting a list of [StepsRecord] objects.
     */
    @Query(
        """
        SELECT * 
        FROM steps
    """
    )
    fun getAllSteps(): Flow<List<StepsRecord>>

    /**
     * Retrieves the total steps count for a specific date.
     *
     * @param date The date for which to retrieve the steps count in milliseconds.
     * @return A [Flow] emitting the total steps count for the specified date.
     */
    @Query(
        """
            SELECT SUM(steps)
            FROM steps WHERE date = :date
        """
    )
    fun getAllDayStepsCount(date: Long): Flow<Int>

    /**
     * Retrieves all steps records for a specific date with the specified `isSent` status.
     *
     * @param isSent The status of the `isSent` flag to filter records by.
     * @return A list of [StepsRecord] objects matching the specified `isSent` status.
     */
    @Query(
        """
            SELECT *
            FROM steps WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDaySteps(isSent: Boolean): List<StepsRecord>

}