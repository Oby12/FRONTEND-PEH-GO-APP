package com.example.peh_goapp.data.remote.api

import com.example.peh_goapp.data.remote.dto.OtherCategoryResponse
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
        @Header("Authorization") token: String,
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
        @Header("Authorization") token: String
    ): Response<DestinationDetailResponse>

    /**
     * Endpoint untuk menambahkan destinasi baru dengan YouTube URL
     * @param categoryId ID kategori
     * @param token Token autentikasi (tanpa prefix Bearer)
     * @param name Nama destinasi
     * @param address Alamat destinasi
     * @param description Deskripsi destinasi
     * @param urlLocation URL Google Maps
     * @param youtubeUrl URL Video YouTube (opsional)
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
        @Part("youtubeUrl") youtubeUrl: RequestBody? = null, // Field baru untuk YouTube URL (opsional)
        @Part cover: MultipartBody.Part,
        @Part picture: List<MultipartBody.Part>? = null
    ): Response<DestinationDetailResponse>

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
    ): Response<Map<String, String>>

    /**
     * Endpoint untuk mengupdate destinasi dengan YouTube URL
     * @param categoryId ID kategori
     * @param destinationId ID destinasi
     * @param token Token autentikasi
     * @param name Nama destinasi
     * @param address Alamat destinasi
     * @param description Deskripsi destinasi
     * @param urlLocation URL Google Maps
     * @param youtubeUrl URL Video YouTube (opsional)
     * @param removedPictureIds ID gambar yang akan dihapus (opsional)
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
        @Part("youtubeUrl") youtubeUrl: RequestBody? = null, // Field baru untuk YouTube URL (opsional)
        @Part("removedPictureIds") removedPictureIds: RequestBody? = null,
        @Part cover: MultipartBody.Part? = null,
        @Part picture: List<MultipartBody.Part>? = null
    ): Response<DestinationDetailResponse>

    /**
     * Endpoint untuk mendapatkan statistik (admin only)
     */
    @GET("admin/stats")
    suspend fun getStats(
        @Header("Authorization") token: String
    ): Response<StatsResponse>

    /**
     * Endpoint untuk mencatat view destinasi
     */
    @POST("destinations/{destinationId}/view")
    suspend fun recordDestinationView(
        @Path("destinationId") destinationId: Int,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    /**
     * Mendapatkan daftar destinasi favorit pengguna
     */
    @GET("users/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): Response<FavoritesResponse>

    /**
     * Toggle status favorit untuk sebuah destinasi
     */
    @POST("destinations/{destinationId}/favorite")
    suspend fun toggleFavorite(
        @Path("destinationId") destinationId: Int,
        @Header("Authorization") token: String
    ): Response<FavoriteToggleResponse>

    /**
     * Memeriksa apakah destinasi adalah favorit
     */
    @GET("destinations/{destinationId}/favorite")
    suspend fun checkIsFavorite(
        @Path("destinationId") destinationId: Int,
        @Header("Authorization") token: String
    ): Response<FavoriteStatusResponse>

    /**
     * Mendapatkan semua data other
     */
    @GET("others")
    suspend fun getOthers(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<OtherResponse>

    /**
     * Mendapatkan detail other berdasarkan ID
     */
    @GET("others/{id}")
    suspend fun getOtherDetail(
        @Path("id") otherId: Int,
        @Header("Authorization") token: String
    ): Response<OtherDetailResponse>

    /**
     * FITUR OTHER - Admin Only Endpoints
     */
    @Multipart
    @POST("admin/others")
    suspend fun createOther(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("category") category: RequestBody,
        @Part("story") story: RequestBody,
        @Part cover: MultipartBody.Part
    ): Response<OtherDetailResponse>

    @GET("others")
    suspend fun getAllOthers(
        @Header("Authorization") token: String
    ): Response<OtherResponse>

    @GET("others/{otherId}")
    suspend fun getOtherById(
        @Header("Authorization") token: String,
        @Path("otherId") otherId: Int
    ): Response<OtherDetailResponse>

    @Multipart
    @PUT("admin/others/{otherId}")
    suspend fun updateOther(
        @Header("Authorization") token: String,
        @Path("otherId") otherId: Int,
        @Part("name") name: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("story") story: RequestBody?,
        @Part cover: MultipartBody.Part?
    ): Response<OtherDetailResponse>

    @DELETE("admin/others/{otherId}")
    suspend fun deleteOther(
        @Header("Authorization") token: String,
        @Path("otherId") otherId: Int
    ): Response<DeleteOtherResponse>
}