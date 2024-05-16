package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.StepsRecordDao
import com.tempo.tempoapp.data.model.StepsRecord
import kotlinx.coroutines.flow.Flow

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

    override fun getAll(): Flow<List<StepsRecord>> {
        return stepsRecordDao.getAllSteps()
    }

    fun getAllDayStepsCount(date: Long): Flow<Int> {
        return stepsRecordDao.getAllDayStepsCount(date)
    }

    suspend fun getAllDaySteps(isSent: Boolean): List<StepsRecord> {
        return stepsRecordDao.getAllDaySteps(isSent)
    }

    override fun getItemFromId(id: Int): Flow<StepsRecord> {
        TODO("Not implemented")
    }
}