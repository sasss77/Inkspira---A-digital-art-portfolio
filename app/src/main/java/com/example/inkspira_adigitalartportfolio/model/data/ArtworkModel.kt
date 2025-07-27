package com.example.inkspira_adigitalartportfolio.model.data

data class ArtworkModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val artistId: String = "",
    val artistUsername: String = "",

    val isPublic: Boolean = true,
    val uploadedAt: Long = 0L,

    val likesCount: Int = 0,

    val commentsCount: Int = 0
) {
    // Helper methods
    fun getDisplayThumbnail(): String {
        return thumbnailUrl.ifEmpty { imageUrl }
    }


    fun getFormattedUploadDate(): String {
        val date = java.util.Date(uploadedAt)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
