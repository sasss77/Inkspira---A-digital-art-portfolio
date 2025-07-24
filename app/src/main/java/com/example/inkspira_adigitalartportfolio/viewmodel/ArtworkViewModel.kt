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
    private val _uploadState = MutableStateFlow<NetworkResult<ArtworkModel>>(NetworkResult.Loading(false))
    val uploadState: StateFlow<NetworkResult<ArtworkModel>> = _uploadState.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Selected artwork for details
    private val _selectedArtwork = MutableStateFlow<ArtworkModel?>(null)
    val selectedArtwork: StateFlow<ArtworkModel?> = _selectedArtwork.asStateFlow()

    // Search functionality
    private val _searchResults = MutableStateFlow<List<ArtworkModel>>(emptyList())
    val searchResults: StateFlow<List<ArtworkModel>> = _searchResults.asStateFlow()

    init {
        loadPublicArtworks()
    }

    // Upload new artwork
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

        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            _errorMessage.value = null

            // First, upload image to Cloudinary
            when (val imageResult = cloudinaryRepository.uploadImage(imageUri, context)) {
                is NetworkResult.Success -> {
                    // Create artwork object
                    val artworkId = generateArtworkId()
                    val artwork = ArtworkModel(
                        artworkId = artworkId,
                        artistId = currentUserId,
                        title = title.trim(),
                        description = description.trim(),
                        imageUrl = imageResult.data,
                        tags = tags.map { it.trim() },
                        uploadedAt = System.currentTimeMillis(),
                        isPublic = isPublic
                    )

                    // Save artwork to database
                    when (val artworkResult = artworkRepository.createArtwork(artwork)) {
                        is NetworkResult.Success -> {
                            _uploadState.value = NetworkResult.Success(artworkResult.data)
                            _isUploading.value = false
                            // Refresh artwork lists
                            loadUserArtworks()
                            loadPublicArtworks()
                        }
                        is NetworkResult.Error -> {
                            _errorMessage.value = artworkResult.message
                            _uploadState.value = NetworkResult.Error(artworkResult.message)
                            _isUploading.value = false
                        }
                        is NetworkResult.Loading -> {
                            _isUploading.value = true
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = imageResult.message
                    _uploadState.value = NetworkResult.Error(imageResult.message)
                    _isUploading.value = false
                }
                is NetworkResult.Loading -> {
                    _isUploading.value = true
                }
            }
        }
    }

    // Load user's own artworks
    fun loadUserArtworks() {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            when (val result = artworkRepository.getArtworksByArtist(currentUserId)) {
                is NetworkResult.Success -> {
                    _artworkList.value = result.data
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

    // Load all public artworks
    fun loadPublicArtworks() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = artworkRepository.getAllPublicArtworks()) {
                is NetworkResult.Success -> {
                    _publicArtworks.value = result.data
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

    // Search artworks
    fun searchArtworks(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            when (val result = artworkRepository.searchArtworks(query.trim())) {
                is NetworkResult.Success -> {
                    _searchResults.value = result.data
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

    // Update artwork
    fun updateArtwork(artwork: ArtworkModel) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = artworkRepository.updateArtwork(artwork)) {
                is NetworkResult.Success -> {
                    _isLoading.value = false
                    // Refresh lists
                    loadUserArtworks()
                    loadPublicArtworks()
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

    // Delete artwork
    fun deleteArtwork(artworkId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = artworkRepository.deleteArtwork(artworkId)) {
                is NetworkResult.Success -> {
                    _isLoading.value = false
                    // Refresh lists
                    loadUserArtworks()
                    loadPublicArtworks()
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

    // Toggle artwork visibility
    fun toggleArtworkVisibility(artworkId: String, isPublic: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = artworkRepository.updateArtworkVisibility(artworkId, isPublic)) {
                is NetworkResult.Success -> {
                    _isLoading.value = false
                    // Refresh lists
                    loadUserArtworks()
                    loadPublicArtworks()
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
        return cloudinaryRepository.getOptimizedImageUrl(imageUrl, width, height)
    }

    // Get thumbnail URL
    fun getThumbnailUrl(imageUrl: String, size: Int = 300): String {
        return cloudinaryRepository.getThumbnailUrl(imageUrl, size)
    }

    // Clear error message
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Clear search results
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    // Generate unique artwork ID
    private fun generateArtworkId(): String {
        return "artwork_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
