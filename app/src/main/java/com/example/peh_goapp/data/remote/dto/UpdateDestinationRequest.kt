package com.example.peh_goapp.data.remote.dto

// UpdateDestinationRequest.kt - Update untuk request
data class UpdateDestinationRequest(
    val name: String,
    val address: String,
    val description: String,
    val urlLocation: String,
    val youtubeUrl: String? = null // Field baru
)