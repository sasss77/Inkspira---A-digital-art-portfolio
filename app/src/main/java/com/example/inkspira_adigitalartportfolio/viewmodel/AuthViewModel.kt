package com.example.inkspira_adigitalartportfolio.viewmodel

import UserModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspira_adigitalartportfolio.controller.repository.AuthRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.UserRepository
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // UI State Management
    private val _authState = MutableStateFlow<NetworkResult<UserModel>>(NetworkResult.Loading())
    val authState: StateFlow<NetworkResult<UserModel>> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // User session state
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    init {
        checkUserSession()
    }

    // Check if user is already logged in - FIXED for null safety
    private fun checkUserSession() {
        viewModelScope.launch {
            if (authRepository.isUserLoggedIn()) {
                // ✅ FIXED: Handle nullable getCurrentUserId() properly
                val userId = authRepository.getCurrentUserId()
                if (!userId.isNullOrBlank()) {
                    when (val result = userRepository.getUserById(userId)) {
                        is NetworkResult.Success -> {
                            result.data?.let { user ->
                                _currentUser.value = user
                                _authState.value = NetworkResult.Success(user)
                            } ?: run {
                                _authState.value = NetworkResult.Error("User data not found")
                            }
                        }
                        is NetworkResult.Error -> {
                            val errorMsg = result.message ?: "Failed to load user data"
                            _authState.value = NetworkResult.Error(errorMsg)
                        }
                        is NetworkResult.Loading -> {
                            _authState.value = NetworkResult.Loading()
                        }
                    }
                } else {
                    _authState.value = NetworkResult.Error("Invalid user session")
                }
            }
        }
    }

    // Expose isUserLoggedIn from AuthRepository
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    // Get current user ID with null safety
    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    // User Login - ENHANCED with better error handling
    fun loginUser(email: String, password: String) {
        // Validate input
        val validation = ValidationUtils.validateLogin(email, password)
        if (!validation.isValid) {
            _errorMessage.value = validation.errorMessage
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = authRepository.loginUser(email.trim(), password)) {
                is NetworkResult.Success -> {
                    val userData = result.data
                    if (userData != null) {
                        _currentUser.value = userData
                        _authState.value = NetworkResult.Success(userData)
                    } else {
                        _authState.value = NetworkResult.Error("Login successful but user data unavailable")
                    }
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Login failed"
                    _errorMessage.value = errorMsg
                    _authState.value = NetworkResult.Error(errorMsg)
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // User Registration - ENHANCED with better error handling
    fun registerUser(email: String, password: String, displayName: String, selectedRole: UserRole) {
        // Validate input
        val validation = ValidationUtils.validateUserRegistration(email, password, displayName)
        if (!validation.isValid) {
            _errorMessage.value = validation.errorMessage
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = authRepository.registerUser(
                email.trim(),
                password,
                displayName.trim(),
                selectedRole.name
            )) {
                is NetworkResult.Success -> {
                    val userData = result.data
                    if (userData != null) {
                        _currentUser.value = userData
                        _authState.value = NetworkResult.Success(userData)
                    } else {
                        _authState.value = NetworkResult.Error("Registration successful but user data unavailable")
                    }
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Registration failed"
                    _errorMessage.value = errorMsg
                    _authState.value = NetworkResult.Error(errorMsg)
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // User Logout
    fun logoutUser() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = authRepository.logoutUser()) {
                is NetworkResult.Success -> {
                    _currentUser.value = null
                    _authState.value = NetworkResult.Loading()
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Logout failed"
                    _errorMessage.value = errorMsg
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Send Password Reset Email
    fun sendPasswordResetEmail(email: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = authRepository.sendPasswordResetEmail(email.trim())) {
                is NetworkResult.Success -> {
                    _errorMessage.value = "Password reset email sent successfully"
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Failed to send password reset email"
                    _errorMessage.value = errorMsg
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Clear error message
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Get current user role
    fun getCurrentUserRole(): UserRole? {
        return _currentUser.value?.role
    }

    // Check if user can create artwork
    fun canCurrentUserCreateArtwork(): Boolean {
        return _currentUser.value?.canCreateArtwork() ?: false
    }
}

// Extension function for login validation
private fun ValidationUtils.validateLogin(email: String, password: String): ValidationUtils.ValidationResult {
    return when {
        !isValidEmail(email) -> ValidationUtils.ValidationResult.invalid("Please enter a valid email address")
        password.isBlank() -> ValidationUtils.ValidationResult.invalid("Password cannot be empty")
        else -> ValidationUtils.ValidationResult.valid()
    }
}
