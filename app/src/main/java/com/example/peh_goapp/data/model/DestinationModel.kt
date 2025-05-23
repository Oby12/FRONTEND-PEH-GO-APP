package com.example.peh_goapp.data.model

data class DestinationModel(
    val id: Int,
    val name: String,
    val address: String,  // Kita tidak mengubah signature-nya, tapi membuat nilai default di repository
    val description: String,
    val urlLocation: String,
    val coverUrl: String,
    val pictures: List<PictureModel> = emptyList(),
    val isFavorite: Boolean = false
)