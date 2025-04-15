package com.example.peh_goapp.data.remote.dto

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class AddDestinationRequest(
    val name: RequestBody,
    val address: RequestBody,
    val description: RequestBody,
    val urlLocation: RequestBody,
    val cover: MultipartBody.Part,
    val pictures: List<MultipartBody.Part>? = null
)