package com.prohub.assistant.data.repository

import com.prohub.assistant.data.db.TodoDao
import com.prohub.assistant.data.db.TodoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
) {
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()
    fun getPendingTodos(): Flow<List<TodoEntity>> = todoDao.getPendingTodos()
    fun getCompletedTodos(): Flow<List<TodoEntity>> = todoDao.getCompletedTodos()
    suspend fun getById(id: String): TodoEntity? = todoDao.getById(id)
    suspend fun getAllPending(): List<TodoEntity> = todoDao.getAllPending()
    suspend fun insert(todo: TodoEntity) = todoDao.insert(todo)
    suspend fun update(todo: TodoEntity) = todoDao.update(todo)
    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)
}
