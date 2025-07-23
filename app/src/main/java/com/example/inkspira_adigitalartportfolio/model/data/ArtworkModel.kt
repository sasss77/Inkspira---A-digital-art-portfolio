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
    // Helper method to get formatted upload date
    fun getFormattedDate(): String {
        return if (uploadedAt > 0) {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(uploadedAt))
        } else {
            "Unknown"
        }
    }

    // Helper method to get tags as comma-separated string
    fun getTagsString(): String {
        return tags.joinToString(", ")
    }

    // Helper method to check if artwork has tags
    fun hasTags(): Boolean {
        return tags.isNotEmpty()
    }

    // Helper method to validate artwork data
    fun isValid(): Boolean {
        return title.isNotBlank() &&
                imageUrl.isNotBlank() &&
                artistId.isNotBlank()
    }
}