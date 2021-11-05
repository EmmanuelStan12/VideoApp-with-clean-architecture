package com.codedev.videoapplication.domain.utils

sealed class Resource<T>(
    val data: T? = null,
    val exception: CustomException? = null
) {
    class Success<T>(data: T?, exception: CustomException? = null): Resource<T>(data, exception)
    class Error<T>(data: T? = null, exception: CustomException?): Resource<T>(data, exception)
    class Loading<T>(data: T? = null, exception: CustomException?): Resource<T>(data, exception)
}

data class CustomException(
    val error: String = "",
    val isError: Boolean = false
)