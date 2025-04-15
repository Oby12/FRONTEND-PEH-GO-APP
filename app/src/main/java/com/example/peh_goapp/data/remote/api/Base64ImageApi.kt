package com.example.peh_goapp.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Interface untuk API yang mengembalikan gambar sebagai base64
 */
interface Base64ImageApi {
    /**
     * Mengambil gambar cover sebagai base64 string
     * @param destinationId ID destinasi
     * @param token Token autentikasi
     * @return Response dengan Base64ImageResponse yang berisi data gambar
     */
    @GET("api/images/base64/covers/{id}")
    suspend fun getBase64CoverImage(
        @Path("id") destinationId: Int,
        @Header("Authorization") token: String
    ): Response<Base64ImageResponse>

    /**
     * Mengambil gambar tambahan sebagai base64 string
     * @param pictureId ID gambar
     * @param token Token autentikasi
     * @return Response dengan Base64ImageResponse yang berisi data gambar
     */
    @GET("api/images/base64/pictures/{id}")
    suspend fun getBase64PictureImage(
        @Path("id") pictureId: Int,
        @Header("Authorization") token: String
    ): Response<Base64ImageResponse>
}

/**
 * Model response untuk Base64 image
 */
data class Base64ImageResponse(
    val imageData: String
)