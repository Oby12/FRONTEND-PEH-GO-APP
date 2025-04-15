package com.example.peh_goapp.data.remote.dto

data class DestinationsResponse(
    val data: List<DestinationDto>,
    val pagination: PaginationDto
)

data class DestinationDto(
    val id: Int,
    val name: String,
    val address: String,
    val description: String,
    val urlLocation: String,
    val coverUrl: String
)

data class PaginationDto(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class DestinationDetailResponse(
    val data: DestinationDetailDto
)

data class DestinationDetailDto(
    val id: Int,
    val name: String,
    val address: String,
    val description: String,
    val urlLocation: String,
    val coverUrl: String,
    val Category: CategoryDto,
    val picture: List<PictureDto>
)

data class CategoryDto(
    val name: String
)

data class PictureDto(
    val id: Int,
    val imageUrl: String
)