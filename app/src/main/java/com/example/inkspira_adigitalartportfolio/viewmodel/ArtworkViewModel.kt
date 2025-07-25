package com.example.inkspira_adigitalartportfolio.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspira_adigitalartportfolio.controller.repository.ArtworkRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.AuthRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.CloudinaryRepository
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtworkViewModel(
    private val artworkRepository: ArtworkRepository,
    private val cloudinaryRepository: CloudinaryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Artwork lists
    private val _artworkList = MutableStateFlow<List<ArtworkModel>>(emptyList())
    val artworkList: StateFlow<List<ArtworkModel>> = _artworkList.asStateFlow()

    private val _publicArtworks = MutableStateFlow<List<ArtworkModel>>(emptyList())
    val publicArtworks: StateFlow<List<ArtworkModel>> = _publicArtworks.asStateFlow()

    // Upload state
    private val _uploadState = MutableStateFlow<NetworkResult<ArtworkModel>>(NetworkResult.Loading())
    val uploadState: StateFlow<NetworkResult<ArtworkModel>> = _uploadState.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success message for user feedback
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Selected artwork for details
    private val _selectedArtwork = MutableStateFlow<ArtworkModel?>(null)
    val selectedArtwork: StateFlow<ArtworkModel?> = _selectedArtwork.asStateFlow()

    // Search functionality
    private val _searchResults = MutableStateFlow<List<ArtworkModel>>(emptyList())
    val searchResults: StateFlow<List<ArtworkModel>> = _searchResults.asStateFlow()

    init {
        loadPublicArtworks()
    }

    // Upload new artwork - COMPLETELY FIXED for all type issues
    fun uploadArtwork(
        title: String,
        description: String,
        tags: List<String>,
        imageUri: Uri,
        context: Context,
        isPublic: Boolean = true
    ) {
        // Validate input
        val validation = ValidationUtils.validateArtwork(title, description, tags)
        if (!validation.isValid) {
            _errorMessage.value = validation.errorMessage
            return
        }

        // Handle nullable getCurrentUserId() properly
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            _errorMessage.value = null

            // First, upload image to Cloudinary
            when (val imageResult = cloudinaryRepository.uploadImage(imageUri, context)) {
                is NetworkResult.Success -> {
                    // ✅ FIXED: Safe access to imageResult.data with proper null handling
                    val imageUrl = imageResult.data
                    if (imageUrl.isNullOrBlank()) {
                        _errorMessage.value = "Failed to get image URL"
                        _uploadState.value = NetworkResult.Error("Failed to get image URL")
                        _isUploading.value = false
                        return@launch
                    }

                    // Create artwork object with validated data
                    val artworkId = generateArtworkId()
                    val artwork = ArtworkModel(
                        artworkId = artworkId,
                        artistId = currentUserId,
                        title = title.trim(),
                        description = description.trim(),
                        imageUrl = imageUrl,
                        tags = tags.map { it.trim() },
                        uploadedAt = System.currentTimeMillis(),
                        isPublic = isPublic
                    )

                    // Save artwork to database
                    when (val artworkResult = artworkRepository.createArtwork(artwork)) {
                        is NetworkResult.Success -> {
                            // ✅ FIXED: Properly handle nullable successData
                            val successData = artworkResult.data
                            if (successData != null) {
                                _uploadState.value = NetworkResult.Success(successData)
                                _successMessage.value = "Artwork uploaded successfully!"
                            } else {
                                _uploadState.value = NetworkResult.Error("Failed to retrieve uploaded artwork")
                                _errorMessage.value = "Failed to retrieve uploaded artwork"
                            }
                            _isUploading.value = false
                            // Refresh artwork lists
                            loadUserArtworks()
                            loadPublicArtworks()
                        }
                        is NetworkResult.Error -> {
                            // ✅ FIXED: Properly handle nullable errorMsg
                            val errorMsg = artworkResult.message ?: "Unknown error occurred"
                            _errorMessage.value = "Failed to save artwork: $errorMsg"
                            _uploadState.value = NetworkResult.Error(errorMsg)
                            _isUploading.value = false
                        }
                        is NetworkResult.Loading -> {
                            _isUploading.value = true
                        }
                    }
                }
                is NetworkResult.Error -> {
                    // ✅ FIXED: Properly handle nullable errorMsg
                    val errorMsg = imageResult.message ?: "Image upload failed"
                    _errorMessage.value = "Failed to upload image: $errorMsg"
                    _uploadState.value = NetworkResult.Error(errorMsg)
                    _isUploading.value = false
                }
                is NetworkResult.Loading -> {
                    _isUploading.value = true
                }
            }
        }
    }

    // Load user's own artworks - FIXED for null safety
    fun loadUserArtworks() {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = artworkRepository.getArtworksByArtist(currentUserId)) {
                is NetworkResult.Success -> {
                    _artworkList.value = result.data ?: emptyList()
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Failed to load artworks"
                    _errorMessage.value = "Failed to load your artworks: $errorMsg"
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Load all public artworks
    fun loadPublicArtworks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = artworkRepository.getAllPublicArtworks()) {
                is NetworkResult.Success -> {
                    _publicArtworks.value = result.data ?: emptyList()
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Failed to load public artworks"
                    _errorMessage.value = "Failed to load public artworks: $errorMsg"
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Search artworks
    fun searchArtworks(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = artworkRepository.searchArtworks(query.trim())) {
                is NetworkResult.Success -> {
                    _searchResults.value = result.data ?: emptyList()
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Search failed"
                    _errorMessage.value = "Search failed: $errorMsg"
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Update artwork - ENHANCED with validation
    fun updateArtwork(artwork: ArtworkModel) {
        if (artwork.artworkId.isBlank()) {
            _errorMessage.value = "Invalid artwork ID"
            return
        }

        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            _errorMessage.value = "User not logged in"
            return
        }

        if (artwork.artistId != currentUserId) {
            _errorMessage.value = "You can only edit your own artworks"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = artworkRepository.updateArtwork(artwork)) {
                is NetworkResult.Success -> {
                    _successMessage.value = "Artwork updated successfully!"
                    _isLoading.value = false
                    loadUserArtworks()
                    loadPublicArtworks()
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Update failed"
                    _errorMessage.value = "Failed to update artwork: $errorMsg"
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Delete artwork - ENHANCED with validation
    fun deleteArtwork(artworkId: String) {
        if (artworkId.isBlank()) {
            _errorMessage.value = "Invalid artwork ID"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = artworkRepository.deleteArtwork(artworkId)) {
                is NetworkResult.Success -> {
                    _successMessage.value = "Artwork deleted successfully!"
                    _isLoading.value = false
                    loadUserArtworks()
                    loadPublicArtworks()
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Delete failed"
                    _errorMessage.value = "Failed to delete artwork: $errorMsg"
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Toggle artwork visibility - ENHANCED with validation
    fun toggleArtworkVisibility(artworkId: String, isPublic: Boolean) {
        if (artworkId.isBlank()) {
            _errorMessage.value = "Invalid artwork ID"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = artworkRepository.updateArtworkVisibility(artworkId, isPublic)) {
                is NetworkResult.Success -> {
                    val visibilityText = if (isPublic) "public" else "private"
                    _successMessage.value = "Artwork is now $visibilityText!"
                    _isLoading.value = false
                    loadUserArtworks()
                    loadPublicArtworks()
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Visibility update failed"
                    _errorMessage.value = "Failed to update visibility: $errorMsg"
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Get artwork by ID - FIXED method for artwork details
    fun getArtworkById(artworkId: String) {
        if (artworkId.isBlank()) {
            _errorMessage.value = "Invalid artwork ID"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = artworkRepository.getArtworkById(artworkId)) {
                is NetworkResult.Success -> {
                    result.data?.let { artwork ->
                        _selectedArtwork.value = artwork
                    } ?: run {
                        _errorMessage.value = "Artwork not found"
                    }
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    val errorMsg = result.message ?: "Failed to load artwork"
                    _errorMessage.value = "Failed to load artwork: $errorMsg"
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // Select artwork for details view
    fun selectArtwork(artwork: ArtworkModel) {
        _selectedArtwork.value = artwork
    }

    // Clear selected artwork
    fun clearSelectedArtwork() {
        _selectedArtwork.value = null
    }

    // Get optimized image URL
    fun getOptimizedImageUrl(imageUrl: String, width: Int = 400, height: Int = 400): String {
        return if (imageUrl.isNotBlank()) {
            cloudinaryRepository.getOptimizedImageUrl(imageUrl, width, height)
        } else {
            imageUrl
        }
    }

    // Get thumbnail URL
    fun getThumbnailUrl(imageUrl: String, size: Int = 300): String {
        return if (imageUrl.isNotBlank()) {
            cloudinaryRepository.getThumbnailUrl(imageUrl, size)
        } else {
            imageUrl
        }
    }

    // Check if current user can edit artwork
    fun canEditArtwork(artwork: ArtworkModel): Boolean {
        val currentUserId = authRepository.getCurrentUserId()
        return currentUserId != null && artwork.artistId == currentUserId
    }

    // Get artwork stats
    fun getArtworkStats(): Map<String, Int> {
        return mapOf(
            "total" to _artworkList.value.size,
            "public" to _artworkList.value.count { it.isPublic },
            "private" to _artworkList.value.count { !it.isPublic }
        )
    }

    // Clear error message
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Clear success message
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    // Clear search results
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    // Refresh all artwork data
    fun refreshAllData() {
        loadUserArtworks()
        loadPublicArtworks()
    }

    // Generate unique artwork ID
    private fun generateArtworkId(): String {
        return "artwork_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
