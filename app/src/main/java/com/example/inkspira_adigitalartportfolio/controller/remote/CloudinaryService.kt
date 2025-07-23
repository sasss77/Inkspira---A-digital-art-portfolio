package com.example.inkspira_adigitalartportfolio.controller.remote

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.inkspira_adigitalartportfolio.utils.Constants
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

    // Upload image - This works with 2.1.0
    suspend fun uploadImage(
        imageUri: Uri,
        context: Context,
        folder: String = "inkspira_artworks"
    ): NetworkResult<String> = suspendCancellableCoroutine { continuation ->

        try {
            val requestId = MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

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
                        continuation.resume(NetworkResult.Error("Upload rescheduled"))
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                // Cancellation support might be limited in 2.1.0
            }

        } catch (e: Exception) {
            continuation.resume(NetworkResult.Error("Upload failed: ${e.message}"))
        }
    }

    // Simplified URL generation for 2.1.0
    fun getOptimizedImageUrl(imageUrl: String, width: Int = 400, height: Int = 400): String {
        return try {
            // For version 2.1.0, you might need to manually construct URLs
            // or use the basic MediaManager.get().url() if available
            if (imageUrl.contains("cloudinary.com")) {
                // Insert transformation parameters into the URL manually
                val parts = imageUrl.split("/upload/")
                if (parts.size == 2) {
                    "${parts[0]}/upload/w_${width},h_${height},c_fill/${parts[1]}"
                } else {
                    imageUrl // Return original if manipulation fails
                }
            } else {
                imageUrl
            }
        } catch (e: Exception) {
            imageUrl
        }
    }

    // Simple thumbnail generation
    fun getThumbnailUrl(imageUrl: String, size: Int = 300): String {
        return getOptimizedImageUrl(imageUrl, size, size)
    }

    // Check if Cloudinary is configured
    fun isCloudinaryConfigured(): Boolean {
        return try {
            MediaManager.get() != null
        } catch (e: Exception) {
            false
        }
    }
}
