package com.example.inkspira_adigitalartportfolio.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspira_adigitalartportfolio.controller.repository.AuthRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.UserRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.CloudinaryRepository
import com.example.inkspira_adigitalartportfolio.model.data.UserModel
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {

    // Current user profile
    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    // Profile update state
    private val _updateState = MutableStateFlow<NetworkResult<UserModel>>(NetworkResult.Loading(false))
    val updateState: StateFlow<NetworkResult<UserModel>> = _updateState.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUserProfile()
    }

    // Load current user profile
    fun loadUserProfile() {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = userRepository.getUserById(currentUserId)) {
                is NetworkResult.Success -> {
                    _userProfile.value = result.data
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Update user profile
    fun updateProfile(displayName: String, profileImageUrl: String? = null) {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _errorMessage.value = "User not logged in"
            return
        }

        // Validate display name
        if (!ValidationUtils.isValidDisplayName(displayName)) {
            _errorMessage.value = "Display name must be 2-30 characters long"
            return
        }

        viewModelScope.launch {
            _isUpdatingProfile.value = true
            _errorMessage.value = null

            val imageUrl = profileImageUrl ?: _userProfile.value?.profileImageUrl ?: ""

            when (val result = userRepository.updateUserProfile(currentUserId, displayName.trim(), imageUrl)) {
                is NetworkResult.Success -> {
                    _userProfile.value = result.data
                    _updateState.value = NetworkResult.Success(result.data)
                    _successMessage.value = "Profile updated successfully!"
                    _isUpdatingProfile.value = false
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _updateState.value = NetworkResult.Error(result.message)
                    _isUpdatingProfile.value = false
                }
                is NetworkResult.Loading -> {
                    _isUpdatingProfile.value = true
                }
            }
        }
    }

    // Update profile image
    fun updateProfileImage(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            _isUploadingImage.value = true
            _errorMessage.value = null

            // Upload image to Cloudinary
            when (val imageResult = cloudinaryRepository.uploadImage(imageUri, context)) {
                is NetworkResult.Success -> {
                    // Update profile with new image URL
                    val currentProfile = _userProfile.value
                    if (currentProfile != null) {
                        updateProfile(currentProfile.displayName, imageResult.data)
                    }
                    _isUploadingImage.value = false
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = imageResult.message
                    _isUploadingImage.value = false
                }
                is NetworkResult.Loading -> {
                    _isUploadingImage.value = true
                }
            }
        }
    }

    // Update user role
    fun updateUserRole(newRole: UserRole) {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isUpdatingProfile.value = true
            _errorMessage.value = null

            when (val result = userRepository.updateUserRole(currentUserId, newRole.name)) {
                is NetworkResult.Success -> {
                    _userProfile.value = result.data
                    _successMessage.value = "Role updated successfully!"
                    _isUpdatingProfile.value = false
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _isUpdatingProfile.value = false
                }
                is NetworkResult.Loading -> {
                    _isUpdatingProfile.value = true
                }
            }
        }
    }

    // Get profile image URL (optimized)
    fun getProfileImageUrl(size: Int = 200): String {
        val imageUrl = _userProfile.value?.profileImageUrl
        return if (imageUrl.isNullOrBlank()) {
            "" // Return empty string for default avatar handling
        } else {
            cloudinaryRepository.getOptimizedImageUrl(imageUrl, size, size)
        }
    }

    // Check if profile is complete
    fun isProfileComplete(): Boolean {
        val profile = _userProfile.value
        return profile != null &&
                profile.displayName.isNotBlank() &&
                profile.email.isNotBlank()
    }

    // Get user statistics (placeholder for future implementation)
    fun getUserStats(): Map<String, Int> {
        return mapOf(
            "artworks" to 0, // Could be loaded from ArtworkRepository
            "favorites" to 0, // Could be loaded from FavoriteRepository
            "views" to 0      // Future feature
        )
    }

    // Validate profile data
    fun validateProfileData(displayName: String): Boolean {
        return ValidationUtils.isValidDisplayName(displayName)
    }

    // Get current user display name
    fun getCurrentDisplayName(): String {
        return _userProfile.value?.displayName ?: ""
    }

    // Get current user email
    fun getCurrentEmail(): String {
        return _userProfile.value?.email ?: ""
    }

    // Get current user role
    fun getCurrentRole(): UserRole {
        return _userProfile.value?.role ?: UserRole.VIEWER
    }

    // Get role display name
    fun getRoleDisplayName(): String {
        return _userProfile.value?.getRoleDisplayName() ?: "Unknown"
    }

    // Check if user can create artwork
    fun canCreateArtwork(): Boolean {
        return _userProfile.value?.canCreateArtwork() ?: false
    }

    // Clear error message
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Clear success message
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    // Refresh profile data
    fun refreshProfile() {
        loadUserProfile()
    }
}
