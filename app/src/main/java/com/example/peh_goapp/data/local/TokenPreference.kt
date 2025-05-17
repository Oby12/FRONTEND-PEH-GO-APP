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
        private const val KEY_USERNAME = "user_username" // Kunci untuk username
        private const val KEY_NAME = "user_name" // Kunci untuk nama lengkap
        private const val KEY_EMAIL = "user_email" // Kunci untuk email
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

    // Fungsi untuk menyimpan username
    fun saveUsername(username: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    // Fungsi untuk mendapatkan username
    fun getUsername(): String {
        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
    }

    // Fungsi untuk menyimpan nama
    fun saveName(name: String) {
        sharedPreferences.edit().apply {
            putString(KEY_NAME, name)
            apply()
        }
    }

    // Fungsi untuk mendapatkan nama
    fun getName(): String {
        return sharedPreferences.getString(KEY_NAME, "") ?: ""
    }

    // Fungsi untuk menyimpan email
    fun saveEmail(email: String) {
        sharedPreferences.edit().apply {
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    // Fungsi untuk mendapatkan email
    fun getEmail(): String {
        return sharedPreferences.getString(KEY_EMAIL, "") ?: ""
    }

    fun isAdmin(): Boolean {
        return getRole() == "ADMIN"
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