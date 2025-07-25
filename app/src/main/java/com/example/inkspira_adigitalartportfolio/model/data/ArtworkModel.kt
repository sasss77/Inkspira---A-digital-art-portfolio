package com.example.inkspira_adigitalartportfolio.model.data

import com.google.firebase.database.PropertyName

data class ArtworkModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val artistId: String = "",

    // ✅ ADD THESE MISSING PROPERTIES
    val artistUsername: String = "",        // Artist's display name
    val thumbnailUrl: String = "",          // Optimized thumbnail URL

    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val uploadedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", "", "", emptyList(), true, 0L, 0L)

    // ✅ ADD THESE HELPER METHODS
    fun getDisplayThumbnail(): String {
        return thumbnailUrl.ifEmpty { imageUrl }
    }

    fun getArtistDisplayName(): String {
        return if (artistUsername.isNotEmpty()) "@$artistUsername" else "Unknown Artist"
    }

    // Existing helper methods...
    fun isValid(): Boolean {
        return title.isNotEmpty() &&
                imageUrl.isNotEmpty() &&
                artistId.isNotEmpty() &&
                artistUsername.isNotEmpty()
    }

    fun canEdit(currentUserId: String): Boolean {
        return artistId == currentUserId
    }

    fun getRelativeTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - uploadedAt

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> "${diff / 604800_000} weeks ago"
        }
    }

    fun getFormattedDate(): String {
        val date = java.util.Date(uploadedAt)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    fun getTagsString(): String {
        return tags.joinToString(", ")
    }

    fun getHashtagString(): String {
        return tags.joinToString(" ") { "#$it" }
    }

    fun isRecentlyUploaded(): Boolean {
        val now = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return (now - uploadedAt) < oneDayInMillis
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "artistId" to artistId,
            "artistUsername" to artistUsername,    // ✅ Include in Firebase mapping
            "thumbnailUrl" to thumbnailUrl,        // ✅ Include in Firebase mapping
            "tags" to tags,
            "isPublic" to isPublic,
            "uploadedAt" to uploadedAt,
            "updatedAt" to updatedAt
        )
    }
}
