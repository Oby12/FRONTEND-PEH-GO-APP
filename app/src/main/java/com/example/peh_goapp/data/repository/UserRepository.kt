package com.example.peh_goapp.data.repository

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

                if (token != null) {
                    // Simpan token ke SharedPreferences
                    tokenPreference.saveToken(token)
                    return@withContext ApiResult.Success(token)
                } else {
                    return@withContext ApiResult.Error("Token tidak ditemukan dalam respons")
                }
            } else {
                val errorMessage = parseErrorResponse(response)
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    suspend fun logout(): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Anda belum login")
            }

            val response = apiService.logout("Bearer $token")
            if (response.isSuccessful) {
                // Hapus token dari SharedPreferences
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

    private fun <T> parseErrorResponse(response: Response<T>): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse?.errors ?: "Terjadi kesalahan: ${response.code()}"
        } catch (e: Exception) {
            "Terjadi kesalahan: ${response.code()}"
        }
    }
}