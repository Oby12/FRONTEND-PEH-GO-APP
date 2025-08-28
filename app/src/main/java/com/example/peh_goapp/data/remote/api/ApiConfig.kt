package com.example.peh_goapp.data.remote.api

import android.util.Log

/**
 * Konfigurasi API dan URL untuk aplikasi
 */
object ApiConfig {

    const val BASE_URL = "https://3aa27ba53912.ngrok-free.app/"

    /**
     * Method untuk mendapatkan URL lengkap gambar cover biasa
     * @param destinationId ID dari destinasi
     */
    fun getCoverImageUrl(destinationId: Int): String {
        val url = "${BASE_URL}api/images/covers/$destinationId"
        Log.d("ApiConfig", "Generated cover URL: $url")
        return url
    }

    /**
     * Method untuk mendapatkan URL lengkap gambar cover base64
     * @param destinationId ID dari destinasi
     */
    fun getBase64CoverImageUrl(destinationId: Int): String {
        val url = "${BASE_URL}api/images/base64/covers/$destinationId"
        Log.d("ApiConfig", "Generated base64 cover URL: $url")
        return url
    }

    /**
     * Method untuk mendapatkan URL lengkap gambar tambahan
     * @param pictureId ID dari gambar
     * @return URL lengkap untuk mengakses gambar
     */
    fun getPictureImageUrl(pictureId: Int): String {
        val url = "${BASE_URL}api/images/pictures/$pictureId"
        Log.d("ApiConfig", "Generated picture URL: $url")
        return url
    }

    /**
     * Method untuk mendapatkan URL lengkap gambar tambahan base64
     * @param pictureId ID dari gambar
     * @return URL lengkap untuk mengakses gambar base64
     */
    fun getBase64PictureImageUrl(pictureId: Int): String {
        val url = "${BASE_URL}api/images/base64/pictures/$pictureId"
        Log.d("ApiConfig", "Generated base64 picture URL: $url")
        return url
    }

    /**
     * Method untuk menormalisasi URL
     * Menangani berbagai format URL (lengkap, relatif, ID saja)
     * @param url URL yang akan dinormalisasi
     * @param defaultId ID default jika hanya ID yang diberikan
     * @param useBase64 Gunakan endpoint base64 jika true
     * @return URL yang dinormalisasi
     */
    fun normalizeImageUrl(url: String?, defaultId: Int, useBase64: Boolean = true): String {
        if (url == null || url.isBlank()) {
            return if (useBase64) {
                getBase64CoverImageUrl(defaultId)
            } else {
                getCoverImageUrl(defaultId)
            }
        }

        // Jika URL sudah lengkap, kembalikan langsung
        if (url.startsWith("http")) {
            return url
        }

        // Jika URL dimulai dengan /api
        if (url.startsWith("/api")) {
            return BASE_URL + url.removePrefix("/")
        }

        // Jika URL hanya berisi ID
        if (url.matches(Regex("\\d+"))) {
            val id = url.toIntOrNull() ?: defaultId
            return if (useBase64) {
                getBase64CoverImageUrl(id)
            } else {
                getCoverImageUrl(id)
            }
        }

        // Jika URL berisi path base64
        if (url.contains("base64")) {
            if (!url.startsWith(BASE_URL)) {
                return BASE_URL + url.removePrefix("/")
            }
            return url
        }

        // Default fallback
        return if (useBase64) {
            getBase64CoverImageUrl(defaultId)
        } else {
            getCoverImageUrl(defaultId)
        }
    }
}