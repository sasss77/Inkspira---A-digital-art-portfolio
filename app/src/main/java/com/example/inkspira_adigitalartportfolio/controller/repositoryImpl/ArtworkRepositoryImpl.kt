package com.example.inkspira_adigitalartportfolio.controller.repositoryImpl

import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseRealtimeService
import com.example.inkspira_adigitalartportfolio.controller.repository.ArtworkRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.UserRepository
import com.example.inkspira_adigitalartportfolio.controller.repository.CloudinaryRepository
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.Constants
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

class ArtworkRepositoryImpl(
    private val dbService: FirebaseRealtimeService,
    private val userRepository: UserRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ArtworkRepository {

    private val artworksNode = Constants.ARTWORKS_NODE

    override suspend fun createArtwork(artwork: ArtworkModel): NetworkResult<ArtworkModel> {
        // ✅ Validate artwork before creation
        if (!artwork.isValid()) {
            return NetworkResult.Error("Invalid artwork data: missing required fields")
        }

        // ✅ Generate artwork ID if empty
        val artworkId = artwork.id.ifEmpty {
            "artwork_${System.currentTimeMillis()}_${(1000..9999).random()}"
        }

        // ✅ Enrich artwork with complete user data
        val enrichedArtwork = enrichArtworkWithUserData(artwork.copy(id = artworkId))

        return when (val result = dbService.saveData(artworksNode, artworkId, enrichedArtwork.toMap())) {
            is NetworkResult.Success -> NetworkResult.Success(enrichedArtwork)
            is NetworkResult.Error -> NetworkResult.Error("Failed to create artwork: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getArtworkById(artworkId: String): NetworkResult<ArtworkModel?> {
        if (artworkId.isBlank()) {
            return NetworkResult.Error("Artwork ID cannot be empty")
        }

        return when (val result = dbService.getData(artworksNode, artworkId, ArtworkModel::class.java)) {
            is NetworkResult.Success -> {
                val artwork = result.data
                if (artwork != null) {
                    // ✅ Enrich with user data before returning
                    val enrichedArtwork = enrichArtworkWithUserData(artwork)
                    NetworkResult.Success(enrichedArtwork)
                } else {
                    NetworkResult.Success(null)
                }
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve artwork: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun updateArtwork(artwork: ArtworkModel): NetworkResult<ArtworkModel> {
        // ✅ Validate artwork data
        if (!artwork.isValid()) {
            return NetworkResult.Error("Invalid artwork data: missing required fields")
        }

        if (artwork.id.isBlank()) {
            return NetworkResult.Error("Invalid artwork ID")
        }

        // ✅ Update timestamp
        val updatedArtwork = artwork.copy(updatedAt = System.currentTimeMillis())

        return when (val result = dbService.updateData(artworksNode, artwork.id, updatedArtwork.toMap())) {
            is NetworkResult.Success -> NetworkResult.Success(updatedArtwork)
            is NetworkResult.Error -> NetworkResult.Error("Failed to update artwork: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun deleteArtwork(artworkId: String): NetworkResult<Boolean> {
        if (artworkId.isBlank()) {
            return NetworkResult.Error("Artwork ID cannot be empty")
        }

        return when (val result = dbService.deleteData(artworksNode, artworkId)) {
            is NetworkResult.Success -> NetworkResult.Success(true)
            is NetworkResult.Error -> NetworkResult.Error("Failed to delete artwork: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getArtworksByArtist(artistId: String): NetworkResult<List<ArtworkModel>> {
        if (artistId.isBlank()) {
            return NetworkResult.Error("Artist ID cannot be empty")
        }

        return when (val result = dbService.queryData(artworksNode, "artistId", artistId, ArtworkModel::class.java)) {
            is NetworkResult.Success -> {
                val artworks = result.data ?: emptyList()
                // ✅ Enrich all artworks with user data and sort by date
                val enrichedArtworks = artworks.map { enrichArtworkWithUserData(it) }
                    .sortedByDescending { it.uploadedAt }
                NetworkResult.Success(enrichedArtworks)
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve artist artworks: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getAllPublicArtworks(): NetworkResult<List<ArtworkModel>> {
        // ✅ CRITICAL FIX: Use boolean true instead of string "true"
        return when (val result = dbService.queryData(artworksNode, "isPublic", "true", ArtworkModel::class.java)) {
            is NetworkResult.Success -> {
                val artworks = result.data ?: emptyList()
                // ✅ Enrich all artworks with user data and sort by date
                val enrichedArtworks = artworks.map { enrichArtworkWithUserData(it) }
                    .sortedByDescending { it.uploadedAt }
                NetworkResult.Success(enrichedArtworks)
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve public artworks: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun searchArtworks(query: String): NetworkResult<List<ArtworkModel>> {
        if (query.isBlank()) {
            return NetworkResult.Success(emptyList())
        }

        // ✅ Enhanced search with enriched data
        return when (val result = getAllPublicArtworks()) {
            is NetworkResult.Success -> {
                val filteredArtworks = result.data?.filter { artwork ->
                    artwork.title.contains(query, ignoreCase = true) ||
                            artwork.description.contains(query, ignoreCase = true) ||
                            artwork.tags.any { tag -> tag.contains(query, ignoreCase = true) } ||
                            artwork.artistUsername.contains(query, ignoreCase = true)
                } ?: emptyList()

                NetworkResult.Success(filteredArtworks)
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to search artworks: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun updateArtworkVisibility(artworkId: String, isPublic: Boolean): NetworkResult<ArtworkModel> {
        if (artworkId.isBlank()) {
            return NetworkResult.Error("Artwork ID cannot be empty")
        }

        val updates = mapOf(
            "isPublic" to isPublic,
            "updatedAt" to System.currentTimeMillis()
        )

        return when (val updateResult = dbService.updateData(artworksNode, artworkId, updates)) {
            is NetworkResult.Success -> {
                // ✅ Properly handle nullable return type
                when (val artworkResult = getArtworkById(artworkId)) {
                    is NetworkResult.Success -> {
                        artworkResult.data?.let { artwork ->
                            NetworkResult.Success(artwork.copy(isPublic = isPublic))
                        } ?: NetworkResult.Error("Artwork not found after visibility update")
                    }
                    is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve updated artwork: ${artworkResult.message}")
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to update artwork visibility: ${updateResult.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    // ✅ Get artworks by tag
    suspend fun getArtworksByTag(tag: String): NetworkResult<List<ArtworkModel>> {
        if (tag.isBlank()) {
            return NetworkResult.Error("Tag cannot be empty")
        }

        return when (val result = getAllPublicArtworks()) {
            is NetworkResult.Success -> {
                val filteredArtworks = result.data?.filter { artwork ->
                    artwork.tags.any { artworkTag ->
                        artworkTag.equals(tag, ignoreCase = true)
                    }
                } ?: emptyList()
                NetworkResult.Success(filteredArtworks)
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to get artworks by tag: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    // ✅ Get recent artworks (last 7 days)
    suspend fun getRecentArtworks(limit: Int = 10): NetworkResult<List<ArtworkModel>> {
        return when (val result = getAllPublicArtworks()) {
            is NetworkResult.Success -> {
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                val recentArtworks = result.data?.filter { artwork ->
                    artwork.uploadedAt >= sevenDaysAgo
                }?.take(limit) ?: emptyList()

                NetworkResult.Success(recentArtworks)
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to get recent artworks: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    // ✅ PRIVATE: Enrich artwork with complete user data
    private suspend fun enrichArtworkWithUserData(artwork: ArtworkModel): ArtworkModel {
        return try {
            // Skip if already has complete data
            if (artwork.artistUsername.isNotEmpty() && artwork.thumbnailUrl.isNotEmpty()) {
                return artwork
            }

            // Fetch artist information
            val artistUsername = if (artwork.artistUsername.isEmpty()) {
                when (val userResult = userRepository.getUserById(artwork.artistId)) {
                    is NetworkResult.Success -> userResult.data?.displayName ?: "Unknown Artist"
                    else -> "Unknown Artist"
                }
            } else {
                artwork.artistUsername
            }

            // Generate thumbnail URL if needed
            val thumbnailUrl = if (artwork.thumbnailUrl.isEmpty() && artwork.imageUrl.isNotEmpty()) {
                try {
                    cloudinaryRepository.getThumbnailUrl(artwork.imageUrl)
                } catch (e: Exception) {
                    artwork.imageUrl // Fallback to original image
                }
            } else {
                artwork.thumbnailUrl
            }

            // Return enriched artwork
            artwork.copy(
                artistUsername = artistUsername,
                thumbnailUrl = thumbnailUrl
            )
        } catch (e: Exception) {
            // Return original artwork if enrichment fails
            artwork
        }
    }

    // ✅ CRITICAL FIX: Batch update artworks with proper null safety
    suspend fun batchUpdateArtworks(artworks: List<ArtworkModel>): NetworkResult<List<ArtworkModel>> {
        val updatedArtworks = mutableListOf<ArtworkModel>()
        val errors = mutableListOf<String>()

        artworks.forEach { artwork ->
            when (val result = updateArtwork(artwork)) {
                is NetworkResult.Success -> {
                    // ✅ FIXED: Properly handle nullable data
                    result.data?.let { updatedArtwork ->
                        updatedArtworks.add(updatedArtwork)
                    } ?: run {
                        errors.add("Updated artwork ${artwork.id} returned null data")
                    }
                }
                is NetworkResult.Error -> {
                    errors.add("Failed to update ${artwork.id}: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    errors.add("Update operation for ${artwork.id} is still in progress")
                }
            }
        }

        return if (errors.isEmpty()) {
            NetworkResult.Success(updatedArtworks)
        } else {
            NetworkResult.Error("Batch update completed with errors: ${errors.joinToString(", ")}")
        }
    }
}
