package com.prohub.assistant.service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
/**
 * A lightweight bridge between Sage (running as a background service) and the
 * Fitness screen's UI. Sage can't directly call into a Composable's state, so
 * it writes the requested exercise name here; FitnessViewModel observes it
 * and starts/creates the matching timer whenever the Fitness screen is open.
 *
 * If the Fitness screen isn't open when the command is spoken, the request
 * just waits here until it is (simple, no extra permissions or cross-process
 * plumbing needed).
 */
object ActiveTimerBridge {
    private val _requestedExercise = MutableStateFlow<String?>(null)
    val requestedExercise = _requestedExercise.asStateFlow()

    fun requestStart(exerciseName: String) {
        _requestedExercise.value = exerciseName
    }

    fun consume() {
        _requestedExercise.value = null
    }
}