package com.soniq.music.domain.repository

import com.soniq.music.core.common.AppResult
import com.soniq.music.domain.model.ProfileUpdateRequest
import com.soniq.music.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain boundary for reading and updating the signed-in user's SONIQ profile.
 * Profile mutations only modify permitted profile fields and never overwrite Auth-managed email.
 */
interface ProfileRepository {
    val currentProfile: StateFlow<UserProfile?>

    suspend fun loadProfile(
        userId: String,
        fallbackEmail: String,
        fallbackDisplayName: String? = null
    ): AppResult<UserProfile>

    suspend fun updateProfile(request: ProfileUpdateRequest): AppResult<UserProfile>
    fun clearCachedProfile()
}
