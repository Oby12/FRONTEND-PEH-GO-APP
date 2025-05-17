package com.example.peh_goapp.util

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject
import javax.inject.Singleton
import com.example.peh_goapp.R

/**
 * Kelas untuk menyediakan akses ke resource Android
 * Menggunakan abstraksi ini menghindari ketergantungan langsung ViewModel pada Context
 */
@Singleton
class ResourceProvider @Inject constructor(private val context: Context) {

    /**
     * Mendapatkan string dari resource ID
     */
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    /**
     * Mendapatkan string dengan format dari resource ID
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    /**
     * Menerjemahkan pesan error API ke bahasa yang sesuai
     */
    fun getErrorMessage(apiErrorMessage: String): String {
        return when (apiErrorMessage) {
            "Email or password is wrong" -> getString(R.string.error_invalid_credentials)
            "Email already exist" -> getString(R.string.error_email_exists)
            "Username already taken" -> getString(R.string.error_username_exists)
            "Token tidak ditemukan dalam respons" -> getString(R.string.error_token_not_found)
            "Unauthorized" -> getString(R.string.error_unauthorized)
            "Unauthorized: Token is required" -> getString(R.string.error_token_required)
            "Unauthorized: Invalid token" -> getString(R.string.error_invalid_token)
            "Forbidden: You do not have permission" -> getString(R.string.error_forbidden)
            "Destinasi tidak ditemukan" -> getString(R.string.error_destination_not_found)
            "Kategori tidak ditemukan" -> getString(R.string.error_category_not_found)
            else -> apiErrorMessage // Return pesan original jika tidak ada terjemahan
        }
    }
}