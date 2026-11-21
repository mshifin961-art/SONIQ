package com.soniq.music.data.auth

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.common.DispatcherProvider
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.AuthUser
import com.soniq.music.domain.model.EmailUpdateOutcome
import com.soniq.music.domain.model.SignUpOutcome
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SupabaseAuthRepository] using a fake [AuthRemoteDataSource].
 * Verifies repository session state-machine transitions (restoration, refresh failure
 * resilience, expired session handling, logout, and Guest mode).
 *
 * Note: Real process-restart persistence via Android SharedPreferences (SettingsSessionManager)
 * requires on-device/emulator verification with a live Supabase project.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SupabaseAuthRepositoryTest {

    private fun testDispatchers(dispatcher: CoroutineDispatcher): DispatcherProvider =
        object : DispatcherProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val default: CoroutineDispatcher = dispatcher
            override val main: CoroutineDispatcher = dispatcher
        }

    @Test
    fun `unconfigured remote data source reports Unconfigured and returns Configuration error`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val fakeRemote = FakeAuthRemoteDataSource(isConfigured = false)
        val repo = SupabaseAuthRepository(fakeRemote, testDispatchers(dispatcher), scope)

        assertTrue(repo.sessionState.value is AuthSessionState.Unconfigured)

        val signInRes = repo.signIn("a@soniq.music", "Password1")
        assertTrue(signInRes is AppResult.Error && signInRes.error is AppError.Configuration)

        val emailRes = repo.updateEmail("b@soniq.music")
        assertTrue(emailRes is AppResult.Error && emailRes.error is AppError.Configuration)
    }

    @Test
    fun `persisted session restores automatically from Initializing to Authenticated`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val fakeRemote = FakeAuthRemoteDataSource(isConfigured = true)
        val repo = SupabaseAuthRepository(fakeRemote, testDispatchers(dispatcher), scope)
        advanceUntilIdle()

        assertTrue(repo.sessionState.value is AuthSessionState.Initializing)

        val restoredUser = AuthUser(id = "u-1", email = "shifin@soniq.music", displayName = "Shifin")
        fakeRemote.sessionEvents.emit(RemoteSessionEvent.Authenticated(restoredUser))
        advanceUntilIdle()

        val state = repo.sessionState.value
        assertTrue(state is AuthSessionState.Authenticated)
        assertEquals("shifin@soniq.music", (state as AuthSessionState.Authenticated).user.email)
    }

    @Test
    fun `temporary network RefreshFailure preserves Authenticated session and never calls signOut`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val fakeRemote = FakeAuthRemoteDataSource(isConfigured = true)
        val repo = SupabaseAuthRepository(fakeRemote, testDispatchers(dispatcher), scope)
        advanceUntilIdle()

        val user = AuthUser(id = "u-1", email = "shifin@soniq.music", displayName = "Shifin")
        fakeRemote.sessionEvents.emit(RemoteSessionEvent.Authenticated(user))
        advanceUntilIdle()
        assertTrue(repo.sessionState.value is AuthSessionState.Authenticated)

        fakeRemote.sessionEvents.emit(RemoteSessionEvent.RefreshFailure)
        advanceUntilIdle()

        assertTrue(repo.sessionState.value is AuthSessionState.Authenticated)
        assertEquals(0, fakeRemote.signOutCalls)
    }

    @Test
    fun `RefreshFailure during Initializing recovers cleanly when Authenticated arrives`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val fakeRemote = FakeAuthRemoteDataSource(isConfigured = true)
        val repo = SupabaseAuthRepository(fakeRemote, testDispatchers(dispatcher), scope)
        advanceUntilIdle()

        fakeRemote.sessionEvents.emit(RemoteSessionEvent.RefreshFailure)
        advanceUntilIdle()
        assertTrue(repo.sessionState.value is AuthSessionState.Unauthenticated)
        assertEquals(0, fakeRemote.signOutCalls)

        val recovered = AuthUser(id = "u-1", email = "shifin@soniq.music", displayName = "Shifin")
        fakeRemote.sessionEvents.emit(RemoteSessionEvent.Authenticated(recovered))
        advanceUntilIdle()
        assertTrue(repo.sessionState.value is AuthSessionState.Authenticated)
    }

    @Test
    fun `expired or invalid session transitions Authenticated safely to Unauthenticated`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val fakeRemote = FakeAuthRemoteDataSource(isConfigured = true)
        val repo = SupabaseAuthRepository(fakeRemote, testDispatchers(dispatcher), scope)
        advanceUntilIdle()

        val user = AuthUser(id = "u-1", email = "shifin@soniq.music", displayName = "Shifin")
        fakeRemote.sessionEvents.emit(RemoteSessionEvent.Authenticated(user))
        advanceUntilIdle()

        fakeRemote.sessionEvents.emit(RemoteSessionEvent.NotAuthenticated(isSignOut = false))
        advanceUntilIdle()

        assertTrue(repo.sessionState.value is AuthSessionState.Unauthenticated)
    }

    @Test
    fun `signIn, updateEmail, and signOut clear remote session and update state consistently`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val fakeRemote = FakeAuthRemoteDataSource(isConfigured = true)
        val repo = SupabaseAuthRepository(fakeRemote, testDispatchers(dispatcher), scope)
        advanceUntilIdle()

        val signInRes = repo.signIn("  Shifin@Soniq.Music ", "Password1")
        assertTrue(signInRes.isSuccess)
        assertEquals("shifin@soniq.music", fakeRemote.lastEmail)
        assertTrue(repo.sessionState.value is AuthSessionState.Authenticated)

        fakeRemote.emailUpdateOutcome = EmailUpdateOutcome.Updated("new@soniq.music")
        val updateRes = repo.updateEmail("  NEW@SONIQ.MUSIC ")
        assertTrue(updateRes.isSuccess)
        val updatedSession = repo.sessionState.value as AuthSessionState.Authenticated
        assertEquals("new@soniq.music", updatedSession.user.email)

        val logoutRes = repo.signOut()
        assertTrue(logoutRes.isSuccess)
        assertEquals(1, fakeRemote.signOutCalls)
        assertTrue(repo.sessionState.value is AuthSessionState.Unauthenticated)
    }

    @Test
    fun `guest mode persists across passive NotAuthenticated events`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val fakeRemote = FakeAuthRemoteDataSource(isConfigured = true)
        val repo = SupabaseAuthRepository(fakeRemote, testDispatchers(dispatcher), scope)
        advanceUntilIdle()

        repo.continueAsGuest()
        assertTrue(repo.sessionState.value is AuthSessionState.Guest)

        fakeRemote.sessionEvents.emit(RemoteSessionEvent.NotAuthenticated(isSignOut = false))
        advanceUntilIdle()
        assertTrue(repo.sessionState.value is AuthSessionState.Guest)
    }

    private class FakeAuthRemoteDataSource(
        override val isConfigured: Boolean
    ) : AuthRemoteDataSource {
        val sessionEvents = MutableSharedFlow<RemoteSessionEvent>()
        var lastEmail: String? = null
        var signOutCalls: Int = 0
        var emailUpdateOutcome: EmailUpdateOutcome = EmailUpdateOutcome.ConfirmationSent("pending@soniq.music")

        override fun observeSessionEvents(): Flow<RemoteSessionEvent> = sessionEvents

        override suspend fun signIn(email: String, password: String): AuthUser {
            lastEmail = email
            return AuthUser(id = "u-1", email = email, displayName = "Shifin")
        }

        override suspend fun signUp(
            email: String,
            password: String,
            displayName: String
        ): SignUpOutcome {
            lastEmail = email
            return SignUpOutcome.AwaitingEmailConfirmation(email)
        }

        override suspend fun updateEmail(newEmail: String): EmailUpdateOutcome {
            lastEmail = newEmail
            return emailUpdateOutcome
        }

        override suspend fun signOut() {
            signOutCalls++
        }

        override suspend fun refreshSession() = Unit
    }
}
