package com.prohub.assistant

import androidx.lifecycle.ViewModel
import com.prohub.assistant.data.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val authManager: AuthManager
) : ViewModel()
