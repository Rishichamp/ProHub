package com.prohub.assistant.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePrefs @Inject constructor(
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
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getWhisperKey(): String = prefs.getString(KEY_WHISPER, "") ?: ""
    fun setWhisperKey(key: String) = prefs.edit().putString(KEY_WHISPER, key).apply()

    fun getGeminiKey(): String = prefs.getString(KEY_GEMINI, "") ?: ""
    fun setGeminiKey(key: String) = prefs.edit().putString(KEY_GEMINI, key).apply()

    fun getDbPassphrase(): ByteArray {
        val stored = prefs.getString(KEY_DB_PASSPHRASE, null)
        return if (stored != null) {
            android.util.Base64.decode(stored, android.util.Base64.DEFAULT)
        } else {
            val newPass = ByteArray(32).apply {
                java.security.SecureRandom().nextBytes(this)
            }
            prefs.edit().putString(KEY_DB_PASSPHRASE, android.util.Base64.encodeToString(newPass, android.util.Base64.DEFAULT)).apply()
            newPass
        }
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun setUserName(name: String) = prefs.edit().putString(KEY_USER_NAME, name).apply()

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_WHISPER = "whisper_api_key"
        private const val KEY_GEMINI = "gemini_api_key"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
    }
}
