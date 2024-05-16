package com.tempo.tempoapp.data.repository

import kotlinx.coroutines.flow.Flow

interface LogbookRepository<T> {

    suspend fun insertItem(item: T):Long

    suspend fun deleteItem(item: T)

    suspend fun updateItem(item: T)

    fun getAll(): Flow<List<T>>

    fun getItemFromId(id: Int): Flow<T>

}