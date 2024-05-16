package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.Accelerometer

@Dao
interface AccelerometerDao : LogbookDao<Accelerometer> {
    @Query(
        """
            SELECT *
            FROM accelerometer WHERE is_sent = :isSent
        """
    )
    suspend fun getAllData(isSent: Boolean = false): List<Accelerometer>
}