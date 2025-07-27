package com.example.inkspira_adigitalartportfolio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.model.data.UserRole

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<NetworkResult<Any?>>(NetworkResult.Loading())
    val authState: StateFlow<NetworkResult<Any?>> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ✅ ENHANCED: Better success message handling
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ✅ ADDED: Email sent status for ForgetPassword
    private val _emailSent = MutableStateFlow(false)
    val emailSent: StateFlow<Boolean> = _emailSent.asStateFlow()

    // Firebase instances
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // ✅ TIMEOUT CONSTANTS - Configurable timeouts
    companion object {
        private const val AUTH_TIMEOUT = 30000L // 30 seconds
        private const val DATABASE_TIMEOUT = 25000L // 25 seconds
        private const val PASSWORD_RESET_TIMEOUT = 20000L // 20 seconds
    }

    // Get current user ID
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    // ✅ IMPROVED: Registration with TIMEOUT PROTECTION
    fun registerUser(
        email: String,
        password: String,
        displayName: String,
        selectedRole: UserRole
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                // Enhanced input validation
                val trimmedEmail = email.trim().lowercase()
                val trimmedDisplayName = displayName.trim()

                when {
                    trimmedEmail.isBlank() -> {
                        _errorMessage.value = "Email is required"
                        return@launch
                    }
                    password.isBlank() -> {
                        _errorMessage.value = "Password is required"
                        return@launch
                    }
                    trimmedDisplayName.isBlank() -> {
                        _errorMessage.value = "Display name is required"
                        return@launch
                    }
                    password.length < 6 -> {
                        _errorMessage.value = "Password must be at least 6 characters"
                        return@launch
                    }
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                        _errorMessage.value = "Please enter a valid email address"
                        return@launch
                    }
                }

                // ✅ TIMEOUT PROTECTION: Wrap Firebase Auth call
                val authResult = withTimeout(AUTH_TIMEOUT) {
                    firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password).await()
                }

                val userId = authResult.user?.uid ?: throw Exception("User creation failed")

                // Enhanced user data structure
                val userData = hashMapOf(
                    "userId" to userId,
                    "email" to trimmedEmail,
                    "displayName" to trimmedDisplayName,
                    "username" to trimmedEmail.substringBefore("@"),
                    "role" to selectedRole.name,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis(),
                    "profileComplete" to false,
                    "bio" to "",
                    "profileImageUrl" to "",
                    "websiteUrl" to "",
                    "artworkCount" to 0,
                    "followersCount" to 0,
                    "followingCount" to 0,
                    "isActive" to true,
                    "lastLoginAt" to System.currentTimeMillis(),
                    "emailVerified" to false
                )

                // ✅ TIMEOUT PROTECTION: Wrap Database call
                withTimeout(DATABASE_TIMEOUT) {
                    database.getReference("users").child(userId).setValue(userData).await()
                }

                _authState.value = NetworkResult.Success("Registration successful")
                _successMessage.value = "Account created successfully! Welcome to Inkspira!"

            } catch (e: TimeoutCancellationException) {
                // ✅ TIMEOUT HANDLING: Specific timeout error
                val timeoutMsg = "Registration is taking longer than usual. Please check your internet connection and try again."
                _authState.value = NetworkResult.Error(timeoutMsg)
                _errorMessage.value = timeoutMsg
                println("AuthViewModel: Registration timeout - ${e.message}")

            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("email address is already in use", ignoreCase = true) == true ->
                        "This email is already registered. Please use a different email or try logging in."
                    e.message?.contains("email address is badly formatted", ignoreCase = true) == true ->
                        "Please enter a valid email address"
                    e.message?.contains("weak password", ignoreCase = true) == true ->
                        "Password is too weak. Please use at least 6 characters."
                    e.message?.contains("network error", ignoreCase = true) == true ||
                            e.message?.contains("network request failed", ignoreCase = true) == true ->
                        "Network error. Please check your internet connection and try again."
                    e.message?.contains("play services", ignoreCase = true) == true ->
                        "Google Play Services error. Please update Google Play Services and try again."
                    else -> e.message ?: "Registration failed. Please try again."
                }

                _authState.value = NetworkResult.Error(errorMsg)
                _errorMessage.value = errorMsg
                println("AuthViewModel: Registration error - ${e.message}")

            } finally {
                _isLoading.value = false
            }
        }
    }

    var _artist = false;


    // ✅ IMPROVED: Login with TIMEOUT PROTECTION
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val trimmedEmail = email.trim().lowercase()

                // Input validation
                when {
                    trimmedEmail.isBlank() -> {
                        _errorMessage.value = "Email is required"
                        return@launch
                    }
                    password.isBlank() -> {
                        _errorMessage.value = "Password is required"
                        return@launch
                    }
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                        _errorMessage.value = "Please enter a valid email address"
                        return@launch
                    }
                }

                // ✅ TIMEOUT PROTECTION: Wrap Firebase Auth call
                val authResult = withTimeout(AUTH_TIMEOUT) {
                    firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password).await()
                }

                val userId = authResult.user?.uid

                // ✅ TIMEOUT PROTECTION: Wrap Database update (non-critical, shorter timeout)
                userId?.let {
                    try {
                        withTimeout(15000L) { // Shorter timeout for non-critical update
                            val updates = mapOf(
                                "lastLoginAt" to System.currentTimeMillis(),
                                "isActive" to true
                            )
                            database.getReference("users").child(it).updateChildren(updates).await()
                        }
                    } catch (timeoutException: TimeoutCancellationException) {
                        // Non-critical update failure - don't fail login
                        println("AuthViewModel: Login update timeout (non-critical)")
                    }
                }

                _authState.value = NetworkResult.Success("Login successful")
                _successMessage.value = "Welcome back!"

            } catch (e: TimeoutCancellationException) {
                // ✅ TIMEOUT HANDLING: Specific timeout error
                val timeoutMsg = "Login is taking longer than usual. Please check your internet connection and try again."
                _authState.value = NetworkResult.Error(timeoutMsg)
                _errorMessage.value = timeoutMsg
                println("AuthViewModel: Login timeout - ${e.message}")

            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("user not found", ignoreCase = true) == true ||
                            e.message?.contains("no user record", ignoreCase = true) == true ->
                        "No account found with this email. Please check your email or sign up."
                    e.message?.contains("wrong password", ignoreCase = true) == true ||
                            e.message?.contains("invalid-credential", ignoreCase = true) == true ||
                            e.message?.contains("invalid password", ignoreCase = true) == true ->
                        "Incorrect password. Please try again."
                    e.message?.contains("too many requests", ignoreCase = true) == true ->
                        "Too many failed attempts. Please try again later."
                    e.message?.contains("user disabled", ignoreCase = true) == true ->
                        "This account has been disabled. Please contact support."
                    e.message?.contains("network error", ignoreCase = true) == true ||
                            e.message?.contains("network request failed", ignoreCase = true) == true ->
                        "Network error. Please check your internet connection."
                    e.message?.contains("play services", ignoreCase = true) == true ->
                        "Google Play Services error. Please update Google Play Services and try again."
                    else -> e.message ?: "Login failed. Please try again."
                }

                _authState.value = NetworkResult.Error(errorMsg)
                _errorMessage.value = errorMsg
                println("AuthViewModel: Login error - ${e.message}")

            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ IMPROVED: Password Reset with TIMEOUT PROTECTION
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _emailSent.value = false

            try {
                val trimmedEmail = email.trim().lowercase()

                println("Debug: Attempting password reset for: $trimmedEmail")

                // Enhanced validation
                when {
                    trimmedEmail.isBlank() -> {
                        _errorMessage.value = "Email address is required"
                        return@launch
                    }
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                        _errorMessage.value = "Please enter a valid email address"
                        return@launch
                    }
                }

                // ✅ TIMEOUT PROTECTION: Wrap password reset call
                withTimeout(PASSWORD_RESET_TIMEOUT) {
                    firebaseAuth.sendPasswordResetEmail(trimmedEmail).await()
                }

                // ✅ SUCCESS: Set all success states
                _emailSent.value = true
                _successMessage.value = "Password reset link sent to $trimmedEmail. Please check your email (including spam folder)."

                println("Debug: Password reset email sent successfully to $trimmedEmail")

            } catch (e: TimeoutCancellationException) {
                // ✅ TIMEOUT HANDLING: Specific timeout error
                val timeoutMsg = "Password reset is taking longer than usual. Please check your internet connection and try again."
                _errorMessage.value = timeoutMsg
                println("AuthViewModel: Password reset timeout - ${e.message}")

            } catch (e: Exception) {
                println("Debug: Password reset error: ${e.message}")
                println("Debug: Error type: ${e.javaClass.simpleName}")

                val errorMsg = when {
                    e.message?.contains("user-not-found", ignoreCase = true) == true ||
                            e.message?.contains("USER_NOT_FOUND", ignoreCase = true) == true ||
                            e.message?.contains("no user record", ignoreCase = true) == true ->
                        "No account found with this email address. Please check your email or create an account."
                    e.message?.contains("invalid-email", ignoreCase = true) == true ||
                            e.message?.contains("INVALID_EMAIL", ignoreCase = true) == true ||
                            e.message?.contains("email address is badly formatted", ignoreCase = true) == true ->
                        "Please enter a valid email address"
                    e.message?.contains("too-many-requests", ignoreCase = true) == true ||
                            e.message?.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) == true ->
                        "Too many reset attempts. Please wait a few minutes before trying again."
                    e.message?.contains("network-request-failed", ignoreCase = true) == true ||
                            e.message?.contains("NETWORK_ERROR", ignoreCase = true) == true ->
                        "Network error. Please check your internet connection and try again."
                    e.message?.contains("QUOTA_EXCEEDED", ignoreCase = true) == true ->
                        "Email quota exceeded. Please try again later."
                    e.message?.contains("play services", ignoreCase = true) == true ->
                        "Google Play Services error. Please update Google Play Services and try again."
                    else -> {
                        println("Debug: Unhandled error - ${e.message}")
                        "Unable to send reset email. Please try again or contact support."
                    }
                }

                _errorMessage.value = errorMsg

            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ IMPROVED: Logout with TIMEOUT PROTECTION
    fun logoutUser() {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()

                // ✅ TIMEOUT PROTECTION: Wrap database update (non-critical)
                userId?.let {
                    try {
                        withTimeout(10000L) { // Short timeout for logout update
                            database.getReference("users").child(it)
                                .child("isActive").setValue(false).await()
                        }
                    } catch (timeoutException: TimeoutCancellationException) {
                        // Non-critical - don't prevent logout
                        println("AuthViewModel: Logout update timeout (non-critical)")
                    }
                }

                // Firebase signOut is local, no timeout needed
                firebaseAuth.signOut()
                _authState.value = NetworkResult.Success(null)
                _successMessage.value = "Logged out successfully"
                clearAllStates()

            } catch (e: Exception) {
                _errorMessage.value = "Error during logout: ${e.message}"
                println("AuthViewModel: Logout error - ${e.message}")
            }
        }
    }

    // User state checks
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null
    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email
    fun getCurrentUserDisplayName(): String? = firebaseAuth.currentUser?.displayName

    // ✅ ENHANCED: Get current user data with TIMEOUT PROTECTION
    fun getCurrentUserData() = flow {
        val userId = getCurrentUserId()
        if (userId != null) {
            try {
                // ✅ TIMEOUT PROTECTION: Wrap database call
                val snapshot = withTimeout(DATABASE_TIMEOUT) {
                    database.getReference("users").child(userId).get().await()
                }

                if (snapshot.exists()) {
                    emit(NetworkResult.Success(snapshot.value))
                } else {
                    emit(NetworkResult.Error("User data not found"))
                }
            } catch (e: TimeoutCancellationException) {
                emit(NetworkResult.Error("Request timeout. Please check your internet connection and try again."))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: "Failed to get user data"))
            }
        } else {
            emit(NetworkResult.Error("User not logged in"))
        }
    }

    // ✅ ENHANCED: Update user profile with TIMEOUT PROTECTION
    fun updateUserProfile(
        displayName: String? = null,
        bio: String? = null,
        profileImageUrl: String? = null,
        websiteUrl: String? = null
    ) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId == null) {
                _errorMessage.value = "User not logged in"
                return@launch
            }

            _isLoading.value = true
            try {
                val updates = mutableMapOf<String, Any>()
                displayName?.let { updates["displayName"] = it.trim() }
                bio?.let { updates["bio"] = it.trim() }
                profileImageUrl?.let { updates["profileImageUrl"] = it }
                websiteUrl?.let { updates["websiteUrl"] = it.trim() }
                updates["updatedAt"] = System.currentTimeMillis()

                // ✅ TIMEOUT PROTECTION: Wrap database update
                withTimeout(DATABASE_TIMEOUT) {
                    database.getReference("users").child(userId).updateChildren(updates).await()
                }

                _successMessage.value = "Profile updated successfully"

            } catch (e: TimeoutCancellationException) {
                _errorMessage.value = "Profile update timeout. Please check your internet connection and try again."
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ ENHANCED: Check authentication state with TIMEOUT PROTECTION
    fun checkAuthState() {
        viewModelScope.launch {
            try {
                // ✅ TIMEOUT PROTECTION: Even for auth state check
                withTimeout(10000L) {
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        _authState.value = NetworkResult.Success("User already logged in")
                    } else {
                        _authState.value = NetworkResult.Success(null)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                // If auth check times out, assume not logged in
                _authState.value = NetworkResult.Success(null)
                println("AuthViewModel: Auth state check timeout")
            } catch (e: Exception) {
                _authState.value = NetworkResult.Error("Failed to check authentication state")
                println("AuthViewModel: Auth state check error - ${e.message}")
            }
        }
    }

    // ✅ NEW: Check email verification with TIMEOUT PROTECTION
    fun checkEmailVerificationStatus() {
        viewModelScope.launch {
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    // ✅ TIMEOUT PROTECTION: Wrap user reload
                    withTimeout(15000L) {
                        currentUser.reload().await()
                    }

                    val isVerified = currentUser.isEmailVerified

                    // Update database with verification status (with timeout)
                    getCurrentUserId()?.let { userId ->
                        try {
                            withTimeout(10000L) {
                                database.getReference("users").child(userId)
                                    .child("emailVerified").setValue(isVerified).await()
                            }
                        } catch (timeoutException: TimeoutCancellationException) {
                            println("AuthViewModel: Email verification update timeout (non-critical)")
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                println("AuthViewModel: Email verification check timeout")
            } catch (e: Exception) {
                println("AuthViewModel: Email verification check error - ${e.message}")
            }
        }
    }

    // ✅ ENHANCED: Message management
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun clearEmailSentStatus() {
        _emailSent.value = false
    }

    fun clearAllStates() {
        _errorMessage.value = null
        _successMessage.value = null
        _emailSent.value = false
    }

    fun resetAuthState() {
        _authState.value = NetworkResult.Loading()
        clearAllStates()
    }

    // ✅ NEW: Resend password reset email
    fun resendPasswordResetEmail(email: String) {
        clearEmailSentStatus()
        sendPasswordResetEmail(email)
    }

    // ✅ NEW: Retry operations with exponential backoff
    suspend fun <T> retryOperation(
        maxAttempts: Int = 3,
        initialDelayMillis: Long = 1000,
        operation: suspend () -> T
    ): T {
        repeat(maxAttempts - 1) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                if (e is TimeoutCancellationException ||
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("play services", ignoreCase = true) == true) {
                    // Only retry for timeout and network errors
                    kotlinx.coroutines.delay(initialDelayMillis * (attempt + 1))
                } else {
                    throw e // Don't retry for other errors
                }
            }
        }
        return operation() // Last attempt
    }
}
