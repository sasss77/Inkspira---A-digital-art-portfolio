package com.example.inkspira_adigitalartportfolio.controller.repositoryImpl

import UserModel
import com.google.firebase.auth.FirebaseAuth
import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseRealtimeService
import com.example.inkspira_adigitalartportfolio.controller.repository.AuthRepository

import com.example.inkspira_adigitalartportfolio.model.data.UserRole

import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val dbService: FirebaseRealtimeService
) : AuthRepository {

    private val usersNode = "users"

    override suspend fun loginUser(email: String, password: String): NetworkResult<UserModel> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()

            // ✅ Safe null handling with let function
            authResult.user?.let { firebaseUser ->
                val uid = firebaseUser.uid

                when (val userResult = dbService.getData(usersNode, uid, UserModel::class.java)) {
                    is NetworkResult.Success -> {
                        userResult.data?.let { user ->
                            NetworkResult.Success(user)
                        } ?: NetworkResult.Error("User profile not found")
                    }
                    is NetworkResult.Error -> NetworkResult.Error("Failed to load user profile: ${userResult.message}")
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            } ?: NetworkResult.Error("Authentication failed - invalid credentials")

        } catch (e: Exception) {
            NetworkResult.Error("Login failed: ${e.localizedMessage}")
        }
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
        role: String
    ): NetworkResult<UserModel> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            // ✅ Safe null handling
            authResult.user?.let { firebaseUser ->
                val uid = firebaseUser.uid
                val userRole = UserRole.valueOf(role.uppercase())

                val newUser = UserModel(
                    userId = uid,
                    email = email,
                    displayName = displayName,
                    role = userRole,
                    profileImageUrl = "",
                    createdAt = System.currentTimeMillis()
                )

                when (val saveResult = dbService.saveData(usersNode, uid, newUser.toMap())) {
                    is NetworkResult.Success -> NetworkResult.Success(newUser)
                    is NetworkResult.Error -> {
                        // Clean up Firebase user if database save fails
                        firebaseUser.delete()
                        NetworkResult.Error("Failed to create user profile: ${saveResult.message}")
                    }
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            } ?: NetworkResult.Error("Failed to create user account")

        } catch (e: Exception) {
            NetworkResult.Error("Registration failed: ${e.localizedMessage}")
        }
    }

    override suspend fun logoutUser(): NetworkResult<Boolean> {
        return try {
            firebaseAuth.signOut()
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error("Logout failed: ${e.localizedMessage}")
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid  // ✅ Safe call on nullable user
    }

    override suspend fun sendPasswordResetEmail(email: String): NetworkResult<Boolean> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error("Password reset failed: ${e.localizedMessage}")
        }
    }
}
