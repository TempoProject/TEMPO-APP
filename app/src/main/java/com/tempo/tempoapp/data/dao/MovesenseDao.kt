package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.Movesense
import kotlinx.coroutines.flow.Flow

@Dao
interface MovesenseDao : LogbookDao<Movesense> {
    @Query(
        """
            SELECT * FROM movesense
        """
    )
    fun getDevice(): Flow<Movesense>

    @Query(
        """
            SELECT * FROM movesense
        """
    )
    suspend fun getDeviceInfo(): Movesense

}