package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.BleedingEventDao
import com.tempo.tempoapp.data.model.BleedingEvent
import kotlinx.coroutines.flow.Flow

class BleedingRepository(private val bleedingEventDao: BleedingEventDao) :
    LogbookRepository<BleedingEvent> {

    override suspend fun insertItem(item: BleedingEvent) = bleedingEventDao.insert(item)

    override suspend fun deleteItem(item: BleedingEvent) = bleedingEventDao.delete(item)

    override suspend fun updateItem(item: BleedingEvent) = bleedingEventDao.update(item)

    override fun getAll(): Flow<List<BleedingEvent>> = bleedingEventDao.getAllBleeding()

    override fun getItemFromId(id: Int): Flow<BleedingEvent> = bleedingEventDao.getEventFromId(id)

    fun getAllDayBleeding(date: Long): Flow<List<BleedingEvent>> {
        return bleedingEventDao.getAllDayBleeding(date)
    }

}