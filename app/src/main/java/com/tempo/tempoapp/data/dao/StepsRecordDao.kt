package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.StepsRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface StepsRecordDao : LogbookDao<StepsRecord> {
    @Query(
        """
        SELECT * 
        FROM steps
    """
    )
    fun getAllSteps(): Flow<List<StepsRecord>>

    @Query(
        """
            SELECT SUM(steps)
            FROM steps WHERE date = :date
        """
    )
    fun getAllDayStepsCount(date: Long): Flow<Int>

    @Query(
        """
            SELECT *
            FROM steps WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDaySteps(isSent: Boolean): List<StepsRecord>

}