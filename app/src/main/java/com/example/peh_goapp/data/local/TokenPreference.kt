package com.example.peh_goapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenPreference @Inject constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_ROLE = "user_role"
        private const val KEY_USERNAME = "user_username"
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_FIRST_TIME = "is_first_time" // Key baru untuk status introduction
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().apply {
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    fun getToken(): String {
        return sharedPreferences.getString(KEY_TOKEN, "") ?: ""
    }

    fun saveRole(role: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ROLE, role)
            apply()
        }
    }

    fun getRole(): String {
        return sharedPreferences.getString(KEY_ROLE, "") ?: ""
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    fun getUsername(): String {
        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
    }

    fun saveName(name: String) {
        sharedPreferences.edit().apply {
            putString(KEY_NAME, name)
            apply()
        }
    }

    fun getName(): String {
        return sharedPreferences.getString(KEY_NAME, "") ?: ""
    }

    fun saveEmail(email: String) {
        sharedPreferences.edit().apply {
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun getEmail(): String {
        return sharedPreferences.getString(KEY_EMAIL, "") ?: ""
    }

    fun isAdmin(): Boolean {
        return getRole() == "ADMIN"
    }

    // Fungsi baru untuk mengelola status introduction
    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true)
    }

    fun setNotFirstTime() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_FIRST_TIME, false)
            apply()
        }
    }

    fun clearToken() {
        sharedPreferences.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_ROLE)
            remove(KEY_USERNAME)
            remove(KEY_NAME)
            remove(KEY_EMAIL)
            apply()
        }
    }
}