

package com.example.inkspira_adigitalartportfolio.model.response

import com.example.inkspira_adigitalartportfolio.model.data.UserModel


data class AuthResponse(
    val isSuccess: Boolean = false,
    val user: UserModel? = null,
    val errorMessage: String = "",
    val errorCode: String = ""
) {
    companion object {
        // Success response factory method
        fun success(user: UserModel): AuthResponse {
            return AuthResponse(
                isSuccess = true,
                user = user,
                errorMessage = "",
                errorCode = ""
            )
        }

        // Error response factory method
        fun error(message: String, code: String = ""): AuthResponse {
            return AuthResponse(
                isSuccess = false,
                user = null,
                errorMessage = message,
                errorCode = code
            )
        }

        // Loading state factory method (optional)
        fun loading(): AuthResponse {
            return AuthResponse(
                isSuccess = false,
                user = null,
                errorMessage = "",
                errorCode = "LOADING"
            )
        }
    }

    // Helper method to check if response has error
    fun hasError(): Boolean {
        return !isSuccess && errorMessage.isNotEmpty()
    }

    // Helper method to check if response is loading
    fun isLoading(): Boolean {
        return errorCode == "LOADING"
    }
}
