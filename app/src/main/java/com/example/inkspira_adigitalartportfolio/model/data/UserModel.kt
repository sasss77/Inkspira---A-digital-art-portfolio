import com.example.inkspira_adigitalartportfolio.model.data.UserRole

data class UserModel(
    val userId: String = "",        // ✅ Non-null String
    val email: String = "",         // ✅ Non-null String
    val displayName: String = "",   // ✅ Non-null String
    val role: UserRole = UserRole.VIEWER,
    val profileImageUrl: String = "",
    val createdAt: Long = 0L
) {
    // No-arg constructor for Firebase
    constructor() : this(
        userId = "",
        email = "",
        displayName = "",
        role = UserRole.VIEWER,
        profileImageUrl = "",
        createdAt = 0L
    )

    // Helper methods (your existing methods)
    fun canCreateArtwork(): Boolean {
        return role == UserRole.ARTIST || role == UserRole.BOTH
    }

    fun canBrowseArtwork(): Boolean {
        return true
    }

    fun getRoleDisplayName(): String {
        return when (role) {
            UserRole.ARTIST -> "Artist"
            UserRole.VIEWER -> "Viewer"
            UserRole.BOTH -> "Artist & Viewer"
        }
    }

    // ✅ Required toMap method for Firebase operations
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "role" to role.name,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to createdAt
        )
    }
}
