package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.Accelerometer

/**
 * DAO (Data Access Object) interface for performing CRUD operations on the `accelerometer` table.
 * This interface extends the [LogbookDao] interface to inherit basic CRUD operations.
 */
@Dao
interface AccelerometerDao : LogbookDao<Accelerometer> {

    /**
     * Retrieves all accelerometer records with the specified `isSent` status from the database.
     *
     * @param isSent The status of the `isSent` flag to filter records by, default is false.
     * @return A list of [Accelerometer] records matching the specified `isSent` status.
     */
    @Query(
        """
            SELECT *
            FROM accelerometer WHERE is_sent = :isSent
        """
    )
    suspend fun getAllData(isSent: Boolean = false): List<Accelerometer>
}