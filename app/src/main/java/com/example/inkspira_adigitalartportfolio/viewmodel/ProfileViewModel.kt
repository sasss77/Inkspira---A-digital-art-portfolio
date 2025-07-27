package com.example.inkspira_adigitalartportfolio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.inkspira_adigitalartportfolio.view.screens.UserProfileData

class ProfileViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val _userProfile = MutableStateFlow<UserProfileData?>(null)
    val userProfile: StateFlow<UserProfileData?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId != null) {
                    val snapshot = database.getReference("users").child(userId).get().await()

                    if (snapshot.exists()) {
                        val profileData = UserProfileData(
                            userId = snapshot.child("userId").value as? String ?: "",
                            displayName = snapshot.child("displayName").value as? String ?: "",
                            username = snapshot.child("username").value as? String ?: "",
                            email = snapshot.child("email").value as? String ?: "",
                            role = snapshot.child("role").value as? String ?: "",
                            bio = snapshot.child("bio").value as? String ?: "",
                            profileImageUrl = snapshot.child("profileImageUrl").value as? String ?: "",
                            websiteUrl = snapshot.child("websiteUrl").value as? String ?: "",
                            artworkCount = (snapshot.child("artworkCount").value as? Long)?.toInt() ?: 0,
                            followersCount = (snapshot.child("followersCount").value as? Long)?.toInt() ?: 0,
                            followingCount = (snapshot.child("followingCount").value as? Long)?.toInt() ?: 0,
                            createdAt = snapshot.child("createdAt").value as? Long ?: 0L,
                            isActive = snapshot.child("isActive").value as? Boolean ?: true
                        )

                        _userProfile.value = profileData
                    } else {
                        _errorMessage.value = "Profile not found"
                    }
                } else {
                    _errorMessage.value = "User not logged in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(updatedProfile: UserProfileData) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId != null) {
                    val updates = mapOf(
                        "displayName" to updatedProfile.displayName,
                        "bio" to updatedProfile.bio,
                        "websiteUrl" to updatedProfile.websiteUrl,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    database.getReference("users").child(userId).updateChildren(updates).await()
                    _userProfile.value = updatedProfile
                } else {
                    _errorMessage.value = "User not logged in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
