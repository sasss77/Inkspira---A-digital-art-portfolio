package com.example.inkspira_adigitalartportfolio.model.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FavoriteModel(
    val favoriteId: String = "",
    val userId: String = "",
    val artworkId: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    // Helper method to get formatted date when added to favorites
    fun getFormattedAddedDate(): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(addedAt))
    }

    // Helper method to validate favorite data
    fun isValid(): Boolean {
        return userId.isNotBlank() && artworkId.isNotBlank()
    }

    // Helper method to generate unique favorite ID
    companion object {
        fun generateId(userId: String, artworkId: String): String {
            return "${userId}_${artworkId}"
        }
    }
}