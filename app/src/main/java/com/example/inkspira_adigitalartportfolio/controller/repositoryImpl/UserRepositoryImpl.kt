package com.example.inkspira_adigitalartportfolio.controller.repositoryImpl

import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseRealtimeService
import com.example.inkspira_adigitalartportfolio.controller.repository.UserRepository
import com.example.inkspira_adigitalartportfolio.model.data.UserModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

class UserRepositoryImpl(
    private val dbService: FirebaseRealtimeService
) : UserRepository {

    private val usersNode = "users"

    override suspend fun createUser(user: UserModel): NetworkResult<UserModel> {
        return when (val result = dbService.saveData(usersNode, user.userId, user)) {
            is NetworkResult.Success -> NetworkResult.Success(user)
            is NetworkResult.Error -> NetworkResult.Error(result.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getUserById(userId: String): NetworkResult<UserModel?> {
        return dbService.getData(usersNode, userId, UserModel::class.java)
    }

    override suspend fun updateUser(user: UserModel): NetworkResult<UserModel> {
        // Convert UserModel to Map for Firebase update
        val userMap = mapOf(
            "userId" to user.userId,
            "email" to user.email,
            "displayName" to user.displayName,
            "role" to user.role.name,
            "profileImageUrl" to user.profileImageUrl,
            "createdAt" to user.createdAt
        )

        return when (val result = dbService.updateData(usersNode, user.userId, userMap)) {
            is NetworkResult.Success -> NetworkResult.Success(user)
            is NetworkResult.Error -> NetworkResult.Error(result.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun deleteUser(userId: String): NetworkResult<Boolean> {
        return dbService.deleteData(usersNode, userId)
    }

    override suspend fun updateUserProfile(
        userId: String,
        displayName: String,
        profileImageUrl: String
    ): NetworkResult<UserModel> {
        val updates = mapOf(
            "displayName" to displayName,
            "profileImageUrl" to profileImageUrl
        )

        return when (val updateResult = dbService.updateData(usersNode, userId, updates)) {
            is NetworkResult.Success -> {
                // Fetch updated user data
                when (val userResult = getUserById(userId)) {
                    is NetworkResult.Success -> {
                        if (userResult.data != null) {
                            NetworkResult.Success(userResult.data)
                        } else {
                            NetworkResult.Error("User not found after update")
                        }
                    }
                    is NetworkResult.Error -> NetworkResult.Error(userResult.message)
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
            is NetworkResult.Error -> NetworkResult.Error(updateResult.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun updateUserRole(userId: String, newRole: String): NetworkResult<UserModel> {
        val updates = mapOf("role" to newRole)

        return when (val updateResult = dbService.updateData(usersNode, userId, updates)) {
            is NetworkResult.Success -> {
                // Fetch updated user data
                when (val userResult = getUserById(userId)) {
                    is NetworkResult.Success -> {
                        if (userResult.data != null) {
                            NetworkResult.Success(userResult.data)
                        } else {
                            NetworkResult.Error("User not found after role update")
                        }
                    }
                    is NetworkResult.Error -> NetworkResult.Error(userResult.message)
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
            is NetworkResult.Error -> NetworkResult.Error(updateResult.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getUsersByRole(role: String): NetworkResult<List<UserModel>> {
        return dbService.queryData(usersNode, "role", role, UserModel::class.java)
    }
}
