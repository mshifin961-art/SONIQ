package com.soniq.music.domain.model

/**
 * Authenticated user identity exposed by the authentication layer.
 */
data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val isEmailConfirmed: Boolean = true
)
