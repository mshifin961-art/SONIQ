package com.soniq.music.network.dto

import com.soniq.music.domain.model.ProfileUpdateRequest
import com.soniq.music.domain.model.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Full PostgREST read/insert model for `public.profiles`.
 */
@Serializable
data class UserProfileDto(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String,
    @SerialName("username")
    val username: String? = null,
    @SerialName("display_name")
    val displayName: String = "",
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("bio")
    val bio: String = "",
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Strictly scoped PostgREST update payload containing ONLY permitted mutable profile fields.
 * Intentionally excludes `id` and `email` so ordinary profile updates can never overwrite
 * the Supabase Auth-managed email address.
 */
@Serializable
data class UserProfileUpdateDto(
    @SerialName("username")
    val username: String? = null,
    @SerialName("display_name")
    val displayName: String = "",
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("bio")
    val bio: String = ""
)

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    email = email,
    username = username.orEmpty().lowercase(),
    displayName = displayName,
    avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
    bio = bio,
    updatedAt = updatedAt
)

fun UserProfile.toDto(): UserProfileDto = UserProfileDto(
    id = id,
    email = email,
    username = username.trim().lowercase().takeIf { it.isNotEmpty() },
    displayName = displayName.trim(),
    avatarUrl = avatarUrl?.trim()?.takeIf { it.isNotEmpty() },
    bio = bio.trim(),
    updatedAt = updatedAt
)

fun ProfileUpdateRequest.toUpdateDto(): UserProfileUpdateDto = UserProfileUpdateDto(
    username = username.trim().lowercase().takeIf { it.isNotEmpty() },
    displayName = displayName.trim(),
    avatarUrl = avatarUrl?.trim()?.takeIf { it.isNotEmpty() },
    bio = bio.trim()
)
