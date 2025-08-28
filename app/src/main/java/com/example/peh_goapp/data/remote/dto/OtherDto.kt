package com.example.peh_goapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO untuk response API Other
 */
data class OtherResponse(
    val status: Boolean,
    val message: String,
    val data: List<OtherDto>? = null
)

data class OtherDetailResponse(
    val status: Boolean,
    val message: String,
    val data: OtherDto? = null
)

data class OtherDto(
    val id: Int,
    val name: String,
    val category: String,
    val story: String,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

// MISSING CLASSES - ADD THESE
data class DeleteOtherResponse(
    val status: Boolean,
    val message: String
)

data class OtherCategoryResponse(
    val status: Boolean,
    val message: String,
    val data: List<OtherCategoryDto>? = null
)

data class OtherCategoryDto(
    val id: Int,
    val name: String
)

// FOR GENERAL USE - Add to main DTO file or create separate
data class DeleteResponse(
    val status: Boolean,
    val message: String
)