package com.example.peh_goapp.data.remote.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.util.DebugEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service untuk mengelola dan mengambil gambar base64 dari API
 */
@Singleton
class Base64ImageService @Inject constructor(
    private val api: Base64ImageApi,
    private val tokenPreference: TokenPreference
) {
    private val TAG = "Base64ImageService"

    // Cache untuk gambar yang sudah dimuat
    private val imageCache = mutableMapOf<String, Bitmap>()

    /**
     * Mengambil gambar cover dalam format base64 dan mengkonversinya ke Bitmap
     * @param destinationId ID dari destinasi
     * @return Bitmap dari gambar, atau null jika gagal
     */
    suspend fun getDestinationCoverImage(destinationId: Int): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "cover_$destinationId"

        // Cek apakah gambar sudah ada di cache
        imageCache[cacheKey]?.let {
            Log.d(TAG, "Using cached image for destination $destinationId")
            return@withContext it
        }

        try {
            val token = tokenPreference.getToken()
            Log.d(TAG, "Requesting base64 image for destination $destinationId with token: ${token.take(10)}...")

            // Gunakan try-catch untuk setiap permintaan API
            try {
                val response = api.getBase64CoverImage(destinationId, token)

                if (response.isSuccessful) {
                    val base64Data = response.body()?.imageData

                    if (base64Data != null && base64Data.isNotEmpty()) {
                        Log.d(TAG, "Received base64 data of length: ${base64Data.length}")

                        // Hapus prefix data URI jika ada
                        val cleanBase64 = if (base64Data.contains(",")) {
                            base64Data.split(",")[1]
                        } else {
                            base64Data
                        }

                        try {
                            // Decode base64 ke byte array
                            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                            Log.d(TAG, "Decoded image bytes size: ${imageBytes.size}")

                            // Konversi ke bitmap dengan opsi
                            val options = BitmapFactory.Options().apply {
                                inPreferredConfig = Bitmap.Config.ARGB_8888
                            }

                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

                            if (bitmap != null) {
                                Log.d(TAG, "Successfully created bitmap: ${bitmap.width}x${bitmap.height}")
                                // Simpan ke cache
                                imageCache[cacheKey] = bitmap
                                return@withContext bitmap
                            } else {
                                Log.e(TAG, "Failed to decode bitmap from bytes")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error decoding base64: ${e.message}", e)
                        }
                    } else {
                        Log.e(TAG, "Empty or null base64 data received")
                    }
                } else {
                    Log.e(TAG, "Error response: ${response.code()} - ${response.message()}")

                    // Log response body untuk debugging
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error body: $errorBody")
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not read error body: ${e.message}")
                    }
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP exception: ${e.code()} - ${e.message()}", e)
            } catch (e: IOException) {
                Log.e(TAG, "IO exception: ${e.message}", e)
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting base64 image: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Mengambil gambar picture dalam format base64 dan mengkonversinya ke Bitmap
     * @param pictureId ID dari picture
     * @return Bitmap dari gambar, atau null jika gagal
     */
    suspend fun getPictureImage(pictureId: Int): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "picture_$pictureId"

        // Cek apakah gambar sudah ada di cache
        imageCache[cacheKey]?.let {
            Log.d(TAG, "Using cached image for picture $pictureId")
            return@withContext it
        }

        try {
            val token = tokenPreference.getToken()
            Log.d(TAG, "Requesting base64 image for picture $pictureId")

            val response = api.getBase64PictureImage(pictureId, token)

            if (response.isSuccessful) {
                val base64Data = response.body()?.imageData

                if (base64Data != null && base64Data.isNotEmpty()) {
                    // Hapus prefix data URI jika ada
                    val cleanBase64 = if (base64Data.contains(",")) {
                        base64Data.split(",")[1]
                    } else {
                        base64Data
                    }

                    try {
                        // Decode base64 ke byte array
                        val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

                        // Konversi ke bitmap dengan opsi
                        val options = BitmapFactory.Options().apply {
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }

                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

                        if (bitmap != null) {
                            // Simpan ke cache
                            imageCache[cacheKey] = bitmap
                            return@withContext bitmap
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error decoding base64 for picture: ${e.message}", e)
                    }
                }
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting base64 picture: ${e.message}", e)
            return@withContext null
        }
    }

    // Method untuk membersihkan cache
    fun clearCache() {
        imageCache.clear()
    }
}