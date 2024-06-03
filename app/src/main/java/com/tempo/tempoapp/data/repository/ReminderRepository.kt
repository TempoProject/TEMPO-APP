package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.ReminderDao
import com.tempo.tempoapp.data.model.ReminderEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for performing CRUD operations on reminder event data.
 *
 * @param reminderDao The Data Access Object (DAO) for reminder event data.
 */
class ReminderRepository(private val reminderDao: ReminderDao) : LogbookRepository<ReminderEvent> {
    override suspend fun insertItem(item: ReminderEvent) = reminderDao.insert(item)
    
    override suspend fun deleteItem(item: ReminderEvent) = reminderDao.delete(item)

    override suspend fun updateItem(item: ReminderEvent) = reminderDao.update(item)

    /**
     * Retrieves all reminder events from the repository.
     *
     * @return A [Flow] emitting a list of reminder events.
     */
    override fun getAll(): Flow<List<ReminderEvent>> = reminderDao.getAllReminder()

    override fun getItemFromId(id: Int): Flow<ReminderEvent> {
        TODO("Not yet implemented")
    }
}