package com.prohub.assistant.ui.screens.todos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prohub.assistant.data.db.TodoEntity
import com.prohub.assistant.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    val pendingTodos: Flow<List<TodoEntity>> = repository.getPendingTodos()
    val completedTodos: Flow<List<TodoEntity>> = repository.getCompletedTodos()

    init {
        viewModelScope.launch {
            combine(pendingTodos, completedTodos) { pending, completed ->
                TodoUiState(
                    pendingTodos = pending,
                    completedTodos = completed,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun addTodo(todo: TodoEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.insert(todo)
                _uiState.update { it.copy(isLoading = false, successMessage = "Task added") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun toggleComplete(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                val updated = todo.copy(
                    completed = !todo.completed,
                    status = if (!todo.completed) "completed" else "pending",
                    completionTime = if (!todo.completed) System.currentTimeMillis() else null
                )
                repository.update(updated)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun delete(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                repository.delete(todo)
                _uiState.update { it.copy(successMessage = "Task deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

data class TodoUiState(
    val pendingTodos: List<TodoEntity> = emptyList(),
    val completedTodos: List<TodoEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
