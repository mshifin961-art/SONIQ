package com.soniq.music.data.profile

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.common.DispatcherProvider
import com.soniq.music.domain.model.ProfileUpdateRequest
import com.soniq.music.network.dto.UserProfileDto
import com.soniq.music.network.dto.UserProfileUpdateDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupabaseProfileRepositoryTest {

    private fun testDispatchers(dispatcher: CoroutineDispatcher): DispatcherProvider =
        object : DispatcherProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val default: CoroutineDispatcher = dispatcher
            override val main: CoroutineDispatcher = dispatcher
        }

    @Test
    fun `missing profile row provisions safely via server-side ensureOwnProfile RPC`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fakeRemote = FakeProfileRemoteDataSource(
            storedProfile = null,
            rpcProvisionedProfile = UserProfileDto(
                id = "u-1",
                email = "auth-verified@soniq.music",
                displayName = "Shifin"
            )
        )
        val repo = SupabaseProfileRepository(fakeRemote, testDispatchers(dispatcher))

        val loaded = repo.loadProfile("u-1", fallbackEmail = "auth-verified@soniq.music")
        assertTrue(loaded.isSuccess)
        assertEquals(1, fakeRemote.ensureOwnProfileCalls)
        assertEquals("auth-verified@soniq.music", loaded.getOrNull()?.email)
    }

    @Test
    fun `updateProfile updates only permitted fields and preserves Auth-managed email`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fakeRemote = FakeProfileRemoteDataSource(
            storedProfile = UserProfileDto(
                id = "u-1",
                email = "auth-owner@soniq.music",
                username = "oldname",
                displayName = "Old Name",
                bio = "Old bio"
            )
        )
        val repo = SupabaseProfileRepository(fakeRemote, testDispatchers(dispatcher))

        val loaded = repo.loadProfile("u-1", "auth-owner@soniq.music")
        assertTrue(loaded.isSuccess)
        assertEquals("auth-owner@soniq.music", loaded.getOrNull()?.email)

        val updated = repo.updateProfile(
            ProfileUpdateRequest(
                userId = "u-1",
                displayName = "Shifin",
                username = "SHIFIN",
                bio = "Lossless audio enthusiast",
                avatarUrl = "https://example.com/avatar.png"
            )
        )

        assertTrue(updated.isSuccess)
        assertEquals("shifin", fakeRemote.lastUpdatePayload?.username)
        assertEquals("Shifin", fakeRemote.lastUpdatePayload?.displayName)
        assertEquals("auth-owner@soniq.music", updated.getOrNull()?.email)
        assertEquals("auth-owner@soniq.music", fakeRemote.storedProfile?.email)
    }

    @Test
    fun `loadProfile synchronizes authoritative Auth email when DB copy lags`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fakeRemote = FakeProfileRemoteDataSource(
            storedProfile = UserProfileDto(
                id = "u-1",
                email = "stale@soniq.music",
                displayName = "Shifin"
            )
        )
        val repo = SupabaseProfileRepository(fakeRemote, testDispatchers(dispatcher))

        val result = repo.loadProfile("u-1", fallbackEmail = "confirmed@soniq.music")
        assertTrue(result.isSuccess)
        assertEquals("confirmed@soniq.music", result.getOrNull()?.email)
    }

    @Test
    fun `clearCachedProfile empties cached profile flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fakeRemote = FakeProfileRemoteDataSource(
            storedProfile = UserProfileDto(id = "u-1", email = "a@soniq.music", displayName = "A")
        )
        val repo = SupabaseProfileRepository(fakeRemote, testDispatchers(dispatcher))

        repo.loadProfile("u-1", "a@soniq.music")
        assertEquals("u-1", repo.currentProfile.value?.id)

        repo.clearCachedProfile()
        assertNull(repo.currentProfile.value)
    }

    @Test
    fun `update failure propagates as AppResult Error`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fakeRemote = FakeProfileRemoteDataSource(
            storedProfile = UserProfileDto(id = "u-1", email = "a@soniq.music", displayName = "A"),
            failUpdateWith = IllegalStateException("duplicate key value violates unique constraint idx_profiles_username_lower_unique")
        )
        val repo = SupabaseProfileRepository(fakeRemote, testDispatchers(dispatcher))

        val res = repo.updateProfile(
            ProfileUpdateRequest("u-1", "Shifin", "shifin", "", null)
        )
        assertTrue(res is AppResult.Error)
        assertTrue(res.errorOrNull() is AppError)
    }

    private class FakeProfileRemoteDataSource(
        var storedProfile: UserProfileDto? = null,
        private val rpcProvisionedProfile: UserProfileDto? = null,
        private val failUpdateWith: Throwable? = null
    ) : ProfileRemoteDataSource {
        override val isConfigured: Boolean = true
        var ensureOwnProfileCalls: Int = 0
        var lastUpdatePayload: UserProfileUpdateDto? = null

        override suspend fun fetchProfile(userId: String): UserProfileDto? = storedProfile

        override suspend fun ensureOwnProfile(): UserProfileDto? {
            ensureOwnProfileCalls++
            storedProfile = rpcProvisionedProfile
            return rpcProvisionedProfile
        }

        override suspend fun updatePermittedProfileFields(
            userId: String,
            payload: UserProfileUpdateDto
        ) {
            failUpdateWith?.let { throw it }
            lastUpdatePayload = payload
            storedProfile = storedProfile?.copy(
                username = payload.username,
                displayName = payload.displayName,
                avatarUrl = payload.avatarUrl,
                bio = payload.bio
            )
        }
    }
}
