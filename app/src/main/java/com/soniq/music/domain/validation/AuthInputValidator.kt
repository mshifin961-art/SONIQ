package com.soniq.music.domain.validation

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult

/**
 * Client-side input validation for registration, sign-in, and profile editing.
 * Prevents malformed input from being submitted to Supabase.
 */
object AuthInputValidator {

    private val emailRegex = Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
    private val usernameRegex = Regex("^[a-zA-Z0-9._]{3,24}$")

    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_DISPLAY_NAME_LENGTH = 2
    const val MAX_DISPLAY_NAME_LENGTH = 40
    const val MAX_BIO_LENGTH = 160

    fun validateEmail(rawEmail: String): AppResult<String> {
        val normalized = rawEmail.trim().lowercase()
        if (normalized.isEmpty()) {
            return AppResult.Error(AppError.Validation("Email address is required.", field = "email"))
        }
        if (!emailRegex.matches(normalized)) {
            return AppResult.Error(AppError.Validation("Enter a valid email address.", field = "email"))
        }
        return AppResult.Success(normalized)
    }

    fun validateLoginPassword(password: String): AppResult<String> {
        if (password.isBlank()) {
            return AppResult.Error(AppError.Validation("Password is required.", field = "password"))
        }
        return AppResult.Success(password)
    }

    fun validateRegistrationPassword(password: String): AppResult<String> {
        if (password.length < MIN_PASSWORD_LENGTH) {
            return AppResult.Error(
                AppError.Validation(
                    "Password must be at least $MIN_PASSWORD_LENGTH characters.",
                    field = "password"
                )
            )
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return AppResult.Error(
                AppError.Validation(
                    "Password must include at least one letter and one number.",
                    field = "password"
                )
            )
        }
        return AppResult.Success(password)
    }

    fun validateDisplayName(rawName: String): AppResult<String> {
        val trimmed = rawName.trim()
        if (trimmed.length < MIN_DISPLAY_NAME_LENGTH) {
            return AppResult.Error(
                AppError.Validation(
                    "Display name must be at least $MIN_DISPLAY_NAME_LENGTH characters.",
                    field = "displayName"
                )
            )
        }
        if (trimmed.length > MAX_DISPLAY_NAME_LENGTH) {
            return AppResult.Error(
                AppError.Validation(
                    "Display name cannot exceed $MAX_DISPLAY_NAME_LENGTH characters.",
                    field = "displayName"
                )
            )
        }
        return AppResult.Success(trimmed)
    }

    fun validateUsernameOptional(rawUsername: String): AppResult<String> {
        val trimmed = rawUsername.trim()
        if (trimmed.isEmpty()) return AppResult.Success("")
        if (!usernameRegex.matches(trimmed)) {
            return AppResult.Error(
                AppError.Validation(
                    "Username must be 3–24 characters using letters, numbers, dots, or underscores.",
                    field = "username"
                )
            )
        }
        return AppResult.Success(trimmed.lowercase())
    }

    fun validateBio(rawBio: String): AppResult<String> {
        val trimmed = rawBio.trim()
        if (trimmed.length > MAX_BIO_LENGTH) {
            return AppResult.Error(
                AppError.Validation(
                    "Bio cannot exceed $MAX_BIO_LENGTH characters.",
                    field = "bio"
                )
            )
        }
        return AppResult.Success(trimmed)
    }

    fun validateAvatarUrlOptional(rawUrl: String): AppResult<String?> {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return AppResult.Success(null)
        if (!trimmed.startsWith("https://", ignoreCase = true) || trimmed.length <= "https://".length) {
            return AppResult.Error(
                AppError.Validation(
                    "Avatar URL must be a valid https:// link.",
                    field = "avatarUrl"
                )
            )
        }
        return AppResult.Success(trimmed)
    }
}
