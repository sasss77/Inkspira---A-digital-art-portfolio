package com.example.inkspira_adigitalartportfolio.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class ImageUtils(
    private val activity: Activity,
    private val registryOwner: ActivityResultRegistryOwner
) {
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var onImageSelectedCallback: ((Uri?) -> Unit)? = null

    fun registerLaunchers(onImageSelected: (Uri?) -> Unit) {
        onImageSelectedCallback = onImageSelected

        // Register for selecting image from gallery
        galleryLauncher = registryOwner.activityResultRegistry.register(
            "galleryLauncher", ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = result.data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                onImageSelectedCallback?.invoke(uri)
            } else {
                Log.e("ImageUtils", "Image selection cancelled or failed")
            }
        }

        // Register permission request
        permissionLauncher = registryOwner.activityResultRegistry.register(
            "permissionLauncher", ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Log.e("ImageUtils", "Permission denied")
            }
        }
    }

    fun launchImagePicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(permission)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
        galleryLauncher.launch(intent)
    }

    companion object {
        // Supported image formats
        private val SUPPORTED_FORMATS = listOf("jpg", "jpeg", "png", "webp")

        // Image format validation
        fun isValidImageFormat(fileName: String): Boolean {
            val extension = getFileExtension(fileName)
            return extension.lowercase() in SUPPORTED_FORMATS
        }

        // Image format validation using URI
        fun isValidImageFormat(uri: Uri, context: Context): Boolean {
            val mimeType = context.contentResolver.getType(uri)
            return mimeType?.startsWith("image/") == true
        }

        // Get file extension from filename
        private fun getFileExtension(fileName: String): String {
            return fileName.substringAfterLast('.', "")
        }

        // Image size validation
        fun isValidImageSize(fileSize: Long): Boolean {
            val maxSizeBytes = 5 * 1024 * 1024L // 5MB
            return fileSize <= maxSizeBytes
        }

        // Calculate image file size from URI
        fun calculateImageSize(uri: Uri, context: Context): Long {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.available()?.toLong() ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

        // Get file extension from URI
        fun getImageFileExtension(uri: Uri, context: Context): String? {
            val mimeType = context.contentResolver.getType(uri)
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        }

        // Validation result for images
        data class ImageValidationResult(
            val isValid: Boolean,
            val errorMessage: String = ""
        ) {
            companion object {
                fun valid(): ImageValidationResult = ImageValidationResult(true)
                fun invalid(message: String): ImageValidationResult = ImageValidationResult(false, message)
            }
        }

        // Comprehensive image validation
        fun validateImage(uri: Uri, context: Context): ImageValidationResult {
            return when {
                !isValidImageFormat(uri, context) -> ImageValidationResult.invalid("Please select a valid image format (JPG, PNG, WEBP)")
                !isValidImageSize(calculateImageSize(uri, context)) -> ImageValidationResult.invalid("Image size must be less than 5MB")
                else -> ImageValidationResult.valid()
            }
        }
    }
}
