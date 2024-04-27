package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.InfusionEventDao
import com.tempo.tempoapp.data.model.InfusionEvent
import kotlinx.coroutines.flow.Flow

class InfusionRepository(private val infusionEventDao: InfusionEventDao) :
    LogbookRepository<InfusionEvent> {
    override suspend fun insertItem(item: InfusionEvent) =
        infusionEventDao.insert(item)


    override suspend fun deleteItem(item: InfusionEvent) {
        infusionEventDao.delete(item)
    }

    override suspend fun updateItem(item: InfusionEvent) {
        infusionEventDao.update(item)
    }

    override fun getAll(): Flow<List<InfusionEvent>> {
        return infusionEventDao.getAllInfusion()
    }

    override fun getItemFromId(id: Int): Flow<InfusionEvent> {
        return infusionEventDao.getEventFromId(id)
    }

    fun getAllDayInfusion(date: Long): Flow<List<InfusionEvent>> {
        return infusionEventDao.getAllDayInfusion(date)
    }

    suspend fun getAllInfusionToSent(isSent: Boolean) = infusionEventDao.getAll(isSent)

}