package com.soniq.music.domain.model

/**
 * Explicitly scoped mutable profile fields permitted for client profile updates.
 * Authenticated email is intentionally excluded and managed exclusively via Supabase Auth.
 */
data class ProfileUpdateRequest(
    val userId: String,
    val displayName: String,
    val username: String,
    val bio: String,
    val avatarUrl: String?
)
