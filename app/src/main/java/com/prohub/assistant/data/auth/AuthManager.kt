package com.prohub.assistant.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            AUTH_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val hasPin = prefs.contains(KEY_PIN_HASH)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        _authState.value = AuthState(
            isAuthenticated = isLoggedIn,
            hasPin = hasPin,
            requiresAuth = false
        )
    }

    fun setPin(pin: String): Boolean {
        if (pin.length < 4) return false
        prefs.edit()
            .putString(KEY_PIN_HASH, hashPin(pin))
            .putBoolean(KEY_HAS_PIN, true)
            .apply()
        _authState.value = _authState.value.copy(hasPin = true)
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val isValid = storedHash == hashPin(pin)
        if (isValid) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
            _authState.value = _authState.value.copy(
                isAuthenticated = true,
                requiresAuth = false
            )
        }
        return isValid
    }

    fun requestAuthForSettings() {
        if (_authState.value.hasPin && !_authState.value.isAuthenticated) {
            _authState.value = _authState.value.copy(requiresAuth = true)
        }
    }

    fun dismissAuth() {
        _authState.value = _authState.value.copy(requiresAuth = false)
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        _authState.value = AuthState(
            isAuthenticated = false,
            hasPin = prefs.contains(KEY_PIN_HASH)
        )
    }

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_HAS_PIN)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        _authState.value = AuthState()
    }

    private fun hashPin(pin: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .run {
                update(pin.toByteArray())
                update("prohub_salt_2026".toByteArray())
                digest().joinToString("") { "%02x".format(it) }
            }
    }

    companion object {
        private const val AUTH_PREFS = "auth_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_HAS_PIN = "has_pin"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}

data class AuthState(
    val isAuthenticated: Boolean = false,
    val hasPin: Boolean = false,
    val requiresAuth: Boolean = false
)
