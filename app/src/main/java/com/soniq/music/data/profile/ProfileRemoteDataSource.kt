package com.soniq.music.data.profile

import com.soniq.music.network.SupabaseClientProvider
import com.soniq.music.network.dto.UserProfileDto
import com.soniq.music.network.dto.UserProfileUpdateDto
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin remote gateway around Supabase PostgREST `public.profiles`.
 * Clients never perform direct INSERT on `public.profiles`; missing profile rows
 * are provisioned server-side via the `ensure_own_profile` SECURITY DEFINER RPC.
 */
interface ProfileRemoteDataSource {
    val isConfigured: Boolean
    suspend fun fetchProfile(userId: String): UserProfileDto?
    suspend fun ensureOwnProfile(): UserProfileDto?
    suspend fun updatePermittedProfileFields(userId: String, payload: UserProfileUpdateDto)
}

@Singleton
class SupabaseProfileRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : ProfileRemoteDataSource {

    override val isConfigured: Boolean
        get() = clientProvider.isConfigured

    override suspend fun fetchProfile(userId: String): UserProfileDto? {
        val client = requireNotNull(clientProvider.client) { "Supabase is not configured." }
        return client.postgrest
            .from(PROFILES_TABLE)
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<UserProfileDto>()
    }

    override suspend fun ensureOwnProfile(): UserProfileDto? {
        val client = requireNotNull(clientProvider.client) { "Supabase is not configured." }
        return client.postgrest
            .rpc(ENSURE_OWN_PROFILE_RPC)
            .decodeSingleOrNull<UserProfileDto>()
    }

    override suspend fun updatePermittedProfileFields(
        userId: String,
        payload: UserProfileUpdateDto
    ) {
        val client = requireNotNull(clientProvider.client) { "Supabase is not configured." }
        client.postgrest
            .from(PROFILES_TABLE)
            .update(payload) {
                filter { eq("id", userId) }
            }
    }

    private companion object {
        const val PROFILES_TABLE = "profiles"
        const val ENSURE_OWN_PROFILE_RPC = "ensure_own_profile"
    }
}
