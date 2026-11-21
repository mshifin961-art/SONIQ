package com.soniq.music.ui.feature.auth

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.config.AppConfig
import com.soniq.music.core.config.SupabaseConfig
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.AuthUser
import com.soniq.music.domain.model.EmailUpdateOutcome
import com.soniq.music.domain.model.SignUpOutcome
import com.soniq.music.domain.repository.AuthRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val configuredAppConfig = AppConfig(
        applicationId = "com.soniq.music",
        versionName = "0.2.0-phase2",
        versionCode = 2,
        phaseLabel = "Phase 2",
        isDebug = true,
        supabase = SupabaseConfig(
            url = "https://example.supabase.co",
            anonKey = "test-anon-key"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submit with invalid email sets inline validation error without calling repository`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo, configuredAppConfig)

        viewModel.onEmailChanged("bad-email")
        viewModel.onPasswordChanged("Soniq2026")
        viewModel.submit()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.emailError)
        assertEquals(0, fakeRepo.signInCalls)
    }

    @Test
    fun `signUp validates weak password and missing display name before calling repository`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo, configuredAppConfig)

        viewModel.setMode(AuthMode.SignUp)
        viewModel.onDisplayNameChanged("A")
        viewModel.onEmailChanged("valid@soniq.music")
        viewModel.onPasswordChanged("weak")
        viewModel.submit()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.displayNameError)
        assertNotNull(viewModel.uiState.value.passwordError)
        assertEquals(0, fakeRepo.signUpCalls)
    }

    @Test
    fun `signUp awaiting email confirmation updates confirmation banner`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository(
            signUpResult = AppResult.Success(SignUpOutcome.AwaitingEmailConfirmation("new@soniq.music"))
        )
        val viewModel = AuthViewModel(fakeRepo, configuredAppConfig)

        viewModel.setMode(AuthMode.SignUp)
        viewModel.onDisplayNameChanged("New Listener")
        viewModel.onEmailChanged("new@soniq.music")
        viewModel.onPasswordChanged("Soniq2026")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("new@soniq.music", viewModel.uiState.value.confirmationEmailSentTo)
        assertNull(viewModel.uiState.value.generalError)
        assertEquals("", viewModel.uiState.value.password)
    }

    @Test
    fun `signIn failure surfaces mapped error message`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository(
            signInResult = AppResult.Error(AppError.Authentication("Incorrect email or password."))
        )
        val viewModel = AuthViewModel(fakeRepo, configuredAppConfig)

        viewModel.onEmailChanged("user@soniq.music")
        viewModel.onPasswordChanged("WrongPass1")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Incorrect email or password.", viewModel.uiState.value.generalError)
    }

    @Test
    fun `continueAsGuest updates session state to Guest`() {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo, configuredAppConfig)

        viewModel.continueAsGuest()
        assertTrue(viewModel.sessionState.value is AuthSessionState.Guest)
    }

    private class FakeAuthRepository(
        private val signInResult: AppResult<AuthUser> = AppResult.Success(
            AuthUser(id = "u1", email = "user@soniq.music", displayName = "User")
        ),
        private val signUpResult: AppResult<SignUpOutcome> = AppResult.Success(
            SignUpOutcome.SignedIn(AuthUser(id = "u1", email = "user@soniq.music", displayName = "User"))
        )
    ) : AuthRepository {
        var signInCalls = 0
        var signUpCalls = 0
        private val _sessionState = MutableStateFlow<AuthSessionState>(AuthSessionState.Unauthenticated)
        override val sessionState: StateFlow<AuthSessionState> = _sessionState

        override suspend fun signIn(email: String, password: String): AppResult<AuthUser> {
            signInCalls++
            if (signInResult is AppResult.Success) {
                _sessionState.value = AuthSessionState.Authenticated(signInResult.data)
            }
            return signInResult
        }

        override suspend fun signUp(
            email: String,
            password: String,
            displayName: String
        ): AppResult<SignUpOutcome> {
            signUpCalls++
            if (signUpResult is AppResult.Success && signUpResult.data is SignUpOutcome.SignedIn) {
                _sessionState.value = AuthSessionState.Authenticated(signUpResult.data.user)
            }
            return signUpResult
        }

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
}
