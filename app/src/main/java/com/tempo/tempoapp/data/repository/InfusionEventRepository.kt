package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.InfusionEventDao
import com.tempo.tempoapp.data.model.InfusionEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for performing CRUD operations on infusion event data.
 *
 * @param infusionEventDao The Data Access Object (DAO) for infusion event data.
 */
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

    /**
     * Retrieves all infusion events from the repository.
     *
     * @return A [Flow] emitting a list of infusion events.
     */
    override fun getAll(): Flow<List<InfusionEvent>> {
        return infusionEventDao.getAllInfusion()
    }

    /**
     * Retrieves an infusion event from the repository by its ID.
     *
     * @param id The ID of the infusion event to retrieve.
     * @return A [Flow] emitting the infusion event.
     */
    override fun getItemFromId(id: Int): Flow<InfusionEvent> {
        return infusionEventDao.getEventFromId(id)
    }

    /**
     * Retrieves all infusion events for a specific date.
     *
     * @param date The date for which to retrieve infusion events in milliseconds.
     * @return A [Flow] emitting a list of infusion events for the specified date.
     */
    fun getAllDayInfusion(date: Long): Flow<List<InfusionEvent>> {
        return infusionEventDao.getAllDayInfusion(date)
    }

    /**
     * Retrieves all infusion events with the specified `isSent` status.
     *
     * @param isSent The status of the `isSent` flag to filter events by.
     * @return A list of infusion events matching the specified `isSent` status.
     */
    suspend fun getAllInfusionToSent(isSent: Boolean) = infusionEventDao.getAll(isSent)

}