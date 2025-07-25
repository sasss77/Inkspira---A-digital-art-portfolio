package com.example.inkspira_adigitalartportfolio.controller.repository

import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

interface ArtworkRepository {

    // Artwork CRUD operations
    suspend fun createArtwork(artwork: ArtworkModel): NetworkResult<ArtworkModel>

    suspend fun getArtworkById(artworkId: String): NetworkResult<ArtworkModel?>

    suspend fun updateArtwork(artwork: ArtworkModel): NetworkResult<ArtworkModel>

    suspend fun deleteArtwork(artworkId: String): NetworkResult<Boolean>

    // Artist-specific operations
    suspend fun getArtworksByArtist(artistId: String): NetworkResult<List<ArtworkModel>>

    // Viewer operations
    suspend fun getAllPublicArtworks(): NetworkResult<List<ArtworkModel>>

    suspend fun searchArtworks(query: String): NetworkResult<List<ArtworkModel>>

    // Artwork management
    suspend fun updateArtworkVisibility(artworkId: String, isPublic: Boolean): NetworkResult<ArtworkModel>
}
