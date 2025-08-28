package com.example.peh_goapp.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.OtherDetailModel
import com.example.peh_goapp.data.model.OtherModel
import com.example.peh_goapp.data.remote.api.ApiService // Use main ApiService
import com.example.peh_goapp.data.remote.api.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtherRepository @Inject constructor(
    private val apiService: ApiService, // Use main ApiService instead of OtherApiService
    private val tokenPreference: TokenPreference,
    private val contentResolver: ContentResolver
) {
    private val TAG = "OtherRepository"

    suspend fun getAllOthers(): ApiResult<List<OtherModel>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Getting others with token: ${token.take(10)}...")

            // FIX: Use correct format and check response
            val response = apiService.getAllOthers("Bearer $token")

            if (response.isSuccessful) {
                val otherResponse = response.body()
                if (otherResponse?.status == true && otherResponse.data != null) {
                    val otherModels = otherResponse.data.map { dto ->
                        OtherModel(
                            id = dto.id,
                            name = dto.name,
                            category = dto.category,
                            story = dto.story,
                            coverUrl = "https://3ae27ba53912.ngrok-free.app/api/images/other/covers/${dto.id}",
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt
                        )
                    }
                    Log.d(TAG, "Successfully loaded ${otherModels.size} others")
                    ApiResult.Success(otherModels)
                } else {
                    val message = otherResponse?.message ?: "Unknown error"
                    Log.e(TAG, "API returned error: $message")
                    ApiResult.Error(message)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP Error ${response.code()}: $errorBody")
                ApiResult.Error("Failed to load others: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting all others: ${e.message}", e)
            ApiResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun getOtherById(otherId: Int): ApiResult<OtherDetailModel> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Getting other detail for ID: $otherId")

            val response = apiService.getOtherById("Bearer $token", otherId)

            if (response.isSuccessful) {
                val otherResponse = response.body()
                if (otherResponse?.status == true && otherResponse.data != null) {
                    val otherDetail = OtherDetailModel(
                        id = otherResponse.data.id,
                        name = otherResponse.data.name,
                        category = otherResponse.data.category,
                        story = otherResponse.data.story,
                        coverUrl = "https://3ae27ba53912.ngrok-free.app/api/images/other/covers/${otherResponse.data.id}",
                        createdAt = otherResponse.data.createdAt,
                        updatedAt = otherResponse.data.updatedAt
                    )
                    ApiResult.Success(otherDetail)
                } else {
                    ApiResult.Error(otherResponse?.message ?: "Failed to get other detail")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP Error ${response.code()}: $errorBody")
                ApiResult.Error("Failed to get other detail: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting other detail: ${e.message}", e)
            ApiResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun createOther(
        name: String,
        category: String,
        story: String,
        coverImageUri: Uri
    ): ApiResult<OtherDetailModel> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Creating other: $name")
            Log.d(TAG, "Using token: ${token.take(10)}...")

            // Prepare request data
            val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryRequestBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
            val storyRequestBody = story.toRequestBody("text/plain".toMediaTypeOrNull())

            // Convert image
            val coverFile = getFileFromUri(coverImageUri)
            val coverRequestFile = coverFile.asRequestBody("image/*".toMediaTypeOrNull())
            val coverPart = MultipartBody.Part.createFormData("cover", coverFile.name, coverRequestFile)

            Log.d(TAG, "Sending create request to API...")

            val response = apiService.createOther(
                token = "Bearer $token",
                name = nameRequestBody,
                category = categoryRequestBody,
                story = storyRequestBody,
                cover = coverPart
            )

            Log.d(TAG, "API Response - Code: ${response.code()}, Success: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val otherResponse = response.body()
                if (otherResponse?.status == true && otherResponse.data != null) {
                    val otherDetail = OtherDetailModel(
                        id = otherResponse.data.id,
                        name = otherResponse.data.name,
                        category = otherResponse.data.category,
                        story = otherResponse.data.story,
                        coverUrl = "https://3ae27ba53912.ngrok-free.app/api/images/other/covers/${otherResponse.data.id}",
                        createdAt = otherResponse.data.createdAt,
                        updatedAt = otherResponse.data.updatedAt
                    )
                    Log.d(TAG, "Successfully created other with ID: ${otherDetail.id}")
                    ApiResult.Success(otherDetail)
                } else {
                    val message = otherResponse?.message ?: "Unknown error"
                    Log.e(TAG, "API returned error: $message")
                    ApiResult.Error(message)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP Error ${response.code()}: $errorBody")
                ApiResult.Error("Failed to create other: HTTP ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating other: ${e.message}", e)
            ApiResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun deleteOther(otherId: Int): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            val response = apiService.deleteOther("Bearer $token", otherId)

            if (response.isSuccessful) {
                val deleteResponse = response.body()
                if (deleteResponse?.status == true) {
                    ApiResult.Success(deleteResponse.message)
                } else {
                    ApiResult.Error(deleteResponse?.message ?: "Failed to delete other")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP Error ${response.code()}: $errorBody")
                ApiResult.Error("Failed to delete other: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception deleting other: ${e.message}", e)
            ApiResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        val file = File.createTempFile("upload", ".jpg")

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }
}