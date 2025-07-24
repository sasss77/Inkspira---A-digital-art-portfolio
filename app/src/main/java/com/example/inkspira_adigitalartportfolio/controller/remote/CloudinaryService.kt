package com.example.inkspira_adigitalartportfolio.controller.remote

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CloudinaryService {

    // Initialize Cloudinary
    fun initializeCloudinary(context: Context, cloudName: String, apiKey: String, apiSecret: String) {
        val config = mapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret
        )
        MediaManager.init(context, config)
    }

    // Upload image with full callback support
    suspend fun uploadImage(
        imageUri: Uri,
        context: Context,
        folder: String = "inkspira_artworks"
    ): NetworkResult<String> = suspendCancellableCoroutine { continuation ->

        try {
            val requestId = MediaManager.get().upload(imageUri)
                .options(mapOf(
                    "folder" to folder,
                    "resource_type" to "image",
                    "quality" to "auto",
                    "fetch_format" to "auto"
                ))
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        // Upload started
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // Upload progress tracking (optional implementation)
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as? String
                        if (imageUrl != null) {
                            continuation.resume(NetworkResult.Success(imageUrl))
                        } else {
                            continuation.resume(NetworkResult.Error("Failed to get image URL"))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resume(NetworkResult.Error("Upload failed: ${error.description}"))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resume(NetworkResult.Error("Upload rescheduled: ${error.description}"))
                    }
                })
                .dispatch()

            // Handle cancellation properly
            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }

        } catch (e: Exception) {
            continuation.resume(NetworkResult.Error("Upload initialization failed: ${e.message}"))
        }
    }

    // Advanced URL transformation using string-based approach
    fun getOptimizedImageUrl(imageUrl: String, width: Int = 400, height: Int = 400): String {
        return try {
            if (imageUrl.contains("cloudinary.com")) {
                // Insert transformation parameters
                val parts = imageUrl.split("/upload/")
                if (parts.size == 2) {
                    "${parts[0]}/upload/w_${width},h_${height},c_fill,q_auto,f_auto/${parts[1]}"
                } else {
                    imageUrl
                }
            } else {
                imageUrl
            }
        } catch (e: Exception) {
            imageUrl
        }
    }

    // Generate thumbnail URLs
    fun getThumbnailUrl(imageUrl: String, size: Int = 300): String {
        return getOptimizedImageUrl(imageUrl, size, size)
    }

    // Extract public ID from Cloudinary URL
    fun extractPublicIdFromUrl(imageUrl: String): String? {
        return try {
            if (imageUrl.contains("cloudinary.com")) {
                // Enhanced regex pattern for better extraction
                val regex = """.*/v\d+/(.+)\.[a-zA-Z]+$""".toRegex()
                val matchResult = regex.find(imageUrl)
                matchResult?.groupValues?.get(1)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Delete image (placeholder for server-side implementation)
    suspend fun deleteImage(publicId: String): NetworkResult<Boolean> {
        return NetworkResult.Error("Image deletion should be handled server-side for security reasons")
    }

    // Check MediaManager initialization
    fun isCloudinaryConfigured(): Boolean {
        return try {
            MediaManager.get() != null
        } catch (e: Exception) {
            false
        }
    }

    // Get image details from upload result
    fun getImageDetails(resultData: Map<*, *>): Map<String, Any?> {
        return mapOf(
            "public_id" to resultData["public_id"],
            "secure_url" to resultData["secure_url"],
            "width" to resultData["width"],
            "height" to resultData["height"],
            "format" to resultData["format"],
            "bytes" to resultData["bytes"]
        )
    }
}
