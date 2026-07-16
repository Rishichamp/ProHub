package com.prohub.assistant.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prohub.assistant.data.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePrefs: SecurePrefs
) : ViewModel() {

    private val _geminiKey = MutableStateFlow(securePrefs.getGeminiKey())
    val geminiKey: StateFlow<String> = _geminiKey

    fun saveGeminiKey(key: String) {
        viewModelScope.launch {
            securePrefs.setGeminiKey(key)
            _geminiKey.value = key
        }
    }
}
