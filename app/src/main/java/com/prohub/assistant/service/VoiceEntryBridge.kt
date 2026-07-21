package com.prohub.assistant.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lets Home's "Voice" quick action tell the AI Chat screen to start live
 * dictation immediately upon arrival, instead of just opening a blank
 * text chat that still requires tapping the mic manually.
 */
object VoiceEntryBridge {
    private val _shouldAutoStart = MutableStateFlow(false)
    val shouldAutoStart = _shouldAutoStart.asStateFlow()

    fun requestAutoStart() {
        _shouldAutoStart.value = true
    }

    fun consume() {
        _shouldAutoStart.value = false
    }
}