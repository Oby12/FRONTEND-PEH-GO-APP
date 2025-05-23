package com.example.peh_goapp.data.remote.dto

data class FavoriteToggleResponse(
    val data: FavoriteStatusDto
)

data class FavoriteStatusResponse(
    val data: FavoriteStatusDto
)

data class FavoriteStatusDto(
    val isFavorite: Boolean
)

data class FavoritesResponse(
    val data: List<DestinationDto>
)