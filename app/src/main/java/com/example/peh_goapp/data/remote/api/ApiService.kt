package com.example.peh_goapp.data.remote.api

import com.example.peh_goapp.data.remote.dto.LoginRequest
import com.example.peh_goapp.data.remote.dto.LoginResponse
import com.example.peh_goapp.data.remote.dto.RegisterRequest
import com.example.peh_goapp.data.remote.dto.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

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
}