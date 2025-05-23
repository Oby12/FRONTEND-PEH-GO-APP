package com.example.peh_goapp.data.repository

import android.util.Log
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.DestinationModel
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.api.ApiService
import com.example.peh_goapp.data.remote.dto.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenPreference: TokenPreference
) {
    private val TAG = "FavoriteRepository"


     //Mendapatkan daftar destinasi favorit pengguna
    suspend fun getFavorites(): ApiResult<List<DestinationModel>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Mengambil data favorit dengan token: ${token.take(10)}...")

            val response = apiService.getFavorites(token)

            if (response.isSuccessful) {
                val favoritesResponse = response.body()
                if (favoritesResponse != null) {
                    val destinations = favoritesResponse.data.map { dto ->
                        DestinationModel(
                            id = dto.id,
                            name = dto.name,
                            address = dto.address,
                            description = dto.description,
                            urlLocation = dto.urlLocation,
                            coverUrl = dto.coverUrl,
                            isFavorite = true // Semua destinasi dalam daftar favorit adalah favorit
                        )
                    }
                    Log.d(TAG, "Berhasil mendapatkan ${destinations.size} favorit")
                    return@withContext ApiResult.Success(destinations)
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
            Log.e(TAG, "Error fetching favorites: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }


     //Toggle status favorit untuk sebuah destinasi

    suspend fun toggleFavorite(destinationId: Int): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Toggle favorit untuk destinasi: $destinationId")

            val response = apiService.toggleFavorite(destinationId, token)

            if (response.isSuccessful) {
                val toggleResponse = response.body()
                if (toggleResponse != null) {
                    Log.d(TAG, "Toggle berhasil, status favorit: ${toggleResponse.data.isFavorite}")
                    return@withContext ApiResult.Success(toggleResponse.data.isFavorite)
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
            Log.e(TAG, "Error toggling favorite: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }


     // Memeriksa apakah destinasi adalah favorit

    suspend fun checkIsFavorite(destinationId: Int): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Memeriksa status favorit untuk destinasi: $destinationId")

            val response = apiService.checkIsFavorite(destinationId, token)

            if (response.isSuccessful) {
                val checkResponse = response.body()
                if (checkResponse != null) {
                    Log.d(TAG, "Status favorit: ${checkResponse.data.isFavorite}")
                    return@withContext ApiResult.Success(checkResponse.data.isFavorite)
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
            Log.e(TAG, "Error checking favorite status: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }


     // Parse response error dari API

    private fun <T> parseErrorResponse(response: Response<T>): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse?.message ?: "Terjadi kesalahan: ${response.code()}"
        } catch (e: Exception) {
            "Terjadi kesalahan: ${response.code()}"
        }
    }
}