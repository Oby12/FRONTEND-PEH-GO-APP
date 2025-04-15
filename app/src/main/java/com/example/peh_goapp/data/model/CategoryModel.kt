package com.example.peh_goapp.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryModel(
    val id: Int,
    val name: String,
    val icon: ImageVector? = null,
    val iconResId: Int? = null
)