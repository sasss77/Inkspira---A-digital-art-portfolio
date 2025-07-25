package com.example.inkspira_adigitalartportfolio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspira_adigitalartportfolio.controller.repository.AuthRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.FavoriteRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.CloudinaryRepository
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {

    // Favorite artworks list
    private val _favoriteArtworks = MutableStateFlow<List<ArtworkModel>>(emptyList())
    val favoriteArtworks: StateFlow<List<ArtworkModel>> = _favoriteArtworks.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success message for user feedback
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Favorited status cache for quick UI updates
    private val _favoritedArtworks = MutableStateFlow<Set<String>>(emptySet())
    val favoritedArtworks: StateFlow<Set<String>> = _favoritedArtworks.asStateFlow()

    init {
        loadUserFavorites()
    }

    // Load user's favorite artworks - FIXED for null safety
    fun loadUserFavorites() {
        // ✅ FIXED: Handle nullable getCurrentUserId() properly
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = favoriteRepository.getUserFavorites(currentUserId)) {
                is NetworkResult.Success -> {
                    // ✅ FIXED: Handle nullable data with Elvis operator
                    val favoritesList = result.data ?: emptyList()
                    _favoriteArtworks.value = favoritesList
                    // Update favorited set for quick lookup
                    _favoritedArtworks.value = favoritesList.map { it.artworkId }.toSet()
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Failed to load favorites"
                    _errorMessage.value = errorMsg
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Add artwork to favorites
    fun addToFavorites(artworkId: String) {
        // ✅ FIXED: Handle nullable getCurrentUserId() properly
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            _errorMessage.value = "User not logged in"
            return
        }

        if (artworkId.isBlank()) {
            _errorMessage.value = "Invalid artwork ID"
            return
        }

        viewModelScope.launch {
            _errorMessage.value = null

            when (val result = favoriteRepository.addToFavorites(currentUserId, artworkId)) {
                is NetworkResult.Success -> {
                    // Update favorited set immediately for UI responsiveness
                    _favoritedArtworks.value = _favoritedArtworks.value + artworkId
                    _successMessage.value = "Added to favorites!"
                    // Refresh favorites list
                    loadUserFavorites()
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Failed to add to favorites"
                    _errorMessage.value = errorMsg
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    // Remove artwork from favorites
    fun removeFromFavorites(artworkId: String) {
        // ✅ FIXED: Handle nullable getCurrentUserId() properly
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            _errorMessage.value = "User not logged in"
            return
        }

        if (artworkId.isBlank()) {
            _errorMessage.value = "Invalid artwork ID"
            return
        }

        viewModelScope.launch {
            _errorMessage.value = null

            when (val result = favoriteRepository.removeFromFavorites(currentUserId, artworkId)) {
                is NetworkResult.Success -> {
                    // Update favorited set immediately for UI responsiveness
                    _favoritedArtworks.value = _favoritedArtworks.value - artworkId
                    _successMessage.value = "Removed from favorites!"
                    // Refresh favorites list
                    loadUserFavorites()
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Failed to remove from favorites"
                    _errorMessage.value = errorMsg
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    // Toggle favorite status
    fun toggleFavorite(artworkId: String) {
        if (isArtworkFavorited(artworkId)) {
            removeFromFavorites(artworkId)
        } else {
            addToFavorites(artworkId)
        }
    }

    // Check if artwork is favorited
    fun isArtworkFavorited(artworkId: String): Boolean {
        return _favoritedArtworks.value.contains(artworkId)
    }

    // Check favorite status from repository (for accurate state)
    fun checkFavoriteStatus(artworkId: String) {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) return

        if (artworkId.isBlank()) return

        viewModelScope.launch {
            when (val result = favoriteRepository.isFavorited(currentUserId, artworkId)) {
                is NetworkResult.Success -> {
                    val isFavorited = result.data ?: false
                    val currentSet = _favoritedArtworks.value.toMutableSet()
                    if (isFavorited) {
                        currentSet.add(artworkId)
                    } else {
                        currentSet.remove(artworkId)
                    }
                    _favoritedArtworks.value = currentSet
                }
                is NetworkResult.Error -> {
                    // Silently handle error for background check
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    // Clear all favorites
    fun clearAllFavorites() {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = favoriteRepository.clearUserFavorites(currentUserId)) {
                is NetworkResult.Success -> {
                    _favoriteArtworks.value = emptyList()
                    _favoritedArtworks.value = emptySet()
                    _successMessage.value = "All favorites cleared!"
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Failed to clear favorites"
                    _errorMessage.value = errorMsg
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Get optimized image URL for favorites display
    fun getOptimizedImageUrl(imageUrl: String, width: Int = 300, height: Int = 300): String {
        return if (imageUrl.isNotBlank()) {
            cloudinaryRepository.getOptimizedImageUrl(imageUrl, width, height)
        } else {
            imageUrl
        }
    }

    // Get thumbnail URL for favorites grid
    fun getThumbnailUrl(imageUrl: String, size: Int = 200): String {
        return if (imageUrl.isNotBlank()) {
            cloudinaryRepository.getThumbnailUrl(imageUrl, size)
        } else {
            imageUrl
        }
    }

    // Get favorites count
    fun getFavoritesCount(): Int {
        return _favoriteArtworks.value.size
    }

    // Check if favorites list is empty
    fun isFavoritesEmpty(): Boolean {
        return _favoriteArtworks.value.isEmpty()
    }

    // Clear error message
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Clear success message
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    // Refresh favorites
    fun refreshFavorites() {
        loadUserFavorites()
    }
}
