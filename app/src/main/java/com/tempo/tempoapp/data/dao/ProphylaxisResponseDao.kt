package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface ProphylaxisResponseDao : LogbookDao<ProphylaxisResponse> {

    @Query(
        """
        SELECT *
        FROM prophylaxis_responses
        WHERE date = :date
    """
    )
    fun getAllDayProphylaxis(date: Long): Flow<List<ProphylaxisResponse>>

    @Query(
        """
            SELECT *
            FROM prophylaxis_responses
            WHERE id = :itemId
        """
    )
    fun getItemFromId(itemId: Int): Flow<ProphylaxisResponse>?

    @Query(
        """
            SELECT *
            FROM prophylaxis_responses
            ORDER BY date DESC
        """
    )
    fun getAll(): Flow<List<ProphylaxisResponse>>

    @Query("UPDATE prophylaxis_responses SET postponedAlarmId = :alarmId WHERE id = :responseId")
    suspend fun updatePostponedAlarmId(responseId: Long, alarmId: Int)

}