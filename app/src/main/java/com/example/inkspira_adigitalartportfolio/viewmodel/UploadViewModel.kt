package com.example.inkspira_adigitalartportfolio.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class UploadViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // ✅ FIXED: Proper StateFlow type declarations
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ✅ FIXED: Complete function with proper syntax
    fun uploadArtwork(
        context: Context,
        imageUri: Uri,
        title: String,
        description: String,
        category: String,
        tags: List<String>, // ✅ FIXED: Proper generic type
        isPublic: Boolean
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = 0f
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId == null) {
                    _errorMessage.value = "User not logged in"
                    _isUploading.value = false // ✅ FIXED: Reset loading state
                    return@launch
                }

                // Generate unique artwork ID
                val artworkId = UUID.randomUUID().toString()

                // Upload image to Cloudinary
                uploadImageToCloudinary(context, imageUri, artworkId) { imageUrl ->
                    viewModelScope.launch {
                        try {
                            // ✅ FIXED: Complete artwork data structure
                            val artworkData = hashMapOf<String, Any>(
                                "id" to artworkId,
                                "title" to title,
                                "description" to description,
                                "imageUrl" to imageUrl,
                                "thumbnailUrl" to generateThumbnailUrl(imageUrl),
                                "category" to category,
                                "isPublic" to isPublic,
                                "artistId" to userId,
                                "artistUsername" to (firebaseAuth.currentUser?.displayName ?: "Unknown"),
                                "likesCount" to 0,
                                "viewsCount" to 0,
                                "commentsCount" to 0,
                                "uploadedAt" to System.currentTimeMillis(),
                                "updatedAt" to System.currentTimeMillis()
                            )

                            database.getReference("artworks").child(artworkId).setValue(artworkData).await()
                            updateUserArtworkCount(userId)
                            _successMessage.value = "Artwork uploaded successfully!"

                        } catch (e: Exception) {
                            _errorMessage.value = "Failed to save artwork: ${e.message}"
                            println("Database save error: ${e.message}")
                        } finally {
                            _isUploading.value = false
                            _uploadProgress.value = 0f
                        }
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Upload failed: ${e.message}"
                _isUploading.value = false
                _uploadProgress.value = 0f
                println("Upload error: ${e.message}")
            }
        }
    }

    // ✅ FIXED: Unsigned upload compliant (no transformations)
    private fun uploadImageToCloudinary(
        context: Context,
        imageUri: Uri,
        artworkId: String,
        onSuccess: (String) -> Unit
    ) {
        try {
            if (!isCloudinaryInitialized()) {
                _errorMessage.value = "Cloudinary not properly configured"
                _isUploading.value = false
                return
            }

            MediaManager.get().upload(imageUri)
                .unsigned("inkspira_unsigned") // Your upload preset name
                .option("public_id", "artworks/$artworkId")
                .option("folder", "inkspira/artworks")
                .option("tags", "artwork,user_upload") // ✅ ALLOWED: Tags in unsigned upload
                // ✅ REMOVED: All transformation options (not allowed in unsigned upload)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        _uploadProgress.value = 0.1f
                        println("Cloudinary upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes.toFloat() / totalBytes.toFloat()) * 0.8f + 0.1f
                        _uploadProgress.value = progress
                        println("Upload progress: ${(progress * 100).toInt()}%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as? String
                        if (imageUrl != null) {
                            _uploadProgress.value = 0.9f
                            onSuccess(imageUrl)
                            println("Cloudinary upload successful: $imageUrl")
                        } else {
                            _errorMessage.value = "Failed to get image URL from Cloudinary"
                            _isUploading.value = false
                            println("Cloudinary success but no URL received")
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        val errorMsg = "Upload failed: ${error.description}"
                        _errorMessage.value = errorMsg
                        _isUploading.value = false
                        _uploadProgress.value = 0f
                        println("Cloudinary error: $errorMsg")
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        _uploadProgress.value = 0f
                        println("Upload rescheduled: ${error.description}")
                    }
                })
                .dispatch()

        } catch (e: Exception) {
            val errorMsg = "Failed to start upload: ${e.message}"
            _errorMessage.value = errorMsg
            _isUploading.value = false
            _uploadProgress.value = 0f
            println("Cloudinary init error: $errorMsg")
        }
    }

    // ✅ FIXED: Complete function implementation
    private fun isCloudinaryInitialized(): Boolean {
        return try {
            MediaManager.get() != null
        } catch (e: Exception) {
            false
        }
    }

    // ✅ FIXED: URL-based thumbnail generation (works with unsigned upload)
    private fun generateThumbnailUrl(originalUrl: String): String {
        return originalUrl.replace("/upload/", "/upload/w_400,h_400,c_fill,q_auto:good/")
    }

    // ✅ NEW: Generate optimized image URLs for different use cases
    fun getOptimizedImageUrl(originalUrl: String, width: Int, height: Int, quality: String = "auto:good"): String {
        return originalUrl.replace("/upload/", "/upload/w_${width},h_${height},c_fill,q_${quality}/")
    }

    // ✅ FIXED: Complete function implementation
    private suspend fun updateUserArtworkCount(userId: String) {
        try {
            val userRef = database.getReference("users").child(userId)
            val snapshot = userRef.child("artworkCount").get().await()
            val currentCount = (snapshot.value as? Long)?.toInt() ?: 0
            userRef.child("artworkCount").setValue(currentCount + 1).await()
            println("User artwork count updated: ${currentCount + 1}")
        } catch (e: Exception) {
            // Log error but don't fail the upload
            println("Failed to update artwork count: ${e.message}")
        }
    }

    // ✅ FIXED: Complete function implementation
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // ✅ NEW: Additional utility functions
    fun resetUploadState() {
        _isUploading.value = false
        _uploadProgress.value = 0f
        clearMessages()
    }

    fun getUploadStatus(): Triple<Boolean, Float, String?> {
        return Triple(
            _isUploading.value,
            _uploadProgress.value,
            _errorMessage.value
        )
    }
}
