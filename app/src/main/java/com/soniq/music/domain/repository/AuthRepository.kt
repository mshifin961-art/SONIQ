package com.soniq.music.domain.repository

import com.soniq.music.core.common.AppResult
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.AuthUser
import com.soniq.music.domain.model.EmailUpdateOutcome
import com.soniq.music.domain.model.SignUpOutcome
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain boundary for SONIQ user authentication and session lifecycle.
 */
interface AuthRepository {
    val sessionState: StateFlow<AuthSessionState>

    suspend fun signIn(email: String, password: String): AppResult<AuthUser>
    suspend fun signUp(email: String, password: String, displayName: String): AppResult<SignUpOutcome>
    suspend fun updateEmail(newEmail: String): AppResult<EmailUpdateOutcome>
    suspend fun signOut(): AppResult<Unit>
    suspend fun refreshSession(): AppResult<Unit>
    fun continueAsGuest()
}
