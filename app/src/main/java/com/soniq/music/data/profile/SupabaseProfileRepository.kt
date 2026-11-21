package com.soniq.music.data.profile

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.common.DispatcherProvider
import com.soniq.music.core.common.runCatchingResult
import com.soniq.music.domain.model.ProfileUpdateRequest
import com.soniq.music.domain.model.UserProfile
import com.soniq.music.domain.repository.ProfileRepository
import com.soniq.music.network.AuthErrorMapper
import com.soniq.music.network.dto.toDomain
import com.soniq.music.network.dto.toUpdateDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PostgREST-backed [ProfileRepository] synced with `public.profiles` under RLS.
 * Never performs client-side INSERT with client-supplied email; missing rows are
 * provisioned server-side via `ensure_own_profile()` RPC, and updates modify only
 * permitted [UserProfileUpdateDto] columns.
 */
@Singleton
class SupabaseProfileRepository @Inject constructor(
    private val remoteDataSource: ProfileRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : ProfileRepository {

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    override val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    override suspend fun loadProfile(
        userId: String,
        fallbackEmail: String,
        fallbackDisplayName: String?
    ): AppResult<UserProfile> = withContext(dispatchers.io) {
        if (!remoteDataSource.isConfigured) {
            return@withContext AppResult.Error(
                AppError.Configuration("Supabase is not configured.")
            )
        }

        runCatchingResult(errorMapper = AuthErrorMapper::map) {
            val dto = remoteDataSource.fetchProfile(userId)
                ?: remoteDataSource.ensureOwnProfile()
                ?: error("Profile could not be loaded for the authenticated user.")

            val domain = dto.toDomain()
            val resolved = if (fallbackEmail.isNotBlank() && domain.email != fallbackEmail) {
                domain.copy(email = fallbackEmail)
            } else {
                domain
            }

            _currentProfile.value = resolved
            resolved
        }
    }

    override suspend fun updateProfile(request: ProfileUpdateRequest): AppResult<UserProfile> =
        withContext(dispatchers.io) {
            if (!remoteDataSource.isConfigured) {
                return@withContext AppResult.Error(
                    AppError.Configuration("Supabase is not configured.")
                )
            }

            runCatchingResult(errorMapper = AuthErrorMapper::map) {
                val updatePayload = request.toUpdateDto()
                remoteDataSource.updatePermittedProfileFields(
                    userId = request.userId,
                    payload = updatePayload
                )

                val existingEmail = _currentProfile.value?.email.orEmpty()
                val refreshed = remoteDataSource.fetchProfile(request.userId)?.toDomain()
                    ?: UserProfile(
                        id = request.userId,
                        email = existingEmail,
                        username = updatePayload.username.orEmpty(),
                        displayName = updatePayload.displayName,
                        avatarUrl = updatePayload.avatarUrl,
                        bio = updatePayload.bio
                    )

                _currentProfile.value = refreshed
                refreshed
            }
        }

    override fun clearCachedProfile() {
        _currentProfile.value = null
    }
}
