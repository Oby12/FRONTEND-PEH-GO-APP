package com.example.peh_goapp.data.model

data class DestinationDetailModel(
    val id: Int,
    val name: String,
    val address: String,
    val description: String,
    val urlLocation: String,
    val coverUrl: String,
    val categoryName: String,
    val pictures: List<PictureModel>
)

data class PictureModel(
    val id: Int,
    val imageUrl: String
)