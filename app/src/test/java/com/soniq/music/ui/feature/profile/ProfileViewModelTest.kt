package com.soniq.music.ui.feature.profile

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.state.UiState
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.AuthUser
import com.soniq.music.domain.model.EmailUpdateOutcome
import com.soniq.music.domain.model.ProfileUpdateRequest
import com.soniq.music.domain.model.SignUpOutcome
import com.soniq.music.domain.model.UserProfile
import com.soniq.music.domain.repository.AuthRepository
import com.soniq.music.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `authenticated session loads user profile and updates permitted fields without altering email`() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepo(
            AuthSessionState.Authenticated(AuthUser(id = "u1", email = "aria@soniq.music", displayName = "Aria"))
        )
        val profileRepo = FakeProfileRepo()
        val viewModel = ProfileViewModel(authRepo, profileRepo)
        advanceUntilIdle()

        val loaded = viewModel.profileState.value
        assertTrue(loaded is UiState.Success)
        assertEquals("aria@soniq.music", loaded.dataOrNull()?.email)

        viewModel.startEditing()
        viewModel.onDisplayNameChanged("Aria Chen")
        viewModel.onUsernameChanged("Shifin")
        viewModel.onBioChanged("Hi-res ambient listener")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertFalse(viewModel.editState.value.isEditing)
        assertEquals("Aria Chen", viewModel.profileState.value.dataOrNull()?.displayName)
        assertEquals("shifin", viewModel.profileState.value.dataOrNull()?.username)
        assertEquals("aria@soniq.music", viewModel.profileState.value.dataOrNull()?.email)
    }

    @Test
    fun `duplicate username error surfaces directly on usernameError field`() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepo(
            AuthSessionState.Authenticated(AuthUser(id = "u1", email = "aria@soniq.music", displayName = "Aria"))
        )
        val profileRepo = FakeProfileRepo(
            updateError = AppError.Validation("That username is already taken. Choose a different handle.", field = "username")
        )
        val viewModel = ProfileViewModel(authRepo, profileRepo)
        advanceUntilIdle()

        viewModel.startEditing()
        viewModel.onDisplayNameChanged("Aria")
        viewModel.onUsernameChanged("SHIFIN")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertTrue(viewModel.editState.value.isEditing)
        assertEquals(
            "That username is already taken. Choose a different handle.",
            viewModel.editState.value.usernameError
        )
        assertNull(viewModel.editState.value.errorMessage)
    }

    @Test
    fun `submitEmailChange uses Auth repository flow and rejects unchanged email`() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepo(
            AuthSessionState.Authenticated(AuthUser(id = "u1", email = "aria@soniq.music", displayName = "Aria"))
        )
        val profileRepo = FakeProfileRepo()
        val viewModel = ProfileViewModel(authRepo, profileRepo)
        advanceUntilIdle()

        viewModel.toggleEmailChange(true)
        viewModel.onNewEmailChanged("ARIA@SONIQ.MUSIC")
        viewModel.submitEmailChange()
        advanceUntilIdle()
        assertNotNull(viewModel.emailChangeState.value.emailError)

        viewModel.onNewEmailChanged("new-aria@soniq.music")
        viewModel.submitEmailChange()
        advanceUntilIdle()

        assertNull(viewModel.emailChangeState.value.emailError)
        assertTrue(viewModel.emailChangeState.value.statusMessage.orEmpty().contains("new-aria@soniq.music"))
    }

    @Test
    fun `signOut clears cached profile and transitions session to Unauthenticated`() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepo(
            AuthSessionState.Authenticated(AuthUser(id = "u1", email = "aria@soniq.music", displayName = "Aria"))
        )
        val profileRepo = FakeProfileRepo()
        val viewModel = ProfileViewModel(authRepo, profileRepo)
        advanceUntilIdle()

        viewModel.signOut()
        advanceUntilIdle()

        assertTrue(authRepo.sessionState.value is AuthSessionState.Unauthenticated)
        assertTrue(profileRepo.cleared)
    }

    private class FakeAuthRepo(initial: AuthSessionState) : AuthRepository {
        private val _sessionState = MutableStateFlow(initial)
        override val sessionState: StateFlow<AuthSessionState> = _sessionState
        override suspend fun signIn(email: String, password: String): AppResult<AuthUser> = error("Unused")
        override suspend fun signUp(email: String, password: String, displayName: String): AppResult<SignUpOutcome> = error("Unused")
        override suspend fun updateEmail(newEmail: String): AppResult<EmailUpdateOutcome> =
            AppResult.Success(EmailUpdateOutcome.ConfirmationSent(newEmail))
        override suspend fun signOut(): AppResult<Unit> {
            _sessionState.value = AuthSessionState.Unauthenticated
            return AppResult.Success(Unit)
        }
        override suspend fun refreshSession(): AppResult<Unit> = AppResult.Success(Unit)
        override fun continueAsGuest() {
            _sessionState.value = AuthSessionState.Guest
        }
    }

    private class FakeProfileRepo(
        private val updateError: AppError? = null
    ) : ProfileRepository {
        var cleared = false
        private val _currentProfile = MutableStateFlow<UserProfile?>(null)
        override val currentProfile: StateFlow<UserProfile?> = _currentProfile

        override suspend fun loadProfile(
            userId: String,
            fallbackEmail: String,
            fallbackDisplayName: String?
        ): AppResult<UserProfile> {
            val profile = UserProfile(
                id = userId,
                email = fallbackEmail,
                displayName = fallbackDisplayName.orEmpty()
            )
            _currentProfile.value = profile
            return AppResult.Success(profile)
        }

        override suspend fun updateProfile(request: ProfileUpdateRequest): AppResult<UserProfile> {
            if (updateError != null) return AppResult.Error(updateError)
            val current = _currentProfile.value ?: error("Profile not loaded")
            val updated = current.copy(
                displayName = request.displayName,
                username = request.username.lowercase(),
                bio = request.bio,
                avatarUrl = request.avatarUrl
            )
            _currentProfile.value = updated
            return AppResult.Success(updated)
        }

        override fun clearCachedProfile() {
            cleared = true
            _currentProfile.value = null
        }
    }
}
