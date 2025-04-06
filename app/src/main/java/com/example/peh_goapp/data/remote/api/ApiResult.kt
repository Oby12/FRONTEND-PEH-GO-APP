package com.example.peh_goapp.data.remote.api

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val errorMessage: String) : ApiResult<Nothing>()
}