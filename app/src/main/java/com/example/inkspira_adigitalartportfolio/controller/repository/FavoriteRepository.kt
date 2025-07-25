package com.example.inkspira_adigitalartportfolio.controller.repository

import com.example.inkspira_adigitalartportfolio.model.data.FavoriteModel
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

interface FavoriteRepository {

    // Favorite operations
    suspend fun addToFavorites(userId: String, artworkId: String): NetworkResult<FavoriteModel>

    suspend fun removeFromFavorites(userId: String, artworkId: String): NetworkResult<Boolean>

    suspend fun getUserFavorites(userId: String): NetworkResult<List<ArtworkModel>>

    suspend fun isFavorited(userId: String, artworkId: String): NetworkResult<Boolean>

    // Favorite management
    suspend fun getFavoritesByArtwork(artworkId: String): NetworkResult<List<FavoriteModel>>

    suspend fun clearUserFavorites(userId: String): NetworkResult<Boolean>
}
