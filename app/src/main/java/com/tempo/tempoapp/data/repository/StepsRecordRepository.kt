package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.StepsRecordDao
import com.tempo.tempoapp.data.model.StepsRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for performing CRUD operations on steps record data.
 *
 * @param stepsRecordDao The Data Access Object (DAO) for steps record data.
 */

class StepsRecordRepository(private val stepsRecordDao: StepsRecordDao) :
    LogbookRepository<StepsRecord> {
    override suspend fun insertItem(item: StepsRecord) =
        stepsRecordDao.insert(item)


    override suspend fun deleteItem(item: StepsRecord) {
        stepsRecordDao.delete(item)
    }

    override suspend fun updateItem(item: StepsRecord) {
        stepsRecordDao.update(item)
    }

    /**
     * Retrieves all steps records from the repository.
     *
     * @return A [Flow] emitting a list of steps records.
     */
    override fun getAll(): Flow<List<StepsRecord>> {
        return stepsRecordDao.getAllSteps()
    }

    /**
     * Retrieves the total steps count for a specific date from the repository.
     *
     * @param date The date for which to retrieve the steps count in milliseconds.
     * @return A [Flow] emitting the total steps count for the specified date.
     */
    fun getAllDayStepsCount(date: Long): Flow<Int> {
        return stepsRecordDao.getAllDayStepsCount(date)
    }

    /**
     * Retrieves all steps records for a specific date with the specified `isSent` status.
     *
     * @param isSent The status of the `isSent` flag to filter records by.
     * @return A list of steps records matching the specified `isSent` status.
     */
    suspend fun getAllDaySteps(isSent: Boolean): List<StepsRecord> {
        return stepsRecordDao.getAllDaySteps(isSent)
    }

    override fun getItemFromId(id: Int): Flow<StepsRecord> {
        TODO("Not implemented")
    }
}