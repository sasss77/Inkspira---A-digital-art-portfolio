package com.example.inkspira_adigitalartportfolio.utils

sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : NetworkResult<T>(data)

    class Error<T>(message: String, data: T? = null) : NetworkResult<T>(data, message)

    class Loading<T>(private val isCurrentlyLoading: Boolean = true) : NetworkResult<T>() {
        // Use a method with different name to avoid conflict
        fun isLoading(): Boolean = isCurrentlyLoading
    }
}
