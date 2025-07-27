package com.example.inkspira_adigitalartportfolio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspira_adigitalartportfolio.view.screens.ArtworkData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DiscoverViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // ✅ FIXED: Proper generic type declarations
    private val _featuredArtworks = MutableStateFlow<List<ArtworkData>>(emptyList())
    val featuredArtworks: StateFlow<List<ArtworkData>> = _featuredArtworks.asStateFlow()

    private val _trendingArtworks = MutableStateFlow<List<ArtworkData>>(emptyList())
    val trendingArtworks: StateFlow<List<ArtworkData>> = _trendingArtworks.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ArtworkData>>(emptyList())
    val searchResults: StateFlow<List<ArtworkData>> = _searchResults.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _discoverArtists = MutableStateFlow<List<ArtistData>>(emptyList())
    val discoverArtists: StateFlow<List<ArtistData>> = _discoverArtists.asStateFlow()

    private val _isLoadingFeatured = MutableStateFlow(false)
    val isLoadingFeatured: StateFlow<Boolean> = _isLoadingFeatured.asStateFlow()

    private val _isLoadingSearch = MutableStateFlow(false)
    val isLoadingSearch: StateFlow<Boolean> = _isLoadingSearch.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    init {
        loadFeaturedArtworks()
        loadTrendingArtworks()
        loadCategories()
        loadDiscoverArtists()
    }

    fun loadFeaturedArtworks() {
        viewModelScope.launch {
            if (_featuredArtworks.value.isEmpty()) {
                _isLoadingFeatured.value = true
            } else {
                _isRefreshing.value = true
            }

            _errorMessage.value = null

            try {
                val snapshot = database.getReference("artworks")
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .limitToLast(50)
                    .get()
                    .await()

                val artworksList = mutableListOf<ArtworkData>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artwork = artworkSnapshot.toArtworkDataSafe()
                        artwork?.let {
                            val currentUserId = firebaseAuth.currentUser?.uid
                            if (it.userId != currentUserId) {
                                artworksList.add(it)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error converting individual artwork: ${e.message}")
                    }
                }

                _featuredArtworks.value = artworksList.sortedByDescending { it.createdAt }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load artworks. Please check your internet connection."
                println("Featured artworks error: ${e.message}")
            } finally {
                _isLoadingFeatured.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun loadTrendingArtworks() {
        viewModelScope.launch {
            try {
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

                val snapshot = database.getReference("artworks")
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val artworksList = mutableListOf<ArtworkData>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artwork = artworkSnapshot.toArtworkDataSafe()
                        artwork?.let {
                            if (it.createdAt >= sevenDaysAgo) {
                                val currentUserId = firebaseAuth.currentUser?.uid
                                if (it.userId != currentUserId) {
                                    artworksList.add(it)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error converting trending artwork: ${e.message}")
                    }
                }

                _trendingArtworks.value = artworksList.sortedByDescending {
                    it.likesCount + (it.viewsCount * 0.1).toInt()
                }.take(20)

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load trending artworks."
                println("Trending artworks error: ${e.message}")
            }
        }
    }

    fun searchArtworks(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            _isSearchMode.value = query.isNotBlank()

            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }

            _isLoadingSearch.value = true
            _errorMessage.value = null

            try {
                val snapshot = database.getReference("artworks")
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val searchResults = mutableListOf<ArtworkData>()
                val searchTerms = query.lowercase().split(" ").filter { it.isNotBlank() }

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artwork = artworkSnapshot.toArtworkDataSafe()
                        artwork?.let {
                            val currentUserId = firebaseAuth.currentUser?.uid
                            if (it.userId != currentUserId) {
                                val searchableText = "${it.title} ${it.description} ${it.category}".lowercase()

                                val matches = searchTerms.any { term ->
                                    searchableText.contains(term)
                                }

                                if (matches) {
                                    searchResults.add(it)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error in search conversion: ${e.message}")
                    }
                }

                _searchResults.value = searchResults.sortedWith(
                    compareByDescending<ArtworkData> { artwork ->
                        val searchableText = "${artwork.title} ${artwork.description} ${artwork.category}".lowercase()
                        searchTerms.count { term -> searchableText.contains(term) }
                    }.thenByDescending { it.createdAt }
                )

            } catch (e: Exception) {
                _errorMessage.value = "Search failed. Please try again."
                println("Search error: ${e.message}")
            } finally {
                _isLoadingSearch.value = false
            }
        }
    }

    fun filterByCategory(category: String) {
        _selectedCategory.value = category

        if (category == "All") {
            loadFeaturedArtworks()
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = database.getReference("artworks")
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val filteredArtworks = mutableListOf<ArtworkData>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artwork = artworkSnapshot.toArtworkDataSafe()
                        artwork?.let {
                            val currentUserId = firebaseAuth.currentUser?.uid
                            if (it.userId != currentUserId && it.category.equals(category, ignoreCase = true)) {
                                filteredArtworks.add(it)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error filtering artwork: ${e.message}")
                    }
                }

                _featuredArtworks.value = filteredArtworks.sortedByDescending { it.createdAt }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to filter artworks."
                println("Filter error: ${e.message}")
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val snapshot = database.getReference("artworks")
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val categoriesSet = mutableSetOf<String>()

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artwork = artworkSnapshot.toArtworkDataSafe()
                        artwork?.let {
                            if (it.category.isNotBlank()) {
                                categoriesSet.add(it.category)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error loading category: ${e.message}")
                    }
                }

                _categories.value = listOf("All") + categoriesSet.sorted()

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load categories."
                println("Categories error: ${e.message}")
            }
        }
    }

    fun loadDiscoverArtists() {
        viewModelScope.launch {
            try {
                val artworksSnapshot = database.getReference("artworks")
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val artistIds = mutableSetOf<String>()
                val currentUserId = firebaseAuth.currentUser?.uid

                artworksSnapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artwork = artworkSnapshot.toArtworkDataSafe()
                        artwork?.let {
                            if (it.userId != currentUserId && it.userId.isNotBlank()) {
                                artistIds.add(it.userId)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error getting artist IDs: ${e.message}")
                    }
                }

                val artists = mutableListOf<ArtistData>()

                artistIds.take(20).forEach { artistId ->
                    try {
                        val userSnapshot = database.getReference("users").child(artistId).get().await()
                        if (userSnapshot.exists()) {
                            val artistData = ArtistData(
                                id = artistId,
                                displayName = userSnapshot.child("displayName").value as? String ?: "",
                                username = userSnapshot.child("username").value as? String ?: "",
                                profileImageUrl = userSnapshot.child("profileImageUrl").value as? String ?: "",
                                bio = userSnapshot.child("bio").value as? String ?: "",
                                artworkCount = (userSnapshot.child("artworkCount").value as? Long)?.toInt() ?: 0,
                                followersCount = (userSnapshot.child("followersCount").value as? Long)?.toInt() ?: 0
                            )
                            artists.add(artistData)
                        }
                    } catch (e: Exception) {
                        println("Error loading artist $artistId: ${e.message}")
                    }
                }

                _discoverArtists.value = artists.sortedByDescending {
                    it.artworkCount + it.followersCount
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load artists."
                println("Artists error: ${e.message}")
            }
        }
    }

    fun toggleLikeArtwork(artworkId: String, currentlyLiked: Boolean) {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch
                val artworkRef = database.getReference("artworks").child(artworkId)

                val snapshot = artworkRef.child("likesCount").get().await()
                val currentLikes = (snapshot.value as? Long)?.toInt() ?: 0

                val newLikesCount = if (currentlyLiked) {
                    maxOf(0, currentLikes - 1)
                } else {
                    currentLikes + 1
                }

                artworkRef.child("likesCount").setValue(newLikesCount).await()
                updateLocalArtworkLikes(artworkId, newLikesCount)

            } catch (e: Exception) {
                _errorMessage.value = "Failed to update like."
                println("Like update error: ${e.message}")
            }
        }
    }

    fun incrementViewCount(artworkId: String) {
        viewModelScope.launch {
            try {
                val artworkRef = database.getReference("artworks").child(artworkId)
                val snapshot = artworkRef.child("viewsCount").get().await()
                val currentViews = (snapshot.value as? Long)?.toInt() ?: 0
                artworkRef.child("viewsCount").setValue(currentViews + 1)
            } catch (e: Exception) {
                println("Failed to increment view count: ${e.message}")
            }
        }
    }

    fun refreshData() {
        loadFeaturedArtworks()
        loadTrendingArtworks()
        loadCategories()
        loadDiscoverArtists()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearchMode.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun updateLocalArtworkLikes(artworkId: String, newLikesCount: Int) {
        _featuredArtworks.value = _featuredArtworks.value.map { artwork ->
            if (artwork.id == artworkId) {
                artwork.copy(likesCount = newLikesCount)
            } else artwork
        }

        _trendingArtworks.value = _trendingArtworks.value.map { artwork ->
            if (artwork.id == artworkId) {
                artwork.copy(likesCount = newLikesCount)
            } else artwork
        }

        _searchResults.value = _searchResults.value.map { artwork ->
            if (artwork.id == artworkId) {
                artwork.copy(likesCount = newLikesCount)
            } else artwork
        }
    }

    // ✅ CRITICAL: Safe DataSnapshot conversion
    private fun DataSnapshot.toArtworkDataSafe(): ArtworkData? {
        return try {
            ArtworkData(
                id = key ?: "",
                title = child("title").value as? String ?: "",
                description = child("description").value as? String ?: "",
                imageUrl = child("imageUrl").value as? String ?: "",
                category = child("category").value as? String ?: "Uncategorized",
                isPublic = child("isPublic").value as? Boolean ?: true,
                likesCount = (child("likesCount").value as? Long)?.toInt() ?: 0,
                viewsCount = (child("viewsCount").value as? Long)?.toInt() ?: 0,
                createdAt = child("uploadedAt").value as? Long ?: child("createdAt").value as? Long ?: System.currentTimeMillis(),
                userId = child("artistId").value as? String ?: ""
            )
        } catch (e: Exception) {
            println("Error converting artwork ${this.key}: ${e.message}")
            null
        }
    }
}

data class ArtistData(
    val id: String = "",
    val displayName: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val artworkCount: Int = 0,
    val followersCount: Int = 0
) {
    fun getProfileDisplayName(): String {
        return displayName.ifEmpty { username.ifEmpty { "Unknown Artist" } }
    }

    fun getFollowersText(): String {
        return when {
            followersCount >= 1000000 -> "${followersCount / 1000000}M followers"
            followersCount >= 1000 -> "${followersCount / 1000}K followers"
            followersCount == 1 -> "1 follower"
            else -> "$followersCount followers"
        }
    }

    fun getArtworkCountText(): String {
        return when {
            artworkCount == 1 -> "1 artwork"
            else -> "$artworkCount artworks"
        }
    }
}
