package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.Utils

/**
 * DAO (Data Access Object) interface for performing CRUD operations on the `utils` table.
 * This interface extends the [LogbookDao] interface to inherit basic CRUD operations.
 */
@Dao
interface UtilsDao : LogbookDao<Utils> {

    /**
     * Retrieves the latest update timestamp from the `utils` table.
     *
     * @return The latest update timestamp, nullable.
     */
    @Query(
        """
        SELECT latest_update
        FROM utils
    """
    )
    suspend fun getLatestUpdate(): Long?
}