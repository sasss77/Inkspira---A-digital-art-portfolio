package com.example.inkspira_adigitalartportfolio.controller.repository

import android.content.Context
import android.net.Uri
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult

interface CloudinaryRepository {

    // Image upload operations
    suspend fun uploadImage(imageUri: Uri, context: Context): NetworkResult<String>

    suspend fun uploadImageWithFolder(imageUri: Uri, context: Context, folder: String): NetworkResult<String>

    // Image URL operations
    fun getOptimizedImageUrl(imageUrl: String, width: Int = 400, height: Int = 400): String

    fun getThumbnailUrl(imageUrl: String, size: Int = 300): String

    // Image management
    suspend fun deleteImage(publicId: String): NetworkResult<Boolean>

    // Utility operations
    fun extractPublicIdFromUrl(imageUrl: String): String?

    fun isCloudinaryConfigured(): Boolean
}
