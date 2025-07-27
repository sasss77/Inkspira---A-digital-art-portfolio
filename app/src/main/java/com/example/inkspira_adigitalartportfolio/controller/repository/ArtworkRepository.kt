package com.example.inkspira_adigitalartportfolio.repository

import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.view.screens.ArtworkData

interface ArtworkRepository {

    // User's personal artworks (Gallery Screen)
    suspend fun getUserArtworks(): NetworkResult<List<ArtworkData>>

    // Public artworks for discovery (Discover Screen)
    suspend fun getPublicArtworks(limit: Int = 50): NetworkResult<List<ArtworkData>>

    // Search functionality
    suspend fun searchArtworks(query: String): NetworkResult<List<ArtworkData>>

    // Trending artworks (last 7 days)
    suspend fun getTrendingArtworks(limit: Int = 20): NetworkResult<List<ArtworkData>>

    // Category filtering
    suspend fun getArtworksByCategory(category: String): NetworkResult<List<ArtworkData>>

    // Get available categories
    suspend fun getCategories(): NetworkResult<List<String>>

    // CRUD Operations
    suspend fun saveArtwork(artworkModel: ArtworkModel): NetworkResult<Boolean>
    suspend fun updateArtwork(artworkModel: ArtworkModel): NetworkResult<Boolean>
    suspend fun deleteArtwork(artworkId: String): NetworkResult<Boolean>

    // Social features
    suspend fun updateArtworkLikes(artworkId: String, newLikesCount: Int): NetworkResult<Boolean>
    suspend fun incrementViewCount(artworkId: String): NetworkResult<Boolean>

    // Get single artwork
    suspend fun getArtworkById(artworkId: String): NetworkResult<ArtworkData>
}
