package com.example.inkspira_adigitalartportfolio.controller.remote


import com.example.inkspira_adigitalartportfolio.model.response.AuthResponse
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Get current authenticated user
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // Register new user with email and password
    suspend fun registerUser(email: String, password: String): NetworkResult<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                NetworkResult.Success(user)
            } else {
                NetworkResult.Error("Failed to create user account")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Registration failed: ${e.message}")
        }
    }

    // Login user with email and password
    suspend fun loginUser(email: String, password: String): NetworkResult<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                NetworkResult.Success(user)
            } else {
                NetworkResult.Error("Login failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Login failed: ${e.message}")
        }
    }

    // Logout current user
    suspend fun logoutUser(): NetworkResult<Boolean> {
        return try {
            firebaseAuth.signOut()
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error("Logout failed: ${e.message}")
        }
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): NetworkResult<Boolean> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to send reset email: ${e.message}")
        }
    }

    // Update user email
    suspend fun updateUserEmail(newEmail: String): NetworkResult<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                currentUser.updateEmail(newEmail).await()
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Failed to update email: ${e.message}")
        }
    }

    // Update user password
    suspend fun updateUserPassword(newPassword: String): NetworkResult<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                currentUser.updatePassword(newPassword).await()
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Failed to update password: ${e.message}")
        }
    }

    // Delete user account
    suspend fun deleteUserAccount(): NetworkResult<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                currentUser.delete().await()
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Failed to delete account: ${e.message}")
        }
    }

    // Refresh user token
    suspend fun refreshUserToken(): NetworkResult<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val tokenResult = currentUser.getIdToken(true).await()
                val token = tokenResult.token
                if (token != null) {
                    NetworkResult.Success(token)
                } else {
                    NetworkResult.Error("Failed to get token")
                }
            } else {
                NetworkResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Failed to refresh token: ${e.message}")
        }
    }

    // Validate current user session
    fun validateUserSession(): Boolean {
        val currentUser = firebaseAuth.currentUser
        return currentUser != null && !currentUser.isAnonymous
    }
}
