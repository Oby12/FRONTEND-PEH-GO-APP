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
    private val TAG = "DestinationRepository"

    /**
     * Mendapatkan detail destinasi
     */
    suspend fun getDestinationDetail(categoryId: Int, destinationId: Int): ApiResult<DestinationDetailModel> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Mengambil detail destinasi: $destinationId dari kategori: $categoryId")

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

                    Log.d(TAG, "Berhasil mendapatkan detail dengan ${pictures.size} gambar")

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
                Log.e(TAG, "Error response: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching destination detail: ${e.message}", e)
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

            Log.d(TAG, "Menghapus destinasi: $destinationId dari kategori: $categoryId")

            val response = apiService.deleteDestination(
                categoryId = categoryId,
                destinationId = destinationId,
                token = token
            )

            if (response.isSuccessful) {
                Log.d(TAG, "Berhasil menghapus destinasi")
                return@withContext ApiResult.Success(Unit)
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e(TAG, "Error menghapus destinasi: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting destination: ${e.message}", e)
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
            Log.d(TAG, "Using token: ${token.take(10)}...")

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
            Log.e(TAG, "Error fetching destinations: ${e.message}", e)
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

            Log.d(TAG, "Preparing to upload destination: $name")

            // Konversi fields ke RequestBody
            val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val addressRequestBody = address.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val urlLocationRequestBody = urlLocation.toRequestBody("text/plain".toMediaTypeOrNull())

            Log.d(TAG, "Converting cover image to file")

            // Konversi cover image ke MultipartBody.Part
            val coverFile = getFileFromUri(coverImageUri)
            val coverRequestFile = coverFile.asRequestBody("image/*".toMediaTypeOrNull())
            val coverPart = MultipartBody.Part.createFormData("cover", coverFile.name, coverRequestFile)

            Log.d(TAG, "Cover image prepared: ${coverFile.length()} bytes")

            // Konversi picture images ke MultipartBody.Part list jika ada
            val pictureParts = pictureImageUris?.mapNotNull { uri ->
                try {
                    val pictureFile = getFileFromUri(uri)
                    val pictureRequestFile = pictureFile.asRequestBody("image/*".toMediaTypeOrNull())
                    Log.d(TAG, "Picture prepared: ${pictureFile.length()} bytes")
                    MultipartBody.Part.createFormData("picture", pictureFile.name, pictureRequestFile)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting picture: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Sending request to API with ${pictureParts?.size ?: 0} additional pictures")

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
                    Log.d(TAG, "Destination added successfully with ID: ${destination.id}")

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
                    Log.e(TAG, "Response body is null despite successful response")
                    return@withContext ApiResult.Error("Respons data kosong")
                }
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e(TAG, "API error: $errorMessage")
                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding destination: ${e.message}", e)
            return@withContext ApiResult.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    /**
     * Mengupdate destinasi yang sudah ada
     */
    suspend fun updateDestination(
        categoryId: Int,
        destinationId: Int,
        name: String,
        address: String,
        description: String,
        urlLocation: String,
        coverImageUri: Uri? = null,
        pictureImageUris: List<Uri>? = null,
        removedPictureIds: List<Int>? = null
    ): ApiResult<DestinationModel> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreference.getToken()
            if (token.isBlank()) {
                return@withContext ApiResult.Error("Token tidak ditemukan. Silakan login terlebih dahulu.")
            }

            Log.d(TAG, "Mempersiapkan update destinasi: $name (ID: $destinationId)")
            Log.d(TAG, "Data update: categoryId=$categoryId, name=$name, address=$address")
            Log.d(TAG, "coverImageUri=$coverImageUri, pictureImageUris=${pictureImageUris?.size ?: 0}")
            Log.d(TAG, "removedPictureIds=$removedPictureIds")

            // Konversi fields ke RequestBody
            val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val addressRequestBody = address.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val urlLocationRequestBody = urlLocation.toRequestBody("text/plain".toMediaTypeOrNull())

            // Jika ada ID gambar yang akan dihapus, tambahkan ke request sebagai string
            val removedPictureIdsRequestBody = if (removedPictureIds != null && removedPictureIds.isNotEmpty()) {
                // Kirim langsung sebagai string CSV, bukan JSON - ini untuk menghindari masalah parsing di backend
                val idsString = removedPictureIds.joinToString(",")
                Log.d(TAG, "removedPictureIds string CSV: $idsString")
                idsString.toRequestBody("text/plain".toMediaTypeOrNull())
            } else {
                null
            }

            // Variabel untuk menyimpan coverPart
            var coverPart: MultipartBody.Part? = null

            // Konversi cover image ke MultipartBody.Part jika ada
            if (coverImageUri != null) {
                Log.d(TAG, "Mengkonversi gambar cover")
                val coverFile = getFileFromUri(coverImageUri)
                val coverRequestFile = coverFile.asRequestBody("image/*".toMediaTypeOrNull())
                coverPart = MultipartBody.Part.createFormData("cover", coverFile.name, coverRequestFile)
                Log.d(TAG, "Cover image disiapkan: ${coverFile.length()} bytes")
            }

            // Konversi picture images ke MultipartBody.Part list jika ada
            val pictureParts = pictureImageUris?.mapNotNull { uri ->
                try {
                    val pictureFile = getFileFromUri(uri)
                    val pictureRequestFile = pictureFile.asRequestBody("image/*".toMediaTypeOrNull())
                    Log.d(TAG, "Picture disiapkan: ${pictureFile.length()} bytes")
                    MultipartBody.Part.createFormData("picture", pictureFile.name, pictureRequestFile)
                } catch (e: Exception) {
                    Log.e(TAG, "Error mengkonversi gambar: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Mengirim request update ke API dengan ${pictureParts?.size ?: 0} gambar tambahan")
            Log.d(TAG, "Token: ${token.take(10)}...")

            // Kirim request ke API
            val response = if (removedPictureIdsRequestBody != null) {
                Log.d(TAG, "Mengirim request dengan removedPictureIds")
                // Request dengan removedPictureIds
                apiService.updateDestination(
                    categoryId = categoryId,
                    destinationId = destinationId,
                    token = token,
                    name = nameRequestBody,
                    address = addressRequestBody,
                    description = descriptionRequestBody,
                    urlLocation = urlLocationRequestBody,
                    removedPictureIds = removedPictureIdsRequestBody,
                    cover = coverPart,
                    picture = pictureParts
                )
            } else {
                Log.d(TAG, "Mengirim request tanpa removedPictureIds")
                // Request tanpa removedPictureIds
                apiService.updateDestination(
                    categoryId = categoryId,
                    destinationId = destinationId,
                    token = token,
                    name = nameRequestBody,
                    address = addressRequestBody,
                    description = descriptionRequestBody,
                    urlLocation = urlLocationRequestBody,
                    cover = coverPart,
                    picture = pictureParts
                )
            }

            if (response.isSuccessful) {
                val destinationResponse = response.body()
                if (destinationResponse != null) {
                    val destination = destinationResponse.data

                    // Pastikan semua field yang diperlukan tidak null
                    // Gunakan nilai dari parameter fungsi jika nilai dari API null
                    val safeDestination = DestinationModel(
                        id = destination.id ?: destinationId,
                        name = destination.name ?: name,
                        address = destination.address ?: address,
                        description = destination.description ?: description,
                        urlLocation = destination.urlLocation ?: urlLocation,
                        coverUrl = destination.coverUrl ?: ""
                    )

                    Log.d(TAG, "Destinasi berhasil diupdate dengan ID: ${safeDestination.id}")
                    return@withContext ApiResult.Success(safeDestination)
                } else {
                    Log.e(TAG, "Response body null meskipun response sukses")
                    // Return model dengan data dari parameter, bukan dari response
                    val fallbackModel = DestinationModel(
                        id = destinationId,
                        name = name,
                        address = address,
                        description = description,
                        urlLocation = urlLocation,
                        coverUrl = ""
                    )
                    return@withContext ApiResult.Success(fallbackModel)
                }
            } else {
                val errorMessage = parseErrorResponse(response)
                Log.e(TAG, "API error: $errorMessage")
                Log.e(TAG, "Response code: ${response.code()}")

                // Log response body for debugging
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error body: $errorBody")
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading error body: ${e.message}")
                }

                return@withContext ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error update destinasi: ${e.message}", e)
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

            Log.d(TAG, "File created from URI: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")

            return tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating file from URI: ${e.message}", e)
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