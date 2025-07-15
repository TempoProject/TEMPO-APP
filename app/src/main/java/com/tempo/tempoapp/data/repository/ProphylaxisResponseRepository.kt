package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.ProphylaxisResponseDao
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import kotlinx.coroutines.flow.Flow

class ProphylaxisResponseRepository(private val prophylaxisResponseDao: ProphylaxisResponseDao) :
    LogbookRepository<ProphylaxisResponse> {

    /**
     * Get all prophylaxis responses for a specific reminder date.
     *
     * @param reminderDateTime The date and time of the reminder.
     * @return Flow of list of prophylaxis responses.
     */
    fun getAllDayProphylaxis(reminderDateTime: Long) =
        prophylaxisResponseDao.getAllDayProphylaxis(reminderDateTime)


    override suspend fun insertItem(item: ProphylaxisResponse): Long {
        return prophylaxisResponseDao.insert(item)
    }

    override suspend fun deleteItem(item: ProphylaxisResponse) {
        prophylaxisResponseDao.delete(item)
    }

    override suspend fun updateItem(item: ProphylaxisResponse) {
        prophylaxisResponseDao.update(item)
    }

    override fun getAll(): Flow<List<ProphylaxisResponse>> =
        prophylaxisResponseDao.getAll()


    override fun getItemFromId(id: Int): Flow<ProphylaxisResponse> {
        return prophylaxisResponseDao.getItemFromId(id)
            ?: throw IllegalArgumentException("ProphylaxisResponse with id $id not found")
    }
}