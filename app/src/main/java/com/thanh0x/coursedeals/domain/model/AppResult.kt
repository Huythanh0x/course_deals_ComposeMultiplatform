package com.thanh0x.coursedeals.domain.model

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val code: Int? = null) : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()
}
