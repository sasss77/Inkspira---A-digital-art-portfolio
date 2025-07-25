package com.example.inkspira_adigitalartportfolio.utils


object Constants {
    // Firebase Realtime Database node names
    const val USERS_NODE = "users"
    const val ARTWORKS_NODE = "artworks"
    const val FAVORITES_NODE = "favorites"

    // App Configuration
    const val APP_NAME = "Inkspira"
    const val DEFAULT_PROFILE_IMAGE = ""

    // Validation Limits
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_TITLE_LENGTH = 50
    const val MAX_DESCRIPTION_LENGTH = 500
    const val MAX_DISPLAY_NAME_LENGTH = 30
    const val MAX_TAGS_COUNT = 10
    const val MAX_TAG_LENGTH = 20

    // Image Configuration
    const val MAX_IMAGE_SIZE_MB = 5
    const val MAX_IMAGE_SIZE_BYTES = MAX_IMAGE_SIZE_MB * 1024 * 1024L
    const val IMAGE_QUALITY = 80
    const val THUMBNAIL_SIZE = 300

    // Cloudinary Configuration
    const val CLOUDINARY_UPLOAD_PRESET = "inkspira_preset"
    const val CLOUDINARY_FOLDER = "inkspira_artworks"

    // User Roles
    const val ROLE_ARTIST = "ARTIST"
    const val ROLE_VIEWER = "VIEWER"
    const val ROLE_BOTH = "BOTH"




    // Error Messages
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_UNKNOWN = "Something went wrong. Please try again."
    const val ERROR_INVALID_EMAIL = "Please enter a valid email address."
    const val ERROR_WEAK_PASSWORD = "Password must be at least 6 characters long."
    const val ERROR_EMPTY_TITLE = "Artwork title cannot be empty."
    const val ERROR_EMPTY_DESCRIPTION = "Please add a description for your artwork."
    const val ERROR_IMAGE_TOO_LARGE = "Image size must be less than 5MB."
    const val ERROR_INVALID_IMAGE_FORMAT = "Please select a valid image format (JPG, PNG, WEBP)."

    // Success Messages
    const val SUCCESS_ARTWORK_UPLOADED = "Artwork uploaded successfully!"
    const val SUCCESS_PROFILE_UPDATED = "Profile updated successfully!"
    const val SUCCESS_ARTWORK_DELETED = "Artwork deleted successfully!"
    const val SUCCESS_ADDED_TO_FAVORITES = "Added to favorites!"
    const val SUCCESS_REMOVED_FROM_FAVORITES = "Removed from favorites!"
}
