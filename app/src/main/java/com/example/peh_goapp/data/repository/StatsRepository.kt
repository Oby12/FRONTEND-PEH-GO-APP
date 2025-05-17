package com.example.peh_goapp.data.repository

import android.util.Log
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.StatsModel
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.api.ApiService
import com.example.peh_goapp.data.remote.dto.StatsResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenPreference: TokenPreference
) {
    private val TAG = "StatsRepository"

    // Mendapatkan statistik untuk admin
    suspend fun getStats(): ApiResult<StatsModel> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Mengambil statistik dengan token: ${token.take(10)}...")

            val response = apiService.getStats(token)

            if (response.isSuccessful) {
                val statsResponse = response.body()
                if (statsResponse != null) {
                    Log.d(TAG, "Berhasil mendapatkan statistik")
                    return@withContext ApiResult.Success(statsResponse.data)
                } else {
                    Log.e(TAG, "Response body kosong")
                    return@withContext ApiResult.Error("Respons data kosong")
                }
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e(TAG, "Error response: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stats: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    // Mencatat view destinasi
    suspend fun recordDestinationView(destinationId: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan")
            }

            Log.d(TAG, "Mencatat view untuk destinasi: $destinationId")

            val response = apiService.recordDestinationView(destinationId, token)

            if (response.isSuccessful) {
                Log.d(TAG, "Berhasil mencatat view")
                return@withContext ApiResult.Success(Unit)
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e(TAG, "Gagal mencatat view: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recording view: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    // Parse error response dari API
    private fun <T> parseErrorResponse(response: Response<T>): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val errorResponse = Gson().fromJson(errorBody, com.example.peh_goapp.data.remote.dto.ErrorResponse::class.java)
            errorResponse?.message ?: "Terjadi kesalahan: ${response.code()}"
        } catch (e: Exception) {
            "Terjadi kesalahan: ${response.code()}"
        }
    }
}