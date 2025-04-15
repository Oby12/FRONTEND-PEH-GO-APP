// Perubahan pada ApiModels.kt
package com.example.peh_goapp.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val data: LoginData? = null,
    val errors: String? = null
)

data class LoginData(
    val token: String,
    val role: String? = null  // Tambahkan field role dengan default null
)

data class ErrorResponse(
    val errors: String
)

data class RegisterRequest(
    //val role: String,
    val username: String,
    val name: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val data: UserData? = null,
    val error: ErrorData? = null
)

data class UserData(
    val role: String,
    val name: String,
    val username: String,
    val email: String
)

data class ErrorData(
    val message: String
)