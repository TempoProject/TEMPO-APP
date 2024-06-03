package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.UtilsDao
import com.tempo.tempoapp.data.model.Utils
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for performing CRUD operations on utility data.
 *
 * @param utilsDao The Data Access Object (DAO) for utility data.
 */
class UtilsRepository(private val utilsDao: UtilsDao) : LogbookRepository<Utils> {
    override suspend fun insertItem(item: Utils) = utilsDao.insert(item)

    override suspend fun deleteItem(item: Utils) {
        TODO("Not yet implemented")
    }

    override suspend fun updateItem(item: Utils) = utilsDao.update(item)

    /**
     * Retrieves the latest update timestamp.
     *
     * @return The latest update timestamp, nullable.
     */
    suspend fun getLatestUpdate() = utilsDao.getLatestUpdate()

    override fun getAll(): Flow<List<Utils>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<Utils> {
        TODO("Not yet implemented")
    }
}