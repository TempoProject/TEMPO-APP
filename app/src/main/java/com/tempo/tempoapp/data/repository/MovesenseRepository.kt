package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.MovesenseDao
import com.tempo.tempoapp.data.model.Movesense
import kotlinx.coroutines.flow.Flow

class MovesenseRepository(private val movesenseDao: MovesenseDao) : LogbookRepository<Movesense> {
    override suspend fun insertItem(item: Movesense): Long = movesenseDao.insert(item)

    override suspend fun deleteItem(item: Movesense) = movesenseDao.delete(item)

    override suspend fun updateItem(item: Movesense) = movesenseDao.update(item)

    fun getDevice() = movesenseDao.getDevice()

    suspend fun getDeviceInfo() = movesenseDao.getDeviceInfo()

    override fun getAll(): Flow<List<Movesense>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<Movesense> {
        TODO("Not yet implemented")
    }
}