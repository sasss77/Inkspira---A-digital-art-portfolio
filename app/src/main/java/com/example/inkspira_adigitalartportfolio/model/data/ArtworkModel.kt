package com.example.inkspira_adigitalartportfolio.model.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ArtworkModel(
    val artworkId: String = "",
    val artistId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val tags: List<String> = emptyList(),
    val uploadedAt: Long = 0L,
    val isPublic: Boolean = true
) {
    // No-arg constructor for Firebase deserialization
    constructor() : this(
        artworkId = "",
        artistId = "",
        title = "",
        description = "",
        imageUrl = "",
        tags = emptyList(),
        uploadedAt = 0L,
        isPublic = true
    )

    // Helper method to get formatted upload date
    fun getFormattedDate(): String {
        return if (uploadedAt > 0) {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(uploadedAt))
        } else {
            "Unknown"
        }
    }

    // Helper method to get relative time (e.g., "2 hours ago")
    fun getRelativeTime(): String {
        if (uploadedAt <= 0) return "Unknown"

        val now = System.currentTimeMillis()
        val diff = now - uploadedAt

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
            else -> getFormattedDate()
        }
    }

    // Helper method to get tags as comma-separated string
    fun getTagsString(): String {
        return tags.joinToString(", ")
    }

    // Helper method to get tags with hashtag prefix
    fun getHashtagString(): String {
        return tags.joinToString(" ") { "#$it" }
    }

    // Helper method to check if artwork has tags
    fun hasTags(): Boolean {
        return tags.isNotEmpty()
    }

    // Helper method to validate artwork data
    fun isValid(): Boolean {
        return artworkId.isNotBlank() &&
                title.isNotBlank() &&
                imageUrl.isNotBlank() &&
                artistId.isNotBlank()
    }

    // Helper method to check if artwork is recently uploaded (within 24 hours)
    fun isRecentlyUploaded(): Boolean {
        val now = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return (now - uploadedAt) < oneDayInMillis
    }

    // Helper method to create map for Firebase storage
    fun toMap(): Map<String, Any> {
        return mapOf(
            "artworkId" to artworkId,
            "artistId" to artistId,
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "tags" to tags,
            "uploadedAt" to uploadedAt,
            "isPublic" to isPublic
        )
    }

    // Helper method to check if current user can edit (placeholder for future role checking)
    fun canEdit(currentUserId: String): Boolean {
        return artistId == currentUserId
    }
}
