package com.soniq.music.domain.model

/**
 * Reactive authentication lifecycle state observed across SONIQ.
 * Guest listening remains supported so users can access the offline Starter Collection without signing in.
 */
sealed interface AuthSessionState {
    data object Initializing : AuthSessionState
    data object Unconfigured : AuthSessionState
    data object Unauthenticated : AuthSessionState
    data object Guest : AuthSessionState
    data class Authenticated(val user: AuthUser) : AuthSessionState
}

/**
 * Result of a registration request.
 * Supabase projects with email confirmation enabled return [AwaitingEmailConfirmation].
 */
sealed interface SignUpOutcome {
    data class SignedIn(val user: AuthUser) : SignUpOutcome
    data class AwaitingEmailConfirmation(val email: String) : SignUpOutcome
}

/**
 * Result of an Auth-managed email update request via Supabase Auth.
 */
sealed interface EmailUpdateOutcome {
    data class ConfirmationSent(val pendingEmail: String) : EmailUpdateOutcome
    data class Updated(val updatedEmail: String) : EmailUpdateOutcome
}
