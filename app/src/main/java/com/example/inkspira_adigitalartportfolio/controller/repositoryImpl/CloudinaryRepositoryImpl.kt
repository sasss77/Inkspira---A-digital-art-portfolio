package com.example.inkspira_adigitalartportfolio.controller.repositoryImpl

import android.content.Context
import android.net.Uri
import com.example.inkspira_adigitalartportfolio.controller.remote.CloudinaryService
import com.example.inkspira_adigitalartportfolio.controller.repository.CloudinaryRepository
import com.example.inkspira_adigitalartportfolio.utils.ImageUtils
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

class CloudinaryRepositoryImpl(
    private val cloudinaryService: CloudinaryService
) : CloudinaryRepository {

    override suspend fun uploadImage(imageUri: Uri, context: Context): NetworkResult<String> {
        // Validate image before upload
        val validation = ImageUtils.validateImage(imageUri, context)
        if (!validation.isValid) {
            return NetworkResult.Error(validation.errorMessage)
        }

        return cloudinaryService.uploadImage(imageUri, context)
    }

    override suspend fun uploadImageWithFolder(
        imageUri: Uri,
        context: Context,
        folder: String
    ): NetworkResult<String> {
        // Validate image before upload
        val validation = ImageUtils.validateImage(imageUri, context)
        if (!validation.isValid) {
            return NetworkResult.Error(validation.errorMessage)
        }

        return cloudinaryService.uploadImage(imageUri, context, folder)
    }

    override fun getOptimizedImageUrl(imageUrl: String, width: Int, height: Int): String {
        return cloudinaryService.getOptimizedImageUrl(imageUrl, width, height)
    }

    override fun getThumbnailUrl(imageUrl: String, size: Int): String {
        return cloudinaryService.getThumbnailUrl(imageUrl, size)
    }

    // FIXED: deleteImage method
    override suspend fun deleteImage(publicId: String): NetworkResult<Boolean> {
        return try {
            // Since Cloudinary deletion should be server-side, return a placeholder implementation
            NetworkResult.Error("Image deletion should be handled server-side for security")
        } catch (e: Exception) {
            NetworkResult.Error("Delete operation failed: ${e.message}")
        }
    }

    // FIXED: extractPublicIdFromUrl method
    override fun extractPublicIdFromUrl(imageUrl: String): String? {
        return try {
            cloudinaryService.extractPublicIdFromUrl(imageUrl)
        } catch (e: Exception) {
            null
        }
    }

    override fun isCloudinaryConfigured(): Boolean {
        return try {
            cloudinaryService.isCloudinaryConfigured()
        } catch (e: Exception) {
            false
        }
    }
}
