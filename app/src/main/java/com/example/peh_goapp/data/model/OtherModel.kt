// File: data/model/OtherModel.kt
package com.example.peh_goapp.data.model

/**
 * Model untuk data Other
 */
data class OtherModel(
    val id: Int,
    val name: String, // Non-nullable
    val category: String, // Non-nullable
    val story: String, // Non-nullable
    val coverUrl: String, // Non-nullable
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Model untuk detail Other
 */
data class OtherDetailModel(
    val id: Int,
    val name: String, // Non-nullable
    val category: String, // Non-nullable
    val story: String, // Non-nullable
    val coverUrl: String, // Non-nullable
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Model untuk category Other (untuk dropdown)
 */
data class OtherCategoryModel(
    val id: Int,
    val name: String
)

