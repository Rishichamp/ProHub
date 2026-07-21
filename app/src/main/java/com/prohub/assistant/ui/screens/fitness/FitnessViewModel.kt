package com.prohub.assistant.ui.screens.fitness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prohub.assistant.data.db.ExerciseLogEntity
import com.prohub.assistant.data.db.FitnessEntry
import com.prohub.assistant.data.db.FitnessGoal
import com.prohub.assistant.data.repository.FitnessRepository
import com.prohub.assistant.data.repository.FitnessStats
import com.prohub.assistant.service.ActiveTimerBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FitnessViewModel @Inject constructor(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FitnessUiState())
    val uiState: StateFlow<FitnessUiState> = _uiState.asStateFlow()

    val todayEntries = repository.getTodayEntries()
    val weekEntries = repository.getWeekEntries()
    val activeGoals = repository.getActiveGoals()
    val todaySessionExercises = repository.getTodaySessionExercises()

    // The exercise currently being timed, and when it started (null = no active timer)
    private val _activeExerciseName = MutableStateFlow<String?>(null)
    val activeExerciseName: StateFlow<String?> = _activeExerciseName.asStateFlow()
    private val _activeStartTime = MutableStateFlow<Long?>(null)
    val activeStartTime: StateFlow<Long?> = _activeStartTime.asStateFlow()

    init {
        loadTodayStats()
        loadWeekStats()

        // Listen for Sage voice commands like "start timing for running"
        viewModelScope.launch {
            ActiveTimerBridge.requestedExercise.collect { name ->
                if (name != null) {
                    startTimer(name)
                    ActiveTimerBridge.consume()
                }
            }
        }
    }

    fun startTimer(exerciseName: String) {
        if (exerciseName.isBlank()) return
        // If something else is already running, stop/log it first
        if (_activeExerciseName.value != null) {
            stopTimer()
        }
        _activeExerciseName.value = exerciseName
        _activeStartTime.value = System.currentTimeMillis()
    }

    fun stopTimer() {
        val name = _activeExerciseName.value ?: return
        val start = _activeStartTime.value ?: return
        val durationSeconds = ((System.currentTimeMillis() - start) / 1000).toInt().coerceAtLeast(1)

        viewModelScope.launch {
            repository.logExercise(name, durationSeconds)
            loadTodayStats()
        }

        _activeExerciseName.value = null
        _activeStartTime.value = null
    }

    fun deleteSessionExercise(entry: ExerciseLogEntity) {
        viewModelScope.launch {
            repository.deleteExerciseLog(entry)
            loadTodayStats()
        }
    }

    fun loadTodayStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val stats = repository.getTodayStats()
                _uiState.update { it.copy(todayStats = stats, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun loadWeekStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getWeekStats()
                _uiState.update { it.copy(weekStats = stats) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addEntry(entry: FitnessEntry) {
        viewModelScope.launch {
            try {
                repository.insertEntry(entry)
                _uiState.update { it.copy(successMessage = "Workout logged!") }
                loadTodayStats()
                loadWeekStats()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteEntry(entry: FitnessEntry) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(entry)
                _uiState.update { it.copy(successMessage = "Entry deleted") }
                loadTodayStats()
                loadWeekStats()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addGoal(goal: FitnessGoal) {
        viewModelScope.launch {
            try {
                repository.insertGoal(goal)
                _uiState.update { it.copy(successMessage = "Goal set!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteGoal(goal: FitnessGoal) {
        viewModelScope.launch {
            try {
                repository.deleteGoal(goal)
                _uiState.update { it.copy(successMessage = "Goal removed") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun getGoalProgress(goal: FitnessGoal, stats: FitnessStats): Float {
        return when (goal.goalType) {
            "steps" -> (stats.steps.toFloat() / goal.targetValue).coerceIn(0f, 1f)
            "calories" -> (stats.calories.toFloat() / goal.targetValue).coerceIn(0f, 1f)
            "distance" -> (stats.distanceKm / goal.targetValue).coerceIn(0f, 1f)
            "activeMinutes" -> (stats.activeMinutes.toFloat() / goal.targetValue).coerceIn(0f, 1f)
            "workouts" -> (stats.workoutCount.toFloat() / goal.targetValue).coerceIn(0f, 1f)
            else -> 0f
        }
    }
}

data class FitnessUiState(
    val todayStats: FitnessStats = FitnessStats(),
    val weekStats: FitnessStats = FitnessStats(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)