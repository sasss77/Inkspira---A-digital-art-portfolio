package com.example.inkspira_adigitalartportfolio.controller.repositoryImpl

import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseAuthService
import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseRealtimeService
import com.example.inkspira_adigitalartportfolio.controller.repository.AuthRepository
import com.example.inkspira_adigitalartportfolio.model.data.UserModel
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

class AuthRepositoryImpl(
    private val authService: FirebaseAuthService,
    private val dbService: FirebaseRealtimeService
) : AuthRepository {

    private val usersNode = "users" // Using direct string until Constants is updated

    override suspend fun loginUser(
        email: String,
        password: String
    ): NetworkResult<UserModel> {
        return when (val authResult = authService.loginUser(email, password)) {
            is NetworkResult.Success -> {
                val uid = authResult.data.uid
                when (val userResult = dbService.getData(usersNode, uid, UserModel::class.java)) {
                    is NetworkResult.Success -> {
                        if (userResult.data != null) {
                            NetworkResult.Success(userResult.data)
                        } else {
                            NetworkResult.Error("User data not found")
                        }
                    }
                    is NetworkResult.Error -> NetworkResult.Error(userResult.message)
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
            is NetworkResult.Error -> NetworkResult.Error(authResult.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
        role: String
    ): NetworkResult<UserModel> {
        return when (val authResult = authService.registerUser(email, password)) {
            is NetworkResult.Success -> {
                val uid = authResult.data.uid
                val userRole = when (role.uppercase()) {
                    "ARTIST" -> UserRole.ARTIST
                    "VIEWER" -> UserRole.VIEWER
                    "BOTH" -> UserRole.BOTH
                    else -> UserRole.VIEWER
                }

                val newUser = UserModel(
                    userId = uid,
                    email = email,
                    displayName = displayName,
                    role = userRole,
                    createdAt = System.currentTimeMillis()
                )

                when (val saveResult = dbService.saveData(usersNode, uid, newUser)) {
                    is NetworkResult.Success -> NetworkResult.Success(newUser)
                    is NetworkResult.Error -> NetworkResult.Error(saveResult.message)
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
            is NetworkResult.Error -> NetworkResult.Error(authResult.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun logoutUser(): NetworkResult<Boolean> {
        return authService.logoutUser()
    }

    override fun getCurrentUserId(): String? {
        return authService.getCurrentUserId()
    }

    override fun isUserLoggedIn(): Boolean {
        return authService.isUserLoggedIn()
    }

    override suspend fun sendPasswordResetEmail(email: String): NetworkResult<Boolean> {
        return authService.sendPasswordResetEmail(email)
    }
}
