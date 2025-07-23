package com.example.inkspira_adigitalartportfolio.utils



import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {

    // Email validation
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Password validation
    fun isValidPassword(password: String): Boolean {
        return password.length >= Constants.MIN_PASSWORD_LENGTH
    }

    // Display name validation
    fun isValidDisplayName(displayName: String): Boolean {
        return displayName.isNotBlank() &&
                displayName.length <= Constants.MAX_DISPLAY_NAME_LENGTH &&
                displayName.trim().length >= 2
    }

    // Artwork title validation
    fun isValidArtworkTitle(title: String): Boolean {
        return title.isNotBlank() &&
                title.length <= Constants.MAX_TITLE_LENGTH &&
                title.trim().isNotEmpty()
    }

    // Artwork description validation
    fun isValidArtworkDescription(description: String): Boolean {
        return description.isNotBlank() &&
                description.length <= Constants.MAX_DESCRIPTION_LENGTH
    }

    // Tag validation
    fun isValidTag(tag: String): Boolean {
        return tag.isNotBlank() &&
                tag.length <= Constants.MAX_TAG_LENGTH &&
                tag.matches(Regex("^[a-zA-Z0-9\\s]+$")) // Only alphanumeric and spaces
    }

    // Tags list validation
    fun isValidTagsList(tags: List<String>): Boolean {
        return tags.size <= Constants.MAX_TAGS_COUNT &&
                tags.all { isValidTag(it) } &&
                tags.distinct().size == tags.size // No duplicates
    }

    // General string validation
    fun isNotBlankAndNotEmpty(text: String): Boolean {
        return text.isNotBlank() && text.trim().isNotEmpty()
    }

    // URL validation (for image URLs)
    fun isValidUrl(url: String): Boolean {
        return try {
            val urlPattern = Pattern.compile(
                "^(https?://)?" + // Optional protocol
                        "([\\w.-]+)" + // Domain
                        "(\\.[a-zA-Z]{2,})?" + // Optional TLD
                        "(/.*)?$" // Optional path
            )
            url.isNotBlank() && urlPattern.matcher(url).matches()
        } catch (e: Exception) {
            false
        }
    }

    // Sanitize user input
    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .take(500) // Limit length for safety
    }

    // Validation result wrapper
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = ""
    ) {
        companion object {
            fun valid(): ValidationResult = ValidationResult(true)
            fun invalid(message: String): ValidationResult = ValidationResult(false, message)
        }
    }

    // Comprehensive user registration validation
    fun validateUserRegistration(
        email: String,
        password: String,
        displayName: String
    ): ValidationResult {
        return when {
            !isValidEmail(email) -> ValidationResult.invalid(Constants.ERROR_INVALID_EMAIL)
            !isValidPassword(password) -> ValidationResult.invalid(Constants.ERROR_WEAK_PASSWORD)
            !isValidDisplayName(displayName) -> ValidationResult.invalid("Display name must be 2-30 characters long")
            else -> ValidationResult.valid()
        }
    }

    // Comprehensive artwork validation
    fun validateArtwork(
        title: String,
        description: String,
        tags: List<String>
    ): ValidationResult {
        return when {
            !isValidArtworkTitle(title) -> ValidationResult.invalid(Constants.ERROR_EMPTY_TITLE)
            !isValidArtworkDescription(description) -> ValidationResult.invalid(Constants.ERROR_EMPTY_DESCRIPTION)
            !isValidTagsList(tags) -> ValidationResult.invalid("Invalid tags. Max ${Constants.MAX_TAGS_COUNT} tags allowed.")
            else -> ValidationResult.valid()
        }
    }
}
