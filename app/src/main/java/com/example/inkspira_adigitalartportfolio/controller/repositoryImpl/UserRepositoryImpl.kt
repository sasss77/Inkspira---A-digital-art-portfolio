package com.example.inkspira_adigitalartportfolio.controller.repositoryImpl

import UserModel
import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseRealtimeService
import com.example.inkspira_adigitalartportfolio.controller.repository.UserRepository

import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

class UserRepositoryImpl(
    private val dbService: FirebaseRealtimeService
) : UserRepository {

    private val usersNode = "users"

    override suspend fun createUser(user: UserModel): NetworkResult<UserModel> {
        // ✅ Validate user data before creation
        if (user.userId.isBlank() || user.email.isBlank()) {
            return NetworkResult.Error("User ID and email cannot be empty")
        }

        return when (val result = dbService.saveData(usersNode, user.userId, user.toMap())) {
            is NetworkResult.Success -> NetworkResult.Success(user)
            is NetworkResult.Error -> NetworkResult.Error("Failed to create user: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getUserById(userId: String): NetworkResult<UserModel?> {
        // ✅ Validate input parameter
        if (userId.isBlank()) {
            return NetworkResult.Error("User ID cannot be empty")
        }

        return when (val result = dbService.getData(usersNode, userId, UserModel::class.java)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Error -> NetworkResult.Error("Failed to get user: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun updateUser(user: UserModel): NetworkResult<UserModel> {
        // ✅ Validate user data
        if (user.userId.isBlank()) {
            return NetworkResult.Error("User ID cannot be empty")
        }

        return when (val result = dbService.updateData(usersNode, user.userId, user.toMap())) {
            is NetworkResult.Success -> NetworkResult.Success(user)
            is NetworkResult.Error -> NetworkResult.Error("Failed to update user: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun deleteUser(userId: String): NetworkResult<Boolean> {
        // ✅ Validate input parameter
        if (userId.isBlank()) {
            return NetworkResult.Error("User ID cannot be empty")
        }

        return when (val result = dbService.deleteData(usersNode, userId)) {
            is NetworkResult.Success -> NetworkResult.Success(true)
            is NetworkResult.Error -> NetworkResult.Error("Failed to delete user: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun updateUserProfile(
        userId: String,
        displayName: String,
        profileImageUrl: String
    ): NetworkResult<UserModel> {
        // ✅ Validate input parameters
        if (userId.isBlank()) {
            return NetworkResult.Error("User ID cannot be empty")
        }

        if (displayName.isBlank()) {
            return NetworkResult.Error("Display name cannot be empty")
        }

        val updates = mapOf(
            "displayName" to displayName,
            "profileImageUrl" to profileImageUrl
        )

        return when (val updateResult = dbService.updateData(usersNode, userId, updates)) {
            is NetworkResult.Success -> {
                // ✅ Retrieve updated user data
                when (val userResult = getUserById(userId)) {
                    is NetworkResult.Success -> {
                        userResult.data?.let { user ->
                            NetworkResult.Success(user)
                        } ?: NetworkResult.Error("User not found after update")
                    }
                    is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve updated user: ${userResult.message}")
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to update user profile: ${updateResult.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun updateUserRole(userId: String, newRole: String): NetworkResult<UserModel> {
        // ✅ Validate input parameters
        if (userId.isBlank()) {
            return NetworkResult.Error("User ID cannot be empty")
        }

        if (newRole.isBlank()) {
            return NetworkResult.Error("Role cannot be empty")
        }

        // ✅ Validate role value
        try {
            UserRole.valueOf(newRole.uppercase())
        } catch (e: IllegalArgumentException) {
            return NetworkResult.Error("Invalid role: $newRole")
        }

        return when (val updateResult = dbService.updateData(usersNode, userId, mapOf("role" to newRole))) {
            is NetworkResult.Success -> {
                // ✅ Retrieve updated user data
                when (val userResult = getUserById(userId)) {
                    is NetworkResult.Success -> {
                        userResult.data?.let { user ->
                            NetworkResult.Success(user)
                        } ?: NetworkResult.Error("User not found after role update")
                    }
                    is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve updated user: ${userResult.message}")
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to update user role: ${updateResult.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getUsersByRole(role: String): NetworkResult<List<UserModel>> {
        // ✅ Validate input parameter
        if (role.isBlank()) {
            return NetworkResult.Error("Role cannot be empty")
        }

        // ✅ Validate role value
        try {
            UserRole.valueOf(role.uppercase())
        } catch (e: IllegalArgumentException) {
            return NetworkResult.Error("Invalid role: $role")
        }

        return when (val result = dbService.queryData(usersNode, "role", role, UserModel::class.java)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data ?: emptyList())
            is NetworkResult.Error -> NetworkResult.Error("Failed to get users by role: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }
}
