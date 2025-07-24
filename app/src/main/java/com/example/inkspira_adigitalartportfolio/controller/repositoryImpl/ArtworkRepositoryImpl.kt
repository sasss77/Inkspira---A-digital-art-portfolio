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
        val artworkMap = mapOf(
            "artworkId" to artwork.artworkId,
            "artistId" to artwork.artistId,
            "title" to artwork.title,
            "description" to artwork.description,
            "imageUrl" to artwork.imageUrl,
            "tags" to artwork.tags,
            "uploadedAt" to artwork.uploadedAt,
            "isPublic" to artwork.isPublic
        )

        return when (val result = dbService.saveData(artworksNode, artwork.artworkId, artworkMap)) {
            is NetworkResult.Success -> NetworkResult.Success(artwork)
            is NetworkResult.Error -> NetworkResult.Error(result.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun getArtworkById(artworkId: String): NetworkResult<ArtworkModel?> {
        return dbService.getData(artworksNode, artworkId, ArtworkModel::class.java)
    }

    override suspend fun updateArtwork(artwork: ArtworkModel): NetworkResult<ArtworkModel> {
        val artworkMap = mapOf(
            "artworkId" to artwork.artworkId,
            "artistId" to artwork.artistId,
            "title" to artwork.title,
            "description" to artwork.description,
            "imageUrl" to artwork.imageUrl,
            "tags" to artwork.tags,
            "uploadedAt" to artwork.uploadedAt,
            "isPublic" to artwork.isPublic
        )

        return when (val result = dbService.updateData(artworksNode, artwork.artworkId, artworkMap)) {
            is NetworkResult.Success -> NetworkResult.Success(artwork)
            is NetworkResult.Error -> NetworkResult.Error(result.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }

    override suspend fun deleteArtwork(artworkId: String): NetworkResult<Boolean> {
        return dbService.deleteData(artworksNode, artworkId)
    }

    override suspend fun getArtworksByArtist(artistId: String): NetworkResult<List<ArtworkModel>> {
        return dbService.queryData(artworksNode, "artistId", artistId, ArtworkModel::class.java)
    }

    override suspend fun getAllPublicArtworks(): NetworkResult<List<ArtworkModel>> {
        return dbService.queryData(artworksNode, "isPublic", "true", ArtworkModel::class.java)
    }

    override suspend fun searchArtworks(query: String): NetworkResult<List<ArtworkModel>> {
        // For simplicity, search by title matching
        return dbService.queryData(artworksNode, "title", query, ArtworkModel::class.java)
    }

    override suspend fun updateArtworkVisibility(artworkId: String, isPublic: Boolean): NetworkResult<ArtworkModel> {
        val updates = mapOf("isPublic" to isPublic)

        return when (val updateResult = dbService.updateData(artworksNode, artworkId, updates)) {
            is NetworkResult.Success -> {
                when (val artworkResult = getArtworkById(artworkId)) {
                    is NetworkResult.Success -> {
                        if (artworkResult.data != null) {
                            NetworkResult.Success(artworkResult.data)
                        } else {
                            NetworkResult.Error("Artwork not found after update")
                        }
                    }
                    is NetworkResult.Error -> NetworkResult.Error(artworkResult.message)
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
            is NetworkResult.Error -> NetworkResult.Error(updateResult.message)
            is NetworkResult.Loading -> NetworkResult.Loading()
        }
    }
}
