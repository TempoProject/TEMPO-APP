package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.BleedingEventDao
import com.tempo.tempoapp.data.model.BleedingEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for performing CRUD operations on bleeding event data.
 *
 * @param bleedingEventDao The Data Access Object (DAO) for bleeding event data.
 */
class BleedingRepository(private val bleedingEventDao: BleedingEventDao) :
    LogbookRepository<BleedingEvent> {

    override suspend fun insertItem(item: BleedingEvent) = bleedingEventDao.insert(item)

    override suspend fun deleteItem(item: BleedingEvent) = bleedingEventDao.delete(item)

    override suspend fun updateItem(item: BleedingEvent) = bleedingEventDao.update(item)

    /**
     * Retrieves all bleeding events from the repository.
     *
     * @return A [Flow] emitting a list of bleeding events.
     */
    override fun getAll(): Flow<List<BleedingEvent>> = bleedingEventDao.getAllBleeding()

    /**
     * Retrieves a bleeding event from the repository by its ID.
     *
     * @param id The ID of the bleeding event to retrieve.
     * @return A [Flow] emitting the bleeding event.
     */
    override fun getItemFromId(id: Int): Flow<BleedingEvent> = bleedingEventDao.getEventFromId(id)

    /**
     * Retrieves all bleeding events for a specific date.
     *
     * @param date The date for which to retrieve bleeding events in milliseconds.
     * @return A [Flow] emitting a list of bleeding events for the specified date.
     */
    fun getAllDayBleeding(date: Long): Flow<List<BleedingEvent>> {
        return bleedingEventDao.getAllDayBleeding(date)
    }

    /**
     * Retrieves all bleeding events with the specified `isSent` status.
     *
     * @param isSent The status of the `isSent` flag to filter events by.
     * @return A list of bleeding events matching the specified `isSent` status.
     */
    suspend fun getAllBleedingToSent(isSent: Boolean) = bleedingEventDao.getAll(isSent)

}