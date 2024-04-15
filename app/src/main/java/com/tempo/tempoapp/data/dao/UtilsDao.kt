package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.Utils

@Dao
interface UtilsDao : LogbookDao<Utils> {

    @Query(
        """
        SELECT latest_update
        FROM utils
    """
    )
    suspend fun getLatestUpdate(): Long?
}