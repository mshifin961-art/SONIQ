package com.soniq.music.data.auth

import com.soniq.music.domain.model.AuthUser
import com.soniq.music.domain.model.EmailUpdateOutcome
import com.soniq.music.domain.model.SignUpOutcome
import com.soniq.music.network.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Low-level session events emitted by the underlying auth provider.
 */
sealed interface RemoteSessionEvent {
    data object Initializing : RemoteSessionEvent
    data class Authenticated(val user: AuthUser) : RemoteSessionEvent
    data class NotAuthenticated(val isSignOut: Boolean) : RemoteSessionEvent
    data object RefreshFailure : RemoteSessionEvent
}

/**
 * Thin remote gateway around Supabase Auth so [SupabaseAuthRepository] can be
 * thoroughly unit-tested without requiring live Supabase credentials.
 */
interface AuthRemoteDataSource {
    val isConfigured: Boolean
    fun observeSessionEvents(): Flow<RemoteSessionEvent>
    suspend fun signIn(email: String, password: String): AuthUser
    suspend fun signUp(email: String, password: String, displayName: String): SignUpOutcome
    suspend fun updateEmail(newEmail: String): EmailUpdateOutcome
    suspend fun signOut()
    suspend fun refreshSession()
}

@Singleton
class SupabaseAuthRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : AuthRemoteDataSource {

    override val isConfigured: Boolean
        get() = clientProvider.isConfigured

    override fun observeSessionEvents(): Flow<RemoteSessionEvent> {
        val client = clientProvider.client ?: return emptyFlow()
        return client.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Initializing -> RemoteSessionEvent.Initializing
                is SessionStatus.Authenticated -> {
                    val user = status.session.user?.toAuthUser()
                    if (user != null) {
                        RemoteSessionEvent.Authenticated(user)
                    } else {
                        RemoteSessionEvent.NotAuthenticated(isSignOut = false)
                    }
                }
                is SessionStatus.NotAuthenticated ->
                    RemoteSessionEvent.NotAuthenticated(isSignOut = status.isSignOut)
                is SessionStatus.RefreshFailure -> RemoteSessionEvent.RefreshFailure
            }
        }
    }

    override suspend fun signIn(email: String, password: String): AuthUser {
        val client = requireNotNull(clientProvider.client) { "Supabase is not configured." }
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return client.auth.currentUserOrNull()?.toAuthUser()
            ?: error("Session established but user record was unavailable.")
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): SignUpOutcome {
        val client = requireNotNull(clientProvider.client) { "Supabase is not configured." }
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("display_name", displayName)
            }
        }
        val user = client.auth.currentSessionOrNull()?.user?.toAuthUser()
        return if (user != null) {
            SignUpOutcome.SignedIn(user)
        } else {
            SignUpOutcome.AwaitingEmailConfirmation(email)
        }
    }

    override suspend fun updateEmail(newEmail: String): EmailUpdateOutcome {
        val client = requireNotNull(clientProvider.client) { "Supabase is not configured." }
        val updatedUser = client.auth.updateUser {
            email = newEmail
        }
        val currentEmail = updatedUser.email?.trim()?.lowercase()
        return if (currentEmail == newEmail) {
            EmailUpdateOutcome.Updated(newEmail)
        } else {
            EmailUpdateOutcome.ConfirmationSent(newEmail)
        }
    }

    override suspend fun signOut() {
        clientProvider.client?.auth?.signOut()
    }

    override suspend fun refreshSession() {
        val client = requireNotNull(clientProvider.client) { "Supabase is not configured." }
        client.auth.refreshCurrentSession()
    }

    private fun UserInfo.toAuthUser(): AuthUser {
        val metaDisplayName = userMetadata
            ?.get("display_name")
            ?.jsonPrimitive
            ?.contentOrNull
            ?.takeIf { it.isNotBlank() }

        return AuthUser(
            id = id,
            email = email.orEmpty(),
            displayName = metaDisplayName,
            isEmailConfirmed = emailConfirmedAt != null
        )
    }
}
