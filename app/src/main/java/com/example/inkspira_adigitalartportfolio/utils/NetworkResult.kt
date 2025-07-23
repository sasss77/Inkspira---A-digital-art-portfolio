package com.example.inkspira_adigitalartportfolio.utils

sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val exception: Throwable? = null) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()

    // Helper methods for easier state checking
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    // Get data safely
    fun getDataOrNull(): T? {
        return if (this is Success) this.data else null
    }

    // Get error message safely
    fun getErrorMessage(): String? {
        return if (this is Error) this.message else null
    }

    companion object {
        // Factory methods for easier creation
        fun <T> success(data: T): NetworkResult<T> = Success(data)
        fun <T> error(message: String, exception: Throwable? = null): NetworkResult<T> = Error(message, exception)
        fun <T> loading(): NetworkResult<T> = Loading()
    }
}

// Extension function for handling common patterns
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message)
    }
    return this
}

inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
}

