package com.example.inkspira_adigitalartportfolio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.view.screens.ArtworkData

class ArtworkViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val artworksRef = database.getReference("artworks")
    private val usersRef = database.getReference("users")

    // ✅ UPDATED: UI State with update functionality
    private val _uiState = MutableStateFlow(ArtworkUiState())
    val uiState: StateFlow<ArtworkUiState> = _uiState.asStateFlow()

    private val _userArtworks = MutableStateFlow<List<ArtworkData>>(emptyList())
    val userArtworks: StateFlow<List<ArtworkData>> = _userArtworks.asStateFlow()

    private val _publicArtworks = MutableStateFlow<List<ArtworkData>>(emptyList())
    val publicArtworks: StateFlow<List<ArtworkData>> = _publicArtworks.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ArtworkData>>(emptyList())
    val searchResults: StateFlow<List<ArtworkData>> = _searchResults.asStateFlow()

    private val _trendingArtworks = MutableStateFlow<List<ArtworkData>>(emptyList())
    val trendingArtworks: StateFlow<List<ArtworkData>> = _trendingArtworks.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _currentArtwork = MutableStateFlow<ArtworkData?>(null)
    val currentArtwork: StateFlow<ArtworkData?> = _currentArtwork.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadUserArtworks()
        loadPublicArtworks()
        loadTrendingArtworks()
        loadCategories()
    }

    fun loadUserArtworks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUserArtworks = true)

            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingUserArtworks = false,
                        errorMessage = "User not authenticated"
                    )
                    return@launch
                }

                val snapshot = artworksRef
                    .orderByChild("artistId")
                    .equalTo(userId)
                    .get()
                    .await()

                val artworksList = mutableListOf<ArtworkData>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artworkModel = artworkSnapshot.getValue(ArtworkModel::class.java)
                        artworkModel?.let { model ->
                            val artworkData = model.toArtworkData()
                            artworksList.add(artworkData)
                        }
                    } catch (e: Exception) {
                        println("Error converting user artwork ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                _userArtworks.value = artworksList.sortedByDescending { it.createdAt }
                _uiState.value = _uiState.value.copy(
                    isLoadingUserArtworks = false,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingUserArtworks = false,
                    errorMessage = "Failed to load user artworks: ${e.message}"
                )
            }
        }
    }

    fun loadPublicArtworks(limit: Int = 50) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPublicArtworks = true)

            try {
                val currentUserId = firebaseAuth.currentUser?.uid

                val snapshot = artworksRef
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .limitToLast(limit)
                    .get()
                    .await()

                val artworksList = mutableListOf<ArtworkData>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artworkModel = artworkSnapshot.getValue(ArtworkModel::class.java)
                        artworkModel?.let { model ->
                            if (model.artistId != currentUserId) {
                                val artworkData = model.toArtworkData()
                                artworksList.add(artworkData)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error converting public artwork ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                _publicArtworks.value = artworksList.sortedByDescending { it.createdAt }
                _uiState.value = _uiState.value.copy(
                    isLoadingPublicArtworks = false,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPublicArtworks = false,
                    errorMessage = "Failed to load public artworks: ${e.message}"
                )
            }
        }
    }

    fun searchArtworks(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query

            if (query.isBlank()) {
                _searchResults.value = emptyList()
                _uiState.value = _uiState.value.copy(isSearchMode = false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isSearching = true,
                isSearchMode = true
            )

            try {
                val currentUserId = firebaseAuth.currentUser?.uid
                val searchTerms = query.lowercase()
                    .split(" ")
                    .filter { it.isNotBlank() && it.length >= 2 }

                if (searchTerms.isEmpty()) {
                    _searchResults.value = emptyList()
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        isSearchMode = false
                    )
                    return@launch
                }

                val snapshot = artworksRef
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val searchResults = mutableListOf<Pair<ArtworkData, Int>>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artworkModel = artworkSnapshot.getValue(ArtworkModel::class.java)
                        artworkModel?.let { model ->
                            if (model.artistId != currentUserId) {
                                val searchableText = buildString {
                                    append(model.title.lowercase())
                                    append(" ")
                                    append(model.description.lowercase())
                                    append(" ")
                                    append(model.artistUsername.lowercase())
                                }

                                val relevanceScore = searchTerms.count { term ->
                                    searchableText.contains(term)
                                }

                                if (relevanceScore > 0) {
                                    val artworkData = model.toArtworkData()
                                    searchResults.add(artworkData to relevanceScore)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error in search for ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                val sortedResults = searchResults
                    .sortedWith(
                        compareByDescending<Pair<ArtworkData, Int>> { it.second }
                            .thenByDescending { it.first.createdAt }
                    )
                    .map { it.first }

                _searchResults.value = sortedResults
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    errorMessage = "Search failed: ${e.message}"
                )
            }
        }
    }

    fun loadTrendingArtworks(limit: Int = 20) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingTrending = true)

            try {
                val currentUserId = firebaseAuth.currentUser?.uid
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

                val snapshot = artworksRef
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val trendingList = mutableListOf<ArtworkData>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artworkModel = artworkSnapshot.getValue(ArtworkModel::class.java)
                        artworkModel?.let { model ->
                            if (model.artistId != currentUserId && model.uploadedAt >= sevenDaysAgo) {
                                val artworkData = model.toArtworkData()
                                trendingList.add(artworkData)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error converting trending artwork ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                _trendingArtworks.value = trendingList
                    .sortedByDescending { it.likesCount }
                    .take(limit)

                _uiState.value = _uiState.value.copy(
                    isLoadingTrending = false,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingTrending = false,
                    errorMessage = "Failed to load trending artworks: ${e.message}"
                )
            }
        }
    }

    fun filterByCategory(category: String) {
        viewModelScope.launch {
            _selectedCategory.value = category

            if (category == "All") {
                loadPublicArtworks()
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoadingPublicArtworks = true)

            try {
                val currentUserId = firebaseAuth.currentUser?.uid

                val snapshot = artworksRef
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val filteredList = mutableListOf<ArtworkData>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artworkModel = artworkSnapshot.getValue(ArtworkModel::class.java)
                        artworkModel?.let { model ->
                            if (model.artistId != currentUserId &&
                                model.title.contains(category, ignoreCase = true)) {
                                val artworkData = model.toArtworkData()
                                filteredList.add(artworkData)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error filtering artwork ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                _publicArtworks.value = filteredList.sortedByDescending { it.createdAt }
                _uiState.value = _uiState.value.copy(
                    isLoadingPublicArtworks = false,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPublicArtworks = false,
                    errorMessage = "Failed to filter artworks: ${e.message}"
                )
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val snapshot = artworksRef
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val categoriesSet = mutableSetOf<String>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artworkModel = artworkSnapshot.getValue(ArtworkModel::class.java)
                        artworkModel?.let { model ->
                            val titleWords = model.title.split(" ").filter { it.length > 3 }
                            titleWords.take(2).forEach { word ->
                                if (word.isNotBlank()) {
                                    categoriesSet.add(word.lowercase().replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase() else it.toString()
                                    })
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error loading categories from ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                _categories.value = listOf("All") + categoriesSet.sorted().take(10)

            } catch (e: Exception) {
                println("Failed to load categories: ${e.message}")
                _categories.value = listOf("All")
            }
        }
    }

    // ✅ NEW: Update Artwork Function
    fun updateArtwork(
        artworkId: String,
        title: String,
        description: String,
        category: String,
        isPublic: Boolean
    ) {
        viewModelScope.launch {
            println("🔄 ArtworkViewModel: Starting updateArtwork for ID: $artworkId")
            _uiState.value = _uiState.value.copy(isUpdatingArtwork = true)

            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingArtwork = false,
                        errorMessage = "User not authenticated"
                    )
                    return@launch
                }

                // Verify artwork ownership
                val artworkSnapshot = artworksRef.child(artworkId).get().await()
                val existingArtwork = artworkSnapshot.getValue(ArtworkModel::class.java)

                if (existingArtwork?.artistId != userId) {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingArtwork = false,
                        errorMessage = "Unauthorized: Cannot edit this artwork"
                    )
                    return@launch
                }

                println("✅ ArtworkViewModel: Ownership verified, updating artwork...")

                // Prepare update data
                val updates = mapOf(
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "category" to category.trim(),
                    "isPublic" to isPublic,
                    "updatedAt" to System.currentTimeMillis()
                )

                // Update in Firebase
                artworksRef.child(artworkId).updateChildren(updates).await()

                println("✅ ArtworkViewModel: Firebase update successful")

                // Update local state in all relevant lists
                updateLocalArtworkData(artworkId, title.trim(), description.trim(), category.trim(), isPublic)

                _uiState.value = _uiState.value.copy(
                    isUpdatingArtwork = false,
                    successMessage = "Artwork updated successfully!",
                    errorMessage = null
                )

                println("✅ ArtworkViewModel: Update completed successfully")

            } catch (e: Exception) {
                println("❌ ArtworkViewModel: Update failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isUpdatingArtwork = false,
                    errorMessage = "Failed to update artwork: ${e.message}"
                )
            }
        }
    }

    fun deleteArtwork(artworkId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = "User not authenticated"
                    )
                    return@launch
                }

                val artworkSnapshot = artworksRef.child(artworkId).get().await()
                val artwork = artworkSnapshot.getValue(ArtworkModel::class.java)

                if (artwork?.artistId != userId) {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = "Unauthorized: Cannot delete artwork"
                    )
                    return@launch
                }

                artworksRef.child(artworkId).removeValue().await()

                _userArtworks.value = _userArtworks.value.filter { it.id != artworkId }
                _publicArtworks.value = _publicArtworks.value.filter { it.id != artworkId }
                _searchResults.value = _searchResults.value.filter { it.id != artworkId }
                _trendingArtworks.value = _trendingArtworks.value.filter { it.id != artworkId }

                updateUserArtworkCount(userId, -1)

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    successMessage = "Artwork deleted successfully"
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Failed to delete artwork: ${e.message}"
                )
            }
        }
    }

    fun toggleLikeArtwork(artworkId: String, currentlyLiked: Boolean) {
        viewModelScope.launch {
            try {
                val artwork = findArtworkById(artworkId) ?: return@launch
                val newLikesCount = if (currentlyLiked) {
                    maxOf(0, artwork.likesCount - 1)
                } else {
                    artwork.likesCount + 1
                }

                artworksRef.child(artworkId).child("likesCount").setValue(newLikesCount).await()
                updateLocalArtworkLikes(artworkId, newLikesCount)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to update like: ${e.message}")
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _uiState.value = _uiState.value.copy(isSearchMode = false)
    }

    private fun findArtworkById(artworkId: String): ArtworkData? {
        return _userArtworks.value.find { it.id == artworkId }
            ?: _publicArtworks.value.find { it.id == artworkId }
            ?: _searchResults.value.find { it.id == artworkId }
            ?: _trendingArtworks.value.find { it.id == artworkId }
    }

    private fun updateLocalArtworkLikes(artworkId: String, newLikesCount: Int) {
        _userArtworks.value = _userArtworks.value.map { artwork ->
            if (artwork.id == artworkId) artwork.copy(likesCount = newLikesCount) else artwork
        }

        _publicArtworks.value = _publicArtworks.value.map { artwork ->
            if (artwork.id == artworkId) artwork.copy(likesCount = newLikesCount) else artwork
        }

        _searchResults.value = _searchResults.value.map { artwork ->
            if (artwork.id == artworkId) artwork.copy(likesCount = newLikesCount) else artwork
        }

        _trendingArtworks.value = _trendingArtworks.value.map { artwork ->
            if (artwork.id == artworkId) artwork.copy(likesCount = newLikesCount) else artwork
        }
    }

    // ✅ NEW: Helper function to update local artwork data after edit
    private fun updateLocalArtworkData(
        artworkId: String,
        title: String,
        description: String,
        category: String,
        isPublic: Boolean
    ) {
        val updatedAt = System.currentTimeMillis()

        println("🔄 ArtworkViewModel: Updating local artwork data for ID: $artworkId")

        // Update user artworks
        _userArtworks.value = _userArtworks.value.map { artwork ->
            if (artwork.id == artworkId) {
                println("✅ ArtworkViewModel: Updated artwork in userArtworks: $title")
                artwork.copy(
                    title = title,
                    description = description,
                    category = category,
                    isPublic = isPublic,
                    updatedAt = updatedAt
                )
            } else artwork
        }

        // Update public artworks (only if it's public)
        _publicArtworks.value = _publicArtworks.value.map { artwork ->
            if (artwork.id == artworkId) {
                if (isPublic) {
                    artwork.copy(
                        title = title,
                        description = description,
                        category = category,
                        isPublic = isPublic,
                        updatedAt = updatedAt
                    )
                } else {
                    // Remove from public list if made private
                    return@map null
                }
            } else artwork
        }.filterNotNull()

        // Update search results
        _searchResults.value = _searchResults.value.map { artwork ->
            if (artwork.id == artworkId) {
                artwork.copy(
                    title = title,
                    description = description,
                    category = category,
                    isPublic = isPublic,
                    updatedAt = updatedAt
                )
            } else artwork
        }

        // Update trending artworks
        _trendingArtworks.value = _trendingArtworks.value.map { artwork ->
            if (artwork.id == artworkId) {
                artwork.copy(
                    title = title,
                    description = description,
                    category = category,
                    isPublic = isPublic,
                    updatedAt = updatedAt
                )
            } else artwork
        }

        println("✅ ArtworkViewModel: Local data update completed")
    }

    private suspend fun updateUserArtworkCount(userId: String, increment: Int) {
        try {
            val userRef = usersRef.child(userId)
            val snapshot = userRef.child("artworkCount").get().await()
            val currentCount = (snapshot.value as? Long)?.toInt() ?: 0
            val newCount = maxOf(0, currentCount + increment)
            userRef.child("artworkCount").setValue(newCount).await()
        } catch (e: Exception) {
            println("Failed to update user artwork count: ${e.message}")
        }
    }

    // ✅ UPDATED: Extension function with proper field mapping
    private fun ArtworkModel.toArtworkData(): ArtworkData {
        return ArtworkData(
            id = this.id,
            title = this.title,
            description = this.description,
            imageUrl = this.getDisplayThumbnail(),

            tags = emptyList(), // Keep empty as requested
            isPublic = this.isPublic,
            likesCount = this.likesCount,
            viewsCount = this.viewsCount, // ✅ FIXED: Use actual viewsCount
            commentsCount = this.commentsCount, // ✅ FIXED: Use actual commentsCount
            createdAt = this.uploadedAt,

            userId = this.artistId
        )
    }
}

// ✅ UPDATED: Complete UI State data class with update functionality
data class ArtworkUiState(
    val isLoadingUserArtworks: Boolean = false,
    val isLoadingPublicArtworks: Boolean = false,
    val isLoadingTrending: Boolean = false,
    val isSearching: Boolean = false,
    val isSearchMode: Boolean = false,
    val isDeleting: Boolean = false,
    val isUpdatingArtwork: Boolean = false,  // ✅ NEW: For update operations
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val isLoading: Boolean
        get() = isLoadingUserArtworks || isLoadingPublicArtworks || isLoadingTrending ||
                isSearching || isDeleting || isUpdatingArtwork
}
