package com.example.inkspira_adigitalartportfolio.controller.repository


import UserModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

interface UserRepository {

    // User CRUD operations
    suspend fun createUser(user: UserModel): NetworkResult<UserModel>

    suspend fun getUserById(userId: String): NetworkResult<UserModel?>

    suspend fun updateUser(user: UserModel): NetworkResult<UserModel>

    suspend fun deleteUser(userId: String): NetworkResult<Boolean>

    // Profile management
    suspend fun updateUserProfile(userId: String, displayName: String, profileImageUrl: String): NetworkResult<UserModel>

    suspend fun updateUserRole(userId: String, newRole: String): NetworkResult<UserModel>

    // User queries
    suspend fun getUsersByRole(role: String): NetworkResult<List<UserModel>>
}
