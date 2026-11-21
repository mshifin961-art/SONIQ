package com.soniq.music.domain.model

/**
 * Editable SONIQ user profile synced with Supabase `public.profiles`.
 */
data class UserProfile(
    val id: String,
    val email: String,
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val bio: String = "",
    val updatedAt: String? = null
) {
    val effectiveDisplayName: String
        get() = displayName.trim().ifEmpty {
            username.trim().ifEmpty {
                email.substringBefore("@").ifEmpty { "SONIQ Listener" }
            }
        }

    val initials: String
        get() {
            val parts = effectiveDisplayName
                .split(" ", "_", ".")
                .filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "SQ"
            }
        }
}
