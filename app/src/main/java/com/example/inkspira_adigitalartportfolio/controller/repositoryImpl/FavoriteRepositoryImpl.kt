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
        val favoriteId = "${userId}_${artworkId}"
        val favorite = FavoriteModel(
            favoriteId = favoriteId,
            userId = userId,
            artworkId = artworkId,
            addedAt = System.currentTimeMillis()
        )

        val favoriteMap = mapOf(
            "favoriteId" to favorite.favoriteId,
            "userId" to favorite.userId,
            "artworkId" to favorite.artworkId,
            "addedAt" to favorite.addedAt
        )

        return when (val result = dbService.saveData(favoritesNode, favoriteId, favoriteMap)) {
            is NetworkResult.Success -> NetworkResult.Success(favorite)
            is NetworkResult.Error -> NetworkResult.Error(result.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun removeFromFavorites(userId: String, artworkId: String): NetworkResult<Boolean> {
        val favoriteId = "${userId}_${artworkId}"
        return dbService.deleteData(favoritesNode, favoriteId)
    }

    override suspend fun getUserFavorites(userId: String): NetworkResult<List<ArtworkModel>> {
        return when (val favoritesResult = dbService.queryData(favoritesNode, "userId", userId, FavoriteModel::class.java)) {
            is NetworkResult.Success -> {
                val artworks = mutableListOf<ArtworkModel>()

                for (favorite in favoritesResult.data) {
                    when (val artworkResult = dbService.getData(artworksNode, favorite.artworkId, ArtworkModel::class.java)) {
                        is NetworkResult.Success -> {
                            artworkResult.data?.let { artworks.add(it) }
                        }
                        is NetworkResult.Error -> {
                            // Continue with other artworks even if one fails
                        }
                        is NetworkResult.Loading -> {
                            // Handle loading state if needed
                        }
                    }
                }

                NetworkResult.Success(artworks)
            }
            is NetworkResult.Error -> NetworkResult.Error(favoritesResult.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun isFavorited(userId: String, artworkId: String): NetworkResult<Boolean> {
        val favoriteId = "${userId}_${artworkId}"
        return when (val result = dbService.getData(favoritesNode, favoriteId, FavoriteModel::class.java)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data != null)
            is NetworkResult.Error -> NetworkResult.Success(false) // If error, assume not favorited
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getFavoritesByArtwork(artworkId: String): NetworkResult<List<FavoriteModel>> {
        return dbService.queryData(favoritesNode, "artworkId", artworkId, FavoriteModel::class.java)
    }

    override suspend fun clearUserFavorites(userId: String): NetworkResult<Boolean> {
        return when (val favoritesResult = dbService.queryData(favoritesNode, "userId", userId, FavoriteModel::class.java)) {
            is NetworkResult.Success -> {
                var allDeleted = true

                for (favorite in favoritesResult.data) {
                    when (dbService.deleteData(favoritesNode, favorite.favoriteId)) {
                        is NetworkResult.Error -> allDeleted = false
                        else -> {} // Continue deletion
                    }
                }

                if (allDeleted) {
                    NetworkResult.Success(true)
                } else {
                    NetworkResult.Error("Failed to clear all favorites")
                }
            }
            is NetworkResult.Error -> NetworkResult.Error(favoritesResult.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }
}
