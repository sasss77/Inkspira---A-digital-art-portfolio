package com.example.inkspira_adigitalartportfolio.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.view.screens.ArtworkData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtworkRepositoryImpl @Inject constructor() : ArtworkRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val artworksRef = database.getReference("artworks")
    private val usersRef = database.getReference("users")

    // ✅ Get user's personal artworks (Gallery Screen)
    override suspend fun getUserArtworks(): NetworkResult<List<ArtworkData>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: return@withContext NetworkResult.Error("User not authenticated")

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
                        // Log individual conversion errors but continue processing
                        println("Error converting user artwork ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                val sortedArtworks = artworksList.sortedByDescending { it.createdAt }
                NetworkResult.Success(sortedArtworks)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to load user artworks: ${e.message}")
            }
        }
    }

    // ✅ Get public artworks for discovery (Discover Screen)
    override suspend fun getPublicArtworks(limit: Int): NetworkResult<List<ArtworkData>> {
        return withContext(Dispatchers.IO) {
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
                            // Exclude current user's artworks from discover feed
                            if (model.artistId != currentUserId) {
                                val artworkData = model.toArtworkData()
                                artworksList.add(artworkData)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error converting public artwork ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                val sortedArtworks = artworksList.sortedByDescending { it.createdAt }
                NetworkResult.Success(sortedArtworks)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to load public artworks: ${e.message}")
            }
        }
    }

    // ✅ Search artworks with multi-term support
    override suspend fun searchArtworks(query: String): NetworkResult<List<ArtworkData>> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.isBlank()) {
                    return@withContext NetworkResult.Success(emptyList())
                }

                val currentUserId = firebaseAuth.currentUser?.uid
                val searchTerms = query.lowercase()
                    .split(" ")
                    .filter { it.isNotBlank() && it.length >= 2 } // Minimum 2 characters per term

                if (searchTerms.isEmpty()) {
                    return@withContext NetworkResult.Success(emptyList())
                }

                val snapshot = artworksRef
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val searchResults = mutableListOf<Pair<ArtworkData, Int>>() // Pair of artwork and relevance score

                snapshot.children.forEach { artworkSnapshot ->
                    try {
                        val artworkModel = artworkSnapshot.getValue(ArtworkModel::class.java)
                        artworkModel?.let { model ->
                            if (model.artistId != currentUserId) {
                                // Create searchable text from multiple fields
                                val searchableText = buildString {
                                    append(model.title.lowercase())
                                    append(" ")
                                    append(model.description.lowercase())
                                    append(" ")

                                }

                                // Calculate relevance score
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

                // Sort by relevance score (descending) then by creation date (descending)
                val sortedResults = searchResults
                    .sortedWith(
                        compareByDescending<Pair<ArtworkData, Int>> { it.second }
                            .thenByDescending { it.first.createdAt }
                    )
                    .map { it.first }

                NetworkResult.Success(sortedResults)

            } catch (e: Exception) {
                NetworkResult.Error("Search failed: ${e.message}")
            }
        }
    }

    //  Get trending artworks (last 7 days, sorted by engagement)
    override suspend fun getTrendingArtworks(limit: Int): NetworkResult<List<ArtworkData>> {
        return withContext(Dispatchers.IO) {
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
                            // Only include recent artworks from other users
                            if (model.artistId != currentUserId && model.uploadedAt >= sevenDaysAgo) {
                                val artworkData = model.toArtworkData()
                                trendingList.add(artworkData)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error converting trending artwork ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                // Sort by engagement score (likes + views * 0.1) descending
                val sortedTrending = trendingList
                    .sortedByDescending { artwork ->
                        artwork.likesCount + (artwork.viewsCount * 0.1).toInt()
                    }
                    .take(limit)

                NetworkResult.Success(sortedTrending)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to load trending artworks: ${e.message}")
            }
        }
    }

    // ✅ Filter artworks by category
    override suspend fun getArtworksByCategory(category: String): NetworkResult<List<ArtworkData>> {
        return withContext(Dispatchers.IO) {
            try {
                if (category.isBlank() || category.equals("All", ignoreCase = true)) {
                    return@withContext getPublicArtworks()
                }

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
                            if (model.artistId != currentUserId
                               ) {
                                val artworkData = model.toArtworkData()
                                filteredList.add(artworkData)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error filtering artwork ${artworkSnapshot.key}: ${e.message}")
                    }
                }

                val sortedFiltered = filteredList.sortedByDescending { it.createdAt }
                NetworkResult.Success(sortedFiltered)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to filter artworks by category: ${e.message}")
            }
        }
    }

    // ✅ Get available categories from existing artworks
    override suspend fun getCategories(): NetworkResult<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = artworksRef
                    .orderByChild("isPublic")
                    .equalTo(true)
                    .get()
                    .await()

                val categoriesSet = mutableSetOf<String>()


                val sortedCategories = listOf("All") + categoriesSet.sorted()
                NetworkResult.Success(sortedCategories)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to load categories: ${e.message}")
            }
        }
    }

    // ✅ Save new artwork
    override suspend fun saveArtwork(artworkModel: ArtworkModel): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: return@withContext NetworkResult.Error("User not authenticated")

                // Ensure artwork has proper user ID
                val updatedArtwork = artworkModel.copy(
                    artistId = userId,
                    uploadedAt = if (artworkModel.uploadedAt == 0L) System.currentTimeMillis() else artworkModel.uploadedAt

                )

                // Save artwork to database
                artworksRef.child(updatedArtwork.id).setValue(updatedArtwork).await()

                // Update user's artwork count
                updateUserArtworkCount(userId, 1)

                NetworkResult.Success(true)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to save artwork: ${e.message}")
            }
        }
    }

    // ✅ Update existing artwork
    override suspend fun updateArtwork(artworkModel: ArtworkModel): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: return@withContext NetworkResult.Error("User not authenticated")

                // Verify ownership
                val existingSnapshot = artworksRef.child(artworkModel.id).get().await()
                val existingArtwork = existingSnapshot.getValue(ArtworkModel::class.java)

                if (existingArtwork?.artistId != userId) {
                    return@withContext NetworkResult.Error("Unauthorized: Cannot update artwork")
                }




                NetworkResult.Success(true)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to update artwork: ${e.message}")
            }
        }
    }

    // ✅ Delete artwork
    override suspend fun deleteArtwork(artworkId: String): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: return@withContext NetworkResult.Error("User not authenticated")

                // Verify ownership before deletion
                val artworkSnapshot = artworksRef.child(artworkId).get().await()
                val artwork = artworkSnapshot.getValue(ArtworkModel::class.java)

                if (artwork?.artistId != userId) {
                    return@withContext NetworkResult.Error("Unauthorized: Cannot delete artwork")
                }

                // Delete artwork
                artworksRef.child(artworkId).removeValue().await()

                // Update user's artwork count
                updateUserArtworkCount(userId, -1)

                NetworkResult.Success(true)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to delete artwork: ${e.message}")
            }
        }
    }

    // ✅ Update artwork likes count
    override suspend fun updateArtworkLikes(artworkId: String, newLikesCount: Int): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val validLikesCount = maxOf(0, newLikesCount) // Ensure non-negative
                artworksRef.child(artworkId).child("likesCount").setValue(validLikesCount).await()
                NetworkResult.Success(true)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to update likes: ${e.message}")
            }
        }
    }

    // ✅ Increment view count
    override suspend fun incrementViewCount(artworkId: String): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = artworksRef.child(artworkId).child("viewsCount").get().await()
                val currentViews = (snapshot.value as? Long)?.toInt() ?: 0
                val newViewsCount = currentViews + 1

                artworksRef.child(artworkId).child("viewsCount").setValue(newViewsCount).await()
                NetworkResult.Success(true)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to increment view count: ${e.message}")
            }
        }
    }

    // ✅ Get single artwork by ID
    override suspend fun getArtworkById(artworkId: String): NetworkResult<ArtworkData> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = artworksRef.child(artworkId).get().await()

                if (!snapshot.exists()) {
                    return@withContext NetworkResult.Error("Artwork not found")
                }

                val artworkModel = snapshot.getValue(ArtworkModel::class.java)
                    ?: return@withContext NetworkResult.Error("Invalid artwork data")

                val artworkData = artworkModel.toArtworkData()
                NetworkResult.Success(artworkData)

            } catch (e: Exception) {
                NetworkResult.Error("Failed to get artwork: ${e.message}")
            }
        }
    }

    // ✅ Helper function to update user artwork count
    private suspend fun updateUserArtworkCount(userId: String, increment: Int) {
        try {
            val userRef = usersRef.child(userId)
            val snapshot = userRef.child("artworkCount").get().await()
            val currentCount = (snapshot.value as? Long)?.toInt() ?: 0
            val newCount = maxOf(0, currentCount + increment) // Ensure non-negative
            userRef.child("artworkCount").setValue(newCount).await()
        } catch (e: Exception) {
            println("Failed to update user artwork count: ${e.message}")
            // Don't throw exception here to avoid failing the main operation
        }
    }
}

// ✅ FIXED: Extension function to convert ArtworkModel to ArtworkData
private fun ArtworkModel.toArtworkData(): ArtworkData {
    return ArtworkData(
        id = this.id,
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl.ifEmpty { this.thumbnailUrl }, // Use thumbnail as fallback


        isPublic = this.isPublic,
        likesCount = this.likesCount,


        createdAt = this.uploadedAt,

        userId = this.artistId
    )
}

