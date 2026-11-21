package com.soniq.music.data.auth

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.common.DispatcherProvider
import com.soniq.music.core.common.runCatchingResult
import com.soniq.music.di.ApplicationScope
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.AuthUser
import com.soniq.music.domain.model.EmailUpdateOutcome
import com.soniq.music.domain.model.SignUpOutcome
import com.soniq.music.domain.repository.AuthRepository
import com.soniq.music.network.AuthErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production Supabase Authentication repository managing registration, sign-in,
 * Auth-managed email changes, automatic session restoration, sign-out, and Guest mode.
 */
@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val dispatchers: DispatcherProvider,
    @ApplicationScope private val appScope: CoroutineScope
) : AuthRepository {

    private val _sessionState = MutableStateFlow<AuthSessionState>(
        if (remoteDataSource.isConfigured) {
            AuthSessionState.Initializing
        } else {
            AuthSessionState.Unconfigured
        }
    )
    override val sessionState: StateFlow<AuthSessionState> = _sessionState.asStateFlow()

    init {
        observeRemoteSession()
    }

    private fun observeRemoteSession() {
        if (!remoteDataSource.isConfigured) return
        appScope.launch(dispatchers.io) {
            remoteDataSource.observeSessionEvents().collect { event ->
                when (event) {
                    is RemoteSessionEvent.Initializing -> {
                        if (_sessionState.value !is AuthSessionState.Guest) {
                            _sessionState.value = AuthSessionState.Initializing
                        }
                    }
                    is RemoteSessionEvent.Authenticated -> {
                        _sessionState.value = AuthSessionState.Authenticated(event.user)
                    }
                    is RemoteSessionEvent.NotAuthenticated -> {
                        if (_sessionState.value !is AuthSessionState.Guest || event.isSignOut) {
                            _sessionState.value = AuthSessionState.Unauthenticated
                        }
                    }
                    is RemoteSessionEvent.RefreshFailure -> {
                        if (_sessionState.value is AuthSessionState.Initializing) {
                            _sessionState.value = AuthSessionState.Unauthenticated
                        }
                    }
                }
            }
        }
    }

    override suspend fun signIn(email: String, password: String): AppResult<AuthUser> =
        withContext(dispatchers.io) {
            if (!remoteDataSource.isConfigured) {
                return@withContext AppResult.Error(
                    AppError.Configuration(
                        "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties."
                    )
                )
            }

            runCatchingResult(errorMapper = AuthErrorMapper::map) {
                val user = remoteDataSource.signIn(
                    email = email.trim().lowercase(),
                    password = password
                )
                _sessionState.value = AuthSessionState.Authenticated(user)
                user
            }
        }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): AppResult<SignUpOutcome> = withContext(dispatchers.io) {
        if (!remoteDataSource.isConfigured) {
            return@withContext AppResult.Error(
                AppError.Configuration(
                    "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties."
                )
            )
        }

        runCatchingResult(errorMapper = AuthErrorMapper::map) {
            val outcome = remoteDataSource.signUp(
                email = email.trim().lowercase(),
                password = password,
                displayName = displayName.trim()
            )
            if (outcome is SignUpOutcome.SignedIn) {
                _sessionState.value = AuthSessionState.Authenticated(outcome.user)
            }
            outcome
        }
    }

    override suspend fun updateEmail(newEmail: String): AppResult<EmailUpdateOutcome> =
        withContext(dispatchers.io) {
            if (!remoteDataSource.isConfigured) {
                return@withContext AppResult.Error(
                    AppError.Configuration(
                        "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties."
                    )
                )
            }

            runCatchingResult(errorMapper = AuthErrorMapper::map) {
                val normalized = newEmail.trim().lowercase()
                val outcome = remoteDataSource.updateEmail(normalized)
                if (outcome is EmailUpdateOutcome.Updated) {
                    val current = _sessionState.value as? AuthSessionState.Authenticated
                    if (current != null) {
                        _sessionState.value = AuthSessionState.Authenticated(
                            current.user.copy(email = outcome.updatedEmail)
                        )
                    }
                }
                outcome
            }
        }

    override suspend fun signOut(): AppResult<Unit> = withContext(dispatchers.io) {
        if (!remoteDataSource.isConfigured) {
            _sessionState.value = AuthSessionState.Unconfigured
            return@withContext AppResult.Success(Unit)
        }

        runCatchingResult(errorMapper = AuthErrorMapper::map) {
            remoteDataSource.signOut()
            _sessionState.value = AuthSessionState.Unauthenticated
        }
    }

    override suspend fun refreshSession(): AppResult<Unit> = withContext(dispatchers.io) {
        if (!remoteDataSource.isConfigured) {
            return@withContext AppResult.Error(
                AppError.Configuration("Supabase is not configured.")
            )
        }

        runCatchingResult(errorMapper = AuthErrorMapper::map) {
            remoteDataSource.refreshSession()
        }
    }

    override fun continueAsGuest() {
        _sessionState.value = AuthSessionState.Guest
    }
}
