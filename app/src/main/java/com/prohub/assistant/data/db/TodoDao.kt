package com.prohub.assistant.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos WHERE completed = 0 AND (snoozedUntil = 0 OR snoozedUntil < :now) ORDER BY dueDate ASC, dueTime ASC")
    fun getPendingTodos(now: Long = System.currentTimeMillis()): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE completed = 1 ORDER BY createdAt DESC")
    fun getCompletedTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TodoEntity?

    @Query("SELECT * FROM todos WHERE completed = 0 AND dueDate IS NOT NULL")
    suspend fun getAllPending(): List<TodoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity)

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)
}
