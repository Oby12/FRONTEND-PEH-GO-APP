package com.example.peh_goapp.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.DestinationDetailModel
import com.example.peh_goapp.data.model.DestinationModel
import com.example.peh_goapp.data.model.PictureModel
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.api.ApiService
import com.example.peh_goapp.data.remote.dto.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk operasi terkait destinasi wisata
 */
@Singleton
class DestinationRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenPreference: TokenPreference,
    private val appContext: Context // Inject context untuk mengakses file system
) {

    /**
     * Mendapatkan detail destinasi
     */
    suspend fun getDestinationDetail(categoryId: Int, destinationId: Int): ApiResult<DestinationDetailModel> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d("DestinationRepository", "Mengambil detail destinasi: $destinationId dari kategori: $categoryId")

            val response = apiService.getDestinationDetail(
                categoryId = categoryId,
                destinationId = destinationId,
                token = token
            )

            if (response.isSuccessful) {
                val detailResponse = response.body()
                if (detailResponse != null) {
                    val destinationDetail = detailResponse.data
                    val pictures = destinationDetail.picture.map { pictureDto ->
                        PictureModel(
                            id = pictureDto.id,
                            imageUrl = pictureDto.imageUrl
                        )
                    }

                    Log.d("DestinationRepository", "Berhasil mendapatkan detail dengan ${pictures.size} gambar")

                    return@withContext ApiResult.Success(
                        DestinationDetailModel(
                            id = destinationDetail.id,
                            name = destinationDetail.name,
                            address = destinationDetail.address,
                            description = destinationDetail.description,
                            urlLocation = destinationDetail.urlLocation,
                            coverUrl = destinationDetail.coverUrl,
                            categoryName = destinationDetail.Category.name,
                            pictures = pictures
                        )
                    )
                } else {
                    return@withContext ApiResult.Error("Respons data kosong")
                }
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e("DestinationRepository", "Error response: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e("DestinationRepository", "Error fetching destination detail: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    /**
     * Menghapus destinasi
     */
    suspend fun deleteDestination(categoryId: Int, destinationId: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d("DestinationRepository", "Menghapus destinasi: $destinationId dari kategori: $categoryId")

            val response = apiService.deleteDestination(
                categoryId = categoryId,
                destinationId = destinationId,
                token = token
            )

            if (response.isSuccessful) {
                Log.d("DestinationRepository", "Berhasil menghapus destinasi")
                return@withContext ApiResult.Success(Unit)
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e("DestinationRepository", "Error menghapus destinasi: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e("DestinationRepository", "Error deleting destination: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    /**
     * Mendapatkan daftar destinasi berdasarkan kategori
     */
    suspend fun getDestinations(categoryId: Int, page: Int = 1, limit: Int = 10): ApiResult<List<DestinationModel>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            // Log token untuk debugging (hanya 10 karakter pertama untuk keamanan)
            Log.d("DestinationRepository", "Using token: ${token.take(10)}...")

            // Mengirim token tanpa menambahkan prefix "Bearer "
            val response = apiService.getDestinations(
                categoryId = categoryId,
                token = token, // Tanpa prefix "Bearer "
                page = page,
                limit = limit
            )

            if (response.isSuccessful) {
                val destinationsResponse = response.body()
                if (destinationsResponse != null) {
                    val destinations = destinationsResponse.data.map { dto ->
                        DestinationModel(
                            id = dto.id,
                            name = dto.name,
                            address = dto.address,
                            description = dto.description,
                            urlLocation = dto.urlLocation,
                            coverUrl = dto.coverUrl
                        )
                    }
                    return@withContext ApiResult.Success(destinations)
                } else {
                    return@withContext ApiResult.Error("Respons data kosong")
                }
            } else {
                val errorMessage = parseErrorResponse(response)
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e("DestinationRepository", "Error fetching destinations: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    /**
     * Menambahkan destinasi baru
     */
    suspend fun addDestination(
        categoryId: Int,
        name: String,
        address: String,
        description: String,
        urlLocation: String,
        coverImageUri: Uri,
        pictureImageUris: List<Uri>?
    ): ApiResult<DestinationModel> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d("DestinationRepository", "Preparing to upload destination: $name")

            // Konversi fields ke RequestBody
            val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val addressRequestBody = address.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val urlLocationRequestBody = urlLocation.toRequestBody("text/plain".toMediaTypeOrNull())

            Log.d("DestinationRepository", "Converting cover image to file")

            // Konversi cover image ke MultipartBody.Part
            val coverFile = getFileFromUri(coverImageUri)
            val coverRequestFile = coverFile.asRequestBody("image/*".toMediaTypeOrNull())
            val coverPart = MultipartBody.Part.createFormData("cover", coverFile.name, coverRequestFile)

            Log.d("DestinationRepository", "Cover image prepared: ${coverFile.length()} bytes")

            // Konversi picture images ke MultipartBody.Part list jika ada
            val pictureParts = pictureImageUris?.mapNotNull { uri ->
                try {
                    val pictureFile = getFileFromUri(uri)
                    val pictureRequestFile = pictureFile.asRequestBody("image/*".toMediaTypeOrNull())
                    Log.d("DestinationRepository", "Picture prepared: ${pictureFile.length()} bytes")
                    MultipartBody.Part.createFormData("picture", pictureFile.name, pictureRequestFile)
                } catch (e: Exception) {
                    Log.e("DestinationRepository", "Error converting picture: ${e.message}")
                    null
                }
            }

            Log.d("DestinationRepository", "Sending request to API with ${pictureParts?.size ?: 0} additional pictures")

            // Kirim request ke API
            val response = apiService.addDestination(
                categoryId = categoryId,
                token = token,
                name = nameRequestBody,
                address = addressRequestBody,
                description = descriptionRequestBody,
                urlLocation = urlLocationRequestBody,
                cover = coverPart,
                picture = pictureParts
            )

            if (response.isSuccessful) {
                val destinationResponse = response.body()
                if (destinationResponse != null) {
                    val destination = destinationResponse.data
                    Log.d("DestinationRepository", "Destination added successfully with ID: ${destination.id}")

                    return@withContext ApiResult.Success(
                        DestinationModel(
                            id = destination.id,
                            name = destination.name,
                            address = destination.address,
                            description = destination.description,
                            urlLocation = destination.urlLocation,
                            coverUrl = destination.coverUrl
                        )
                    )
                } else {
                    Log.e("DestinationRepository", "Response body is null despite successful response")
                    return@withContext ApiResult.Error("Respons data kosong")
                }
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e("DestinationRepository", "API error: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e("DestinationRepository", "Error adding destination: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    /**
     * Mengkonversi URI ke File
     */
    private fun getFileFromUri(uri: Uri): File {
        // Mendapatkan ekstensi file dari MIME type
        val contentResolver = appContext.contentResolver
        val mimeType = contentResolver.getType(uri)
        val fileExtension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType) ?: "jpg" // Default ke jpg jika tidak ditemukan

        // Buat file temporary di cache
        val tempFile = File(
            appContext.cacheDir,
            "upload_${System.currentTimeMillis()}.$fileExtension"
        )

        // Salin data dari URI ke file
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IllegalStateException("Tidak dapat membuka file dari URI: $uri")

            Log.d("DestinationRepository", "File created from URI: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")

            return tempFile
        } catch (e: Exception) {
            Log.e("DestinationRepository", "Error creating file from URI: ${e.message}", e)
            throw e
        }
    }

    /**
     * Parse response error dari API
     */
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