package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.Movesense
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) interface for performing CRUD operations on the `movesense` table.
 * This interface extends the [LogbookDao] interface to inherit basic CRUD operations.
 */
@Dao
interface MovesenseDao : LogbookDao<Movesense> {

    /**
     * Retrieves the Movesense device information from the database.
     *
     * @return A [Flow] emitting the Movesense device information.
     */
    @Query(
        """
            SELECT * FROM movesense
        """
    )
    fun getDevice(): Flow<Movesense>

    /**
     * Retrieves the Movesense device information synchronously.
     *
     * @return The Movesense device information.
     */
    @Query(
        """
            SELECT * FROM movesense
        """
    )
    suspend fun getDeviceInfo(): Movesense

}