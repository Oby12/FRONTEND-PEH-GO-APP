package com.example.peh_goapp.data.remote.api

import com.example.peh_goapp.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface OtherApiService {

    @GET("others")
    suspend fun getAllOthers(
        @Header("Authorization") token: String
    ): OtherResponse

    @GET("others/{otherId}")
    suspend fun getOtherById(
        @Header("Authorization") token: String,
        @Path("otherId") otherId: Int
    ): OtherDetailResponse

    @Multipart
    @POST("admin/others")
    suspend fun createOther(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("category") category: RequestBody,
        @Part("story") story: RequestBody,
        @Part cover: MultipartBody.Part
    ): OtherDetailResponse

    @Multipart
    @PUT("admin/others/{otherId}")
    suspend fun updateOther(
        @Header("Authorization") token: String,
        @Path("otherId") otherId: Int,
        @Part("name") name: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("story") story: RequestBody?,
        @Part cover: MultipartBody.Part?
    ): OtherDetailResponse

    @DELETE("admin/others/{otherId}")
    suspend fun deleteOther(
        @Header("Authorization") token: String,
        @Path("otherId") otherId: Int
    ): DeleteOtherResponse
}