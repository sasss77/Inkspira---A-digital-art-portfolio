package com.example.inkspira_adigitalartportfolio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspira_adigitalartportfolio.controller.repository.AuthRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.UserRepository
import com.example.inkspira_adigitalartportfolio.model.data.UserModel
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainDashboardViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Current user state
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    // Active role for dual-role users
    private val _activeRole = MutableStateFlow<UserRole>(UserRole.VIEWER)
    val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Navigation state
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        loadCurrentUser()
    }

    // Load current user data
    private fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true

            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                when (val result = userRepository.getUserById(userId)) {
                    is NetworkResult.Success -> {
                        result.data?.let { user ->
                            _currentUser.value = user
                            _activeRole.value = user.role
                            _isLoading.value = false
                        }
                    }
                    is NetworkResult.Error -> {
                        _errorMessage.value = result.message
                        _isLoading.value = false
                    }
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }
                }
            } else {
                _errorMessage.value = "User not logged in"
                _navigationEvent.value = NavigationEvent.NavigateToLogin
                _isLoading.value = false
            }
        }
    }

    // Switch active role (for users with BOTH role)
    fun switchRole(newRole: UserRole) {
        val currentUser = _currentUser.value
        if (currentUser?.role == UserRole.BOTH) {
            _activeRole.value = newRole
        }
    }

    // Check if user can switch roles
    fun canSwitchRoles(): Boolean {
        return _currentUser.value?.role == UserRole.BOTH
    }

    // Get available roles for current user
    fun getAvailableRoles(): List<UserRole> {
        return when (_currentUser.value?.role) {
            UserRole.ARTIST -> listOf(UserRole.ARTIST)
            UserRole.VIEWER -> listOf(UserRole.VIEWER)
            UserRole.BOTH -> listOf(UserRole.ARTIST, UserRole.VIEWER)
            null -> emptyList()
        }
    }

    // Navigation methods
    fun navigateToArtworkUpload() {
        if (canCurrentUserCreateArtwork()) {
            _navigationEvent.value = NavigationEvent.NavigateToArtworkUpload
        } else {
            _errorMessage.value = "You don't have permission to upload artwork"
        }
    }

    fun navigateToArtworkList() {
        _navigationEvent.value = NavigationEvent.NavigateToArtworkList
    }

    fun navigateToBrowseArtwork() {
        _navigationEvent.value = NavigationEvent.NavigateToBrowseArtwork
    }

    fun navigateToFavorites() {
        _navigationEvent.value = NavigationEvent.NavigateToFavorites
    }

    fun navigateToProfile() {
        _navigationEvent.value = NavigationEvent.NavigateToProfile
    }

    // Utility methods
    fun canCurrentUserCreateArtwork(): Boolean {
        return when (_activeRole.value) {
            UserRole.ARTIST -> true
            UserRole.VIEWER -> false
            UserRole.BOTH -> true
        }
    }

    fun getCurrentUserDisplayName(): String {
        return _currentUser.value?.displayName ?: "User"
    }

    fun getCurrentUserRole(): String {
        return _currentUser.value?.getRoleDisplayName() ?: "Unknown"
    }

    // Clear navigation event after handling
    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    // Clear error message
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Refresh user data
    fun refreshUserData() {
        loadCurrentUser()
    }
}

// Navigation events enum
sealed class NavigationEvent {
    object NavigateToLogin : NavigationEvent()
    object NavigateToArtworkUpload : NavigationEvent()
    object NavigateToArtworkList : NavigationEvent()
    object NavigateToBrowseArtwork : NavigationEvent()
    object NavigateToFavorites : NavigationEvent()
    object NavigateToProfile : NavigationEvent()
}
