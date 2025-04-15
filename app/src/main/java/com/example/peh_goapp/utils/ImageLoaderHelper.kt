package com.example.peh_goapp.util

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.CachePolicy
import com.example.peh_goapp.data.local.TokenPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Helper class untuk membuat ImageLoader yang menyertakan token autentikasi
 * untuk request gambar menggunakan Coil
 */
object ImageLoaderHelper {

    /**
     * Membuat ImageLoader yang menyertakan token autentikasi pada setiap request
     * @param context Context aplikasi
     * @param tokenPreference TokenPreference untuk mengambil token autentikasi
     * @return ImageLoader yang dapat digunakan dengan Coil
     */
    fun createAuthenticatedImageLoader(context: Context, tokenPreference: TokenPreference): ImageLoader {
        val token = tokenPreference.getToken()
        Log.d("ImageLoaderHelper", "Creating ImageLoader with token: ${token.take(10)}...")

        // Logging interceptor untuk debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // OkHttpClient dengan timeout yang lebih lama dan error handling yang lebih baik
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()

                // Tambahkan header Authorization
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", token)
                    .build()

                try {
                    chain.proceed(newRequest)
                } catch (e: Exception) {
                    Log.e("ImageLoaderHelper", "Error during request: ${e.message}", e)
                    throw e
                }
            }
            .connectTimeout(60, TimeUnit.SECONDS)  // Timeout lebih lama
            .readTimeout(60, TimeUnit.SECONDS)     // Timeout lebih lama
            .retryOnConnectionFailure(true)        // Retry jika koneksi gagal
            .build()

        return ImageLoader.Builder(context)
            .okHttpClient(client)
            .crossfade(true)
            .build()
    }
}