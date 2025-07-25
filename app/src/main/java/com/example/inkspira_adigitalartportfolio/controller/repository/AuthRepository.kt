package com.example.inkspira_adigitalartportfolio.controller.repository



import UserModel
import com.example.inkspira_adigitalartportfolio.model.response.AuthResponse
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

interface AuthRepository {

    // User authentication operations
    suspend fun loginUser(email: String, password: String): NetworkResult<UserModel>

    suspend fun registerUser(email: String, password: String, displayName: String, role: String): NetworkResult<UserModel>

    suspend fun logoutUser(): NetworkResult<Boolean>

    // Session management
    fun getCurrentUserId(): String?

    fun isUserLoggedIn(): Boolean

    // Password management
    suspend fun sendPasswordResetEmail(email: String): NetworkResult<Boolean>
}
