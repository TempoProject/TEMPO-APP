package com.tempo.tempoapp.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Generic repository interface for performing CRUD operations on logbook items.
 *
 * @param T The type of the logbook item.
 */
interface LogbookRepository<T> {

    /**
     * Inserts a new item into the repository.
     *
     * @param item The item to be inserted.
     * @return The row ID of the newly inserted item.
     */
    suspend fun insertItem(item: T):Long

    /**
     * Deletes an item from the repository.
     *
     * @param item The item to be deleted.
     */
    suspend fun deleteItem(item: T)

    /**
     * Updates an existing item in the repository.
     *
     * @param item The item to be updated.
     */
    suspend fun updateItem(item: T)

    /**
     * Retrieves all items from the repository.
     *
     * @return A [Flow] emitting a list of items.
     */
    fun getAll(): Flow<List<T>>

    /**
     * Retrieves an item from the repository by its ID.
     *
     * @param id The ID of the item to retrieve.
     * @return A [Flow] emitting the item.
     */
    fun getItemFromId(id: Int): Flow<T>

}