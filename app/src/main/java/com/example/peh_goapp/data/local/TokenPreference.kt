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

    fun clearToken() {
        sharedPreferences.edit().apply {
            remove(KEY_TOKEN)
            apply()
        }
    }
}