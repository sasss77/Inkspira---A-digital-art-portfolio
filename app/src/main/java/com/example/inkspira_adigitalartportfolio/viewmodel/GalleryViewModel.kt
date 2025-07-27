package com.example.inkspira_adigitalartportfolio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inkspira_adigitalartportfolio.view.screens.ArtworkData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.inkspira_adigitalartportfolio.view.screens.SortOption

class GalleryViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // ✅ FIXED: Proper generic types (removed HTML entities)
    private val _artworks = MutableStateFlow<List<ArtworkData>>(emptyList())
    val artworks: StateFlow<List<ArtworkData>> = _artworks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentSortOption = SortOption.NEWEST

    fun loadUserArtworks() {
        viewModelScope.launch {
            println("🔥 GalleryViewModel: Starting loadUserArtworks...")

            if (_artworks.value.isEmpty()) {
                _isLoading.value = true
                println("🔥 GalleryViewModel: Setting loading to true")
            } else {
                _isRefreshing.value = true
                println("🔥 GalleryViewModel: Setting refreshing to true")
            }

            _errorMessage.value = null

            try {
                val userId = firebaseAuth.currentUser?.uid
                println("🔥 GalleryViewModel: Current user ID: $userId")

                if (userId != null) {
                    val snapshot = database.getReference("artworks")
                        .orderByChild("artistId")
                        .equalTo(userId)
                        .get()
                        .await()

                    println("🔥 GalleryViewModel: Firebase query complete. Children: ${snapshot.childrenCount}")

                    val artworksList = mutableListOf<ArtworkData>()

                    snapshot.children.forEach { artworkSnapshot ->
                        val artwork = artworkSnapshot.toArtworkData()
                        artwork?.let {
                            artworksList.add(it)
                            println("🔥 GalleryViewModel: Added artwork: ${it.title}")
                        }
                    }

                    println("🔥 GalleryViewModel: Total artworks loaded: ${artworksList.size}")
                    _artworks.value = sortArtworksList(artworksList, currentSortOption)

                } else {
                    _errorMessage.value = "User not logged in"
                    println("🔥 GalleryViewModel: User not logged in")
                }

            } catch (e: Exception) {
                val errorMsg = "Failed to load artworks: ${e.message}"
                _errorMessage.value = errorMsg
                println("🔥 GalleryViewModel: ERROR - $errorMsg")
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
                println("🔥 GalleryViewModel: Loading complete")
            }
        }
    }

    fun sortArtworks(sortOption: SortOption) {
        currentSortOption = sortOption
        val sortedList = sortArtworksList(_artworks.value, sortOption)
        _artworks.value = sortedList
    }

    // ✅ FIXED: Proper arrow syntax and generic types
    private fun sortArtworksList(artworks: List<ArtworkData>, sortOption: SortOption): List<ArtworkData> {
        return when (sortOption) {
            SortOption.NEWEST -> artworks.sortedByDescending { it.createdAt }
            SortOption.OLDEST -> artworks.sortedBy { it.createdAt }
            SortOption.MOST_LIKED -> artworks.sortedByDescending { it.likesCount }
            SortOption.MOST_VIEWED -> artworks.sortedByDescending { it.viewsCount }
            SortOption.TITLE_AZ -> artworks.sortedBy { it.title.lowercase() }
        }
    }

    fun deleteArtwork(artworkId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                database.getReference("artworks").child(artworkId).removeValue().await()

                val updatedList = _artworks.value.filter { it.id != artworkId }
                _artworks.value = updatedList

                val userId = firebaseAuth.currentUser?.uid
                userId?.let {
                    database.getReference("users").child(it)
                        .child("artworkCount").setValue(updatedList.size)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete artwork: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // ✅ FIXED: Complete extension function with better error handling
    private fun DataSnapshot.toArtworkData(): ArtworkData? {
        return try {
            ArtworkData(
                id = key ?: "",
                title = child("title").value as? String ?: "",
                description = child("description").value as? String ?: "",
                imageUrl = child("imageUrl").value as? String ?: "",
                category = child("category").value as? String ?: "",
                tags = emptyList(), // Always empty since tags removed
                isPublic = child("isPublic").value as? Boolean ?: true,
                likesCount = (child("likesCount").value as? Long)?.toInt() ?: 0,
                viewsCount = (child("viewsCount").value as? Long)?.toInt() ?: 0,
                commentsCount = (child("commentsCount").value as? Long)?.toInt() ?: 0,
                createdAt = child("uploadedAt").value as? Long ?: 0L,
                updatedAt = child("updatedAt").value as? Long ?: child("uploadedAt").value as? Long ?: 0L,
                userId = child("artistId").value as? String ?: ""
            )
        } catch (e: Exception) {
            println("🔥 GalleryViewModel: Error converting artwork: ${e.message}")
            null
        }
    }
}
