package com.example.inkspira_adigitalartportfolio.model.data

data class UserModel(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.VIEWER,
    val profileImageUrl: String = "",
    val createdAt: Long = 0L
) {
    // Helper method to check if user can create artwork
    fun canCreateArtwork(): Boolean {
        return role == UserRole.ARTIST || role == UserRole.BOTH
    }

    // Helper method to check if user can browse artwork
    fun canBrowseArtwork(): Boolean {
        return true // All users can browse
    }

    // Helper method to get role display name
    fun getRoleDisplayName(): String {
        return when (role) {
            UserRole.ARTIST -> "Artist"
            UserRole.VIEWER -> "Viewer"
            UserRole.BOTH -> "Artist & Viewer"
        }
    }
}