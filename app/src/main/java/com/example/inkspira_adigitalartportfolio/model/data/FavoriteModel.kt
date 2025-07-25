package com.example.inkspira_adigitalartportfolio.model.data

data class FavoriteModel(
    val favoriteId: String = "",
    val userId: String = "",
    val artworkId: String = "",
    val addedAt: Long = 0L
) {
    // No-arg constructor for Firebase
    constructor() : this(
        favoriteId = "",
        userId = "",
        artworkId = "",
        addedAt = 0L
    )

    // Helper method to create map for Firebase storage
    fun toMap(): Map<String, Any> {
        return mapOf(
            "favoriteId" to favoriteId,
            "userId" to userId,
            "artworkId" to artworkId,
            "addedAt" to addedAt
        )
    }

    // Helper method to validate favorite data
    fun isValid(): Boolean {
        return favoriteId.isNotBlank() &&
                userId.isNotBlank() &&
                artworkId.isNotBlank()
    }
}
