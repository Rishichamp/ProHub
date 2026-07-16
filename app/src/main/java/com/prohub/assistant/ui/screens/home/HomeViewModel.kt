package com.prohub.assistant.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prohub.assistant.data.db.FitnessDao
import com.prohub.assistant.data.prefs.SecurePrefs
import com.prohub.assistant.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val fitnessDao: FitnessDao,
    private val securePrefs: SecurePrefs
) : ViewModel() {

    private val _pendingTaskCount = MutableStateFlow(0)
    val pendingTaskCount: StateFlow<Int> = _pendingTaskCount.asStateFlow()

    private val _userName = MutableStateFlow(securePrefs.getUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _activeMinutesToday = MutableStateFlow(0)
    val activeMinutesToday: StateFlow<Int> = _activeMinutesToday.asStateFlow()

    init {
        viewModelScope.launch {
            todoRepository.getPendingTodos().collect { todos ->
                _pendingTaskCount.value = todos.size
            }
        }
        viewModelScope.launch {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
            _activeMinutesToday.value = fitnessDao.getTotalActiveMinutes(startOfDay, endOfDay) ?: 0
        }
    }
}