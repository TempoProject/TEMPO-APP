package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.UtilsDao
import com.tempo.tempoapp.data.model.Utils
import kotlinx.coroutines.flow.Flow

class UtilsRepository(private val utilsDao: UtilsDao) : LogbookRepository<Utils> {
    override suspend fun insertItem(item: Utils) = utilsDao.insert(item)

    override suspend fun deleteItem(item: Utils) {
        TODO("Not yet implemented")
    }

    override suspend fun updateItem(item: Utils) = utilsDao.update(item)

    suspend fun getLatestUpdate() = utilsDao.getLatestUpdate()

    override fun getAll(): Flow<List<Utils>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<Utils> {
        TODO("Not yet implemented")
    }
}