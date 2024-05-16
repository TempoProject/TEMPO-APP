package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.AccelerometerDao
import com.tempo.tempoapp.data.model.Accelerometer
import kotlinx.coroutines.flow.Flow

class AccelerometerRepository(private val accelerometerDao: AccelerometerDao) :
    LogbookRepository<Accelerometer> {
    override suspend fun insertItem(item: Accelerometer): Long = accelerometerDao.insert(item)

    override suspend fun deleteItem(item: Accelerometer) = accelerometerDao.delete(item)

    override suspend fun updateItem(item: Accelerometer) = accelerometerDao.update(item)

    suspend fun getAllData(isSent:Boolean) = accelerometerDao.getAllData(isSent)

    override fun getAll(): Flow<List<Accelerometer>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<Accelerometer> {
        TODO("Not yet implemented")
    }

}