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
    val role: String? = null,
    val user: UserInfo? = null  // Tambahkan field untuk info user dari server
)

data class UserInfo(
    val username: String,
    val name: String,
    val email: String
)

/**
 * Model untuk respons error dari API
 * Format sesuai dengan respons API:
 * {
 *   "status": false,
 *   "message": "Email or password is wrong",
 *   "errors": null
 * }
 */
data class ErrorResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val errors: String? = null  // Ubah dari String ke String? (nullable)
)

data class RegisterRequest(
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