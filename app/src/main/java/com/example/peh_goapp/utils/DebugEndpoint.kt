package com.example.peh_goapp.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utilitas untuk menguji endpoint API secara langsung
 * Gunakan fungsi ini untuk debugging, membantu isolasi masalah
 */
object DebugEndpoint {
    private val TAG = "DebugEndpoint"

    /**
     * Melakukan test langsung terhadap endpoint gambar
     * Menyimpan hasilnya ke file temporary dan menampilkan informasi debug
     *
     * @param context Context aplikasi
     * @param url URL endpoint yang akan diuji
     * @param token Token autentikasi (opsional)
     */
    fun testImageEndpoint(context: Context, url: String, token: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Buat client dan request
                val client = OkHttpClient.Builder().build()
                val requestBuilder = Request.Builder().url(url)

                // Tambahkan header Authorization jika token disediakan
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", token)
                }

                // Eksekusi request
                val response: Response = client.newCall(requestBuilder.build()).execute()

                // Log informasi response
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response headers: ${response.headers}")

                if (response.isSuccessful) {
                    // Dapatkan byte array dari body
                    val responseBody = response.body

                    if (responseBody != null) {
                        val contentType = response.header("Content-Type")
                        Log.d(TAG, "Content-Type: $contentType")

                        val contentLength = responseBody.contentLength()
                        Log.d(TAG, "Content length: $contentLength bytes")

                        // Baca byte array
                        val bytes = responseBody.bytes()

                        // Simpan ke file temporary untuk inspeksi
                        val file = File(context.cacheDir, "debug_image.jpg")
                        FileOutputStream(file).use { output ->
                            output.write(bytes)
                        }

                        // Log informasi file
                        Log.d(TAG, "Image saved to: ${file.absolutePath}")
                        Log.d(TAG, "File size: ${file.length()} bytes")

                        // Update UI
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Debug image saved (${bytes.size} bytes)",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Log.e(TAG, "Response body is null")
                    }
                } else {
                    // Log error
                    Log.e(TAG, "Error: ${response.code} - ${response.message}")
                    val errorBody = response.body?.string()
                    Log.e(TAG, "Error body: $errorBody")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Error: ${response.code} - ${response.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Exception: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Exception: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}