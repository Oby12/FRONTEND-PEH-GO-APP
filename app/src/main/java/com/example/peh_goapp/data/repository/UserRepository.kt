package com.example.peh_goapp.data.repository

import android.util.Log
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.api.ApiService
import com.example.peh_goapp.data.remote.dto.ErrorResponse
import com.example.peh_goapp.data.remote.dto.LoginRequest
import com.example.peh_goapp.data.remote.dto.RegisterRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenPreference: TokenPreference
) {
    private val TAG = "UserRepository"

    suspend fun register(request: RegisterRequest): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(request)
            if (response.isSuccessful) {
                return@withContext ApiResult.Success(Unit)
            } else {
                val errorMessage = parseErrorResponse(response)
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    suspend fun login(request: LoginRequest): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(request)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                val token = loginResponse?.data?.token
                val role = loginResponse?.data?.role
                val user = loginResponse?.data?.user

                if (token != null) {
                    // Simpan token ke SharedPreferences
                    tokenPreference.saveToken(token)

                    // Simpan role jika ada
                    role?.let { tokenPreference.saveRole(it) }

                    // Simpan informasi user jika tersedia
                    user?.let {
                        tokenPreference.saveUsername(it.username)
                        tokenPreference.saveName(it.name)
                        tokenPreference.saveEmail(it.email)

                        Log.d("UserRepository", "User data saved: ${it.username}, ${it.name}, ${it.email}")
                    }

                    return@withContext ApiResult.Success(token)
                } else {
                    return@withContext ApiResult.Error("Token tidak ditemukan dalam respons")
                }
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e(TAG, "Login error: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login exception: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    suspend fun logout(): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Anda belum login")
            }

            val response = apiService.logout(token)
            if (response.isSuccessful) {
                // Hapus semua data user dari SharedPreferences
                tokenPreference.clearToken()
                return@withContext ApiResult.Success("Logout berhasil")
            } else {
                val errorMessage = parseErrorResponse(response)
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    fun isLoggedIn(): Boolean {
        return tokenPreference.getToken().isNotBlank()
    }

    // Fungsi untuk mendapatkan nama pengguna
    fun getUserName(): String {
        val name = tokenPreference.getName()
        val username = tokenPreference.getUsername()

        // Jika nama tidak ada, gunakan username sebagai fallback
        return if (name.isNotBlank()) name else username
    }

    /**
     * Parse error response dari API menggunakan model ErrorResponse
     */
    private fun <T> parseErrorResponse(response: Response<T>): String {
        val errorBody = response.errorBody()?.string()
        Log.d(TAG, "Error body: $errorBody")

        if (errorBody.isNullOrBlank()) {
            return "Terjadi kesalahan: ${response.code()}"
        }

        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

            // Pilih pesan error berdasarkan prioritas
            val errorMessage = when {
                !errorResponse.message.isNullOrBlank() -> errorResponse.message
                !errorResponse.errors.isNullOrBlank() -> errorResponse.errors  // Perubahan di sini
                else -> "Terjadi kesalahan: ${response.code()}"
            }

            // Terjemahkan pesan error
            translateErrorMessage(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing error response: ${e.message}", e)
            "Terjadi kesalahan: ${response.code()}"
        }
    }

    /**
     * Menerjemahkan pesan error dari API ke Bahasa Indonesia
     */
    private fun translateErrorMessage(errorMessage: String): String {
        return when (errorMessage) {
            "Email or password is wrong" -> "Email atau password salah"
            "Email already exist" -> "Email sudah digunakan"
            "Username already taken" -> "Username sudah digunakan"
            "Token tidak ditemukan dalam respons" -> "Token tidak ditemukan dalam respons"
            "Unauthorized" -> "Tidak memiliki akses"
            "Unauthorized: Token is required" -> "Token diperlukan untuk akses"
            "Unauthorized: Invalid token" -> "Token tidak valid"
            "Forbidden: You do not have permission" -> "Anda tidak memiliki izin"
            "Destinasi tidak ditemukan" -> "Destinasi tidak ditemukan"
            "Kategori tidak ditemukan" -> "Kategori tidak ditemukan"
            else -> errorMessage // Jika tidak ada terjemahan, gunakan pesan asli
        }
    }
}