package com.example.inkspira_adigitalartportfolio.controller.repositoryImpl

import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseRealtimeService
import com.example.inkspira_adigitalartportfolio.controller.repository.ArtworkRepository
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

class ArtworkRepositoryImpl(
    private val dbService: FirebaseRealtimeService
) : ArtworkRepository {

    private val artworksNode = "artworks"

    override suspend fun createArtwork(artwork: ArtworkModel): NetworkResult<ArtworkModel> {
        // ✅ Validate artwork before creation
        if (!artwork.isValid()) {
            return NetworkResult.Error("Invalid artwork data: missing required fields")
        }

        // ✅ Ensure artworkId is not empty, generate if needed
        val artworkId = artwork.artworkId.ifEmpty {
            "artwork_${System.currentTimeMillis()}_${(1000..9999).random()}"
        }

        // ✅ Create artwork with validated ID
        val updatedArtwork = artwork.copy(artworkId = artworkId)

        return when (val result = dbService.saveData(artworksNode, artworkId, updatedArtwork.toMap())) {
            is NetworkResult.Success -> NetworkResult.Success(updatedArtwork)
            is NetworkResult.Error -> NetworkResult.Error("Failed to create artwork: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getArtworkById(artworkId: String): NetworkResult<ArtworkModel?> {
        if (artworkId.isBlank()) {
            return NetworkResult.Error("Artwork ID cannot be empty")
        }

        return when (val result = dbService.getData(artworksNode, artworkId, ArtworkModel::class.java)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve artwork: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun updateArtwork(artwork: ArtworkModel): NetworkResult<ArtworkModel> {
        // ✅ Validate artwork data
        if (!artwork.isValid()) {
            return NetworkResult.Error("Invalid artwork data: missing required fields")
        }

        val artworkId = artwork.artworkId.ifEmpty {
            return NetworkResult.Error("Invalid artwork ID")
        }

        return when (val result = dbService.updateData(artworksNode, artworkId, artwork.toMap())) {
            is NetworkResult.Success -> NetworkResult.Success(artwork)
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
            is NetworkResult.Success -> NetworkResult.Success(result.data ?: emptyList())
            is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve artist artworks: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getAllPublicArtworks(): NetworkResult<List<ArtworkModel>> {
        return when (val result = dbService.queryData(artworksNode, "isPublic", "true", ArtworkModel::class.java)) {
            is NetworkResult.Success -> {
                // ✅ Sort by upload date (newest first)
                val sortedArtworks = result.data?.sortedByDescending { it.uploadedAt } ?: emptyList()
                NetworkResult.Success(sortedArtworks)
            }
            is NetworkResult.Error -> NetworkResult.Error("Failed to retrieve public artworks: ${result.message}")
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun searchArtworks(query: String): NetworkResult<List<ArtworkModel>> {
        if (query.isBlank()) {
            return NetworkResult.Success(emptyList())
        }

        // ✅ Enhanced search: check both title and tags
        return when (val result = getAllPublicArtworks()) {
            is NetworkResult.Success -> {
                val filteredArtworks = result.data?.filter { artwork ->
                    artwork.title.contains(query, ignoreCase = true) ||
                            artwork.description.contains(query, ignoreCase = true) ||
                            artwork.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                } ?: emptyList() // ✅ Handle null case with empty list

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

        val updates = mapOf("isPublic" to isPublic)

        return when (val updateResult = dbService.updateData(artworksNode, artworkId, updates)) {
            is NetworkResult.Success -> {
                // ✅ Retrieve updated artwork to return
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
}
