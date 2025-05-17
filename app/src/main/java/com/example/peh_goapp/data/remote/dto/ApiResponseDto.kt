package com.example.peh_goapp.data.remote.dto

/**
 * Base class untuk semua respons dari API
 */
open class ApiResponseDto

/**
 * DTO untuk respons error dari API
 */
data class ApiErrorResponseDto(
    val status: Boolean? = null,
    val message: String? = null,
    val errors: String? = null
) : ApiResponseDto()