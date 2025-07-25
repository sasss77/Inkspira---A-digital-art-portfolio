package com.example.inkspira_adigitalartportfolio.controller.repositoryImpl

import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseRealtimeService
import com.example.inkspira_adigitalartportfolio.controller.repository.FavoriteRepository
import com.example.inkspira_adigitalartportfolio.model.data.FavoriteModel
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

class FavoriteRepositoryImpl(
    private val dbService: FirebaseRealtimeService
) : FavoriteRepository {

    private val favoritesNode = "favorites"
    private val artworksNode = "artworks"

    override suspend fun addToFavorites(userId: String, artworkId: String): NetworkResult<FavoriteModel> {
        // ✅ Validate input parameters
        if (userId.isBlank() || artworkId.isBlank()) {
            return NetworkResult.Error("User ID and Artwork ID cannot be empty")
        }

        val favoriteId = "${userId}_${artworkId}"
        val favorite = FavoriteModel(
            favoriteId = favoriteId,
            userId = userId,
            artworkId = artworkId,
            addedAt = System.currentTimeMillis()
        )

        val favoriteMap = mapOf(
            "favoriteId" to favoriteId,
            "userId" to userId,
            "artworkId" to artworkId,
            "addedAt" to favorite.addedAt
        )

        return when (val result = dbService.saveData(favoritesNode, favoriteId, favoriteMap)) {
            is NetworkResult.Success -> NetworkResult.Success(favorite)
            is NetworkResult.Error -> NetworkResult.Error("Failed to add to favorites: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun removeFromFavorites(userId: String, artworkId: String): NetworkResult<Boolean> {
        // ✅ Validate input parameters
        if (userId.isBlank() || artworkId.isBlank()) {
            return NetworkResult.Error("User ID and Artwork ID cannot be empty")
        }

        val favoriteId = "${userId}_${artworkId}"

        return when (val result = dbService.deleteData(favoritesNode, favoriteId)) {
            is NetworkResult.Success -> NetworkResult.Success(true)
            is NetworkResult.Error -> NetworkResult.Error("Failed to remove from favorites: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getUserFavorites(userId: String): NetworkResult<List<ArtworkModel>> {
        // ✅ Validate input parameter
        if (userId.isBlank()) {
            return NetworkResult.Error("User ID cannot be empty")
        }

        return when (val favoritesResult = dbService.queryData(favoritesNode, "userId", userId, FavoriteModel::class.java)) {
            is NetworkResult.Success -> {
                val artworks = mutableListOf<ArtworkModel>()

                // ✅ Safe iteration with null checks
                favoritesResult.data?.forEach { favorite ->
                    // ✅ Ensure artworkId is not null or empty before using
                    val artworkId = favorite.artworkId
                    if (artworkId.isNotBlank()) {
                        when (val artworkResult = dbService.getData(artworksNode, artworkId, ArtworkModel::class.java)) {
                            is NetworkResult.Success -> {
                                artworkResult.data?.let { artwork ->
                                    artworks.add(artwork)
                                }
                            }
                            is NetworkResult.Error -> {
                                // Continue with other artworks even if one fails
                            }
                            is NetworkResult.Loading -> {
                                // Handle loading state if needed
                            }
                        }
                    }
                }

                NetworkResult.Success(artworks)
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to get user favorites: ${favoritesResult.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun isFavorited(userId: String, artworkId: String): NetworkResult<Boolean> {
        // ✅ Validate input parameters
        if (userId.isBlank() || artworkId.isBlank()) {
            return NetworkResult.Error("User ID and Artwork ID cannot be empty")
        }

        val favoriteId = "${userId}_${artworkId}"

        return when (val result = dbService.getData(favoritesNode, favoriteId, FavoriteModel::class.java)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data != null)
            is NetworkResult.Error -> NetworkResult.Success(false) // If error, assume not favorited
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getFavoritesByArtwork(artworkId: String): NetworkResult<List<FavoriteModel>> {
        // ✅ Validate input parameter
        if (artworkId.isBlank()) {
            return NetworkResult.Error("Artwork ID cannot be empty")
        }

        return when (val result = dbService.queryData(favoritesNode, "artworkId", artworkId, FavoriteModel::class.java)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data ?: emptyList())
            is NetworkResult.Error -> NetworkResult.Error("Failed to get artwork favorites: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun clearUserFavorites(userId: String): NetworkResult<Boolean> {
        // ✅ Validate input parameter
        if (userId.isBlank()) {
            return NetworkResult.Error("User ID cannot be empty")
        }

        return when (val favoritesResult = dbService.queryData(favoritesNode, "userId", userId, FavoriteModel::class.java)) {
            is NetworkResult.Success -> {
                var allDeleted = true

                // ✅ Safe iteration with null checks
                favoritesResult.data?.forEach { favorite ->
                    // ✅ Ensure favoriteId is not null before using
                    val favoriteId = favorite.favoriteId
                    if (favoriteId.isNotBlank()) {
                        when (dbService.deleteData(favoritesNode, favoriteId)) {
                            is NetworkResult.Error -> allDeleted = false
                            else -> {} // Continue deletion
                        }
                    }
                }

                if (allDeleted) {
                    NetworkResult.Success(true)
                } else {
                    NetworkResult.Error("Failed to clear some favorites")
                }
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to clear user favorites: ${favoritesResult.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }
}
