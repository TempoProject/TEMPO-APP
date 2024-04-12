package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.ReminderDao
import com.tempo.tempoapp.data.model.ReminderEvent
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) : LogbookRepository<ReminderEvent> {
    override suspend fun insertItem(item: ReminderEvent) = reminderDao.insert(item)

    override suspend fun deleteItem(item: ReminderEvent) = reminderDao.delete(item)

    override suspend fun updateItem(item: ReminderEvent) = reminderDao.update(item)

    override fun getAll(): Flow<List<ReminderEvent>> = reminderDao.getAllReminder()

    override fun getItemFromId(id: Int): Flow<ReminderEvent> {
        TODO("Not yet implemented")
    }
}