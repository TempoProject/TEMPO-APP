package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Generic DAO (Data Access Object) interface for performing basic CRUD operations in the database.
 * This interface is intended to be implemented by specific DAOs for different data models.
 *
 * @param T The type of the data model.
 */
@Dao
interface LogbookDao<T> {

    /**
     * Inserts a new item into the database.
     *
     * @param item The item to be inserted.
     * @return The row ID of the newly inserted item, or -1 if the insertion failed.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: T): Long

    /**
     * Updates an existing item in the database.
     *
     * @param item The item to be updated.
     */
    @Update
    suspend fun update(item: T)

    /**
     * Deletes an item from the database.
     *
     * @param item The item to be deleted.
     */
    @Delete
    suspend fun delete(item: T)

}