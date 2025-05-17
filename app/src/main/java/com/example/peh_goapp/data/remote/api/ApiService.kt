package com.example.peh_goapp.data.remote.api

import com.example.peh_goapp.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("users/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("users/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @DELETE("users/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    /**
     * Endpoint untuk mendapatkan daftar destinasi berdasarkan kategori
     * @param categoryId ID kategori
     * @param token Token autentikasi (tanpa prefix Bearer)
     * @param page Nomor halaman (default: 1)
     * @param limit Jumlah item per halaman (default: 10)
     */
    @GET("users/{categoryId}/destinations")
    suspend fun getDestinations(
        @Path("categoryId") categoryId: Int,
        @Header("Authorization") token: String, // Mengirim token tanpa "Bearer "
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<DestinationsResponse>

    /**
     * Endpoint untuk mendapatkan detail destinasi
     * @param categoryId ID kategori
     * @param destinationId ID destinasi
     * @param token Token autentikasi (tanpa prefix Bearer)
     */
    @GET("users/{categoryId}/destinations/{destinationId}")
    suspend fun getDestinationDetail(
        @Path("categoryId") categoryId: Int,
        @Path("destinationId") destinationId: Int,
        @Header("Authorization") token: String // Mengirim token tanpa "Bearer "
    ): Response<DestinationDetailResponse>

    /**
     * Endpoint untuk menambahkan destinasi baru
     * @param categoryId ID kategori
     * @param token Token autentikasi (tanpa prefix Bearer)
     * @param name Nama destinasi
     * @param address Alamat destinasi
     * @param description Deskripsi destinasi
     * @param urlLocation URL Google Maps
     * @param cover Gambar cover (wajib)
     * @param picture Daftar gambar tambahan (opsional, maksimal 3)
     */
    @Multipart
    @POST("users/{categoryId}/destinations")
    suspend fun addDestination(
        @Path("categoryId") categoryId: Int,
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("address") address: RequestBody,
        @Part("description") description: RequestBody,
        @Part("urlLocation") urlLocation: RequestBody,
        @Part cover: MultipartBody.Part,
        @Part picture: List<MultipartBody.Part>?
    ): Response<DestinationDetailResponse>

    // Tambahkan fungsi ini ke dalam ApiService.kt

    /**
     * Endpoint untuk menghapus destinasi
     * @param categoryId ID kategori
     * @param destinationId ID destinasi
     * @param token Token autentikasi
     */
    @DELETE("users/{categoryId}/destinations/{destinationId}")
    suspend fun deleteDestination(
        @Path("categoryId") categoryId: Int,
        @Path("destinationId") destinationId: Int,
        @Header("Authorization") token: String
    ): Response<Map<String, String>> // Response: { "message": "Destinasi berhasil dihapus" }


    /**
     * Endpoint untuk mengupdate destinasi yang sudah ada
     * @param categoryId ID kategori
     * @param destinationId ID destinasi
     * @param token Token autentikasi
     * @param name Nama destinasi
     * @param address Alamat destinasi
     * @param description Deskripsi destinasi
     * @param urlLocation URL Google Maps
     * @param cover Gambar cover (opsional)
     * @param picture Daftar gambar tambahan (opsional)
     */
    @Multipart
    @PUT("users/{categoryId}/destinations/{destinationId}")
    suspend fun updateDestination(
        @Path("categoryId") categoryId: Int,
        @Path("destinationId") destinationId: Int,
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("address") address: RequestBody,
        @Part("description") description: RequestBody,
        @Part("urlLocation") urlLocation: RequestBody,
        @Part cover: MultipartBody.Part?,
        @Part picture: List<MultipartBody.Part>?
    ): Response<DestinationDetailResponse>

    /**
     * Endpoint untuk mengupdate destinasi dengan daftar ID gambar yang dihapus
     * @param categoryId ID kategori
     * @param destinationId ID destinasi
     * @param token Token autentikasi
     * @param name Nama destinasi
     * @param address Alamat destinasi
     * @param description Deskripsi destinasi
     * @param urlLocation URL Google Maps
     * @param removedPictureIds ID gambar yang akan dihapus (string dengan format daftar ID terpisah koma)
     * @param cover Gambar cover (opsional)
     * @param picture Daftar gambar tambahan (opsional)
     */
    @Multipart
    @PUT("users/{categoryId}/destinations/{destinationId}")
    suspend fun updateDestination(
        @Path("categoryId") categoryId: Int,
        @Path("destinationId") destinationId: Int,
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("address") address: RequestBody,
        @Part("description") description: RequestBody,
        @Part("urlLocation") urlLocation: RequestBody,
        @Part("removedPictureIds") removedPictureIds: RequestBody? = null,
        @Part cover: MultipartBody.Part? = null,
        @Part picture: List<MultipartBody.Part>? = null
    ): Response<DestinationDetailResponse>

    @GET("admin/stats")
    suspend fun getStats(
        @Header("Authorization") token: String
    ): Response<StatsResponse>

    @POST("destinations/{destinationId}/view")
    suspend fun recordDestinationView(
        @Path("destinationId") destinationId: Int,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>
}