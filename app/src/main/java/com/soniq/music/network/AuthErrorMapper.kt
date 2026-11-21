package com.soniq.music.network

import com.soniq.music.core.common.AppError
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException

/**
 * Maps Supabase Auth, PostgREST, and transport exceptions to safe, user-facing [AppError]s.
 * Never leaks raw tokens, SQL errors, or stack traces to UI layers.
 */
object AuthErrorMapper {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is HttpRequestException -> AppError.Network(
            message = "Unable to reach SONIQ servers. Check your internet connection and try again.",
            isRetryable = true
        )
        is IOException -> AppError.Network(
            message = "Network connection interrupted. Please try again.",
            isRetryable = true
        )
        is RestException -> mapRestMessage(
            rawMessage = throwable.error.ifBlank { throwable.message.orEmpty() },
            statusCode = throwable.statusCode
        )
        else -> mapGenericMessage(throwable.message.orEmpty(), throwable)
    }

    internal fun mapRestMessage(rawMessage: String, statusCode: Int? = null): AppError {
        val lower = rawMessage.lowercase()
        return when {
            "invalid login credentials" in lower || "invalid_credentials" in lower ->
                AppError.Authentication("Incorrect email or password. Please check your details and try again.")
            "email not confirmed" in lower || "email_not_confirmed" in lower ->
                AppError.Authentication("Please confirm your email address using the link sent to your inbox before signing in.")
            "user already registered" in lower || "already been registered" in lower || "user_already_exists" in lower ->
                AppError.Authentication("An account with this email already exists. Try signing in instead.")
            "rate limit" in lower || "too many requests" in lower || statusCode == 429 ->
                AppError.Authentication("Too many attempts. Please wait a moment before trying again.")
            "password" in lower && ("weak" in lower || "short" in lower || "at least" in lower) ->
                AppError.Validation("Password does not meet security requirements. Use at least 8 characters with letters and numbers.", field = "password")
            "idx_profiles_username_lower_unique" in lower ||
                "profiles_username_key" in lower ||
                (("duplicate key" in lower || "23505" in lower) && "username" in lower) ->
                AppError.Validation("That username is already taken. Choose a different handle.", field = "username")
            "email is managed by supabase auth" in lower ->
                AppError.Validation("Email changes must be completed through account email verification.", field = "email")
            "row-level security" in lower || statusCode == 401 || statusCode == 403 ->
                AppError.Authentication("Your session has expired or lacks permission. Please sign in again.")
            statusCode != null && statusCode >= 500 ->
                AppError.Network("SONIQ authentication service is temporarily unavailable. Please try again shortly.", statusCode = statusCode)
            else ->
                AppError.Authentication("Authentication request could not be completed. Please try again.")
        }
    }

    private fun mapGenericMessage(rawMessage: String, cause: Throwable): AppError {
        val lower = rawMessage.lowercase()
        return when {
            "unable to resolve host" in lower || "timeout" in lower || "failed to connect" in lower ->
                AppError.Network("Unable to reach SONIQ servers. Check your internet connection and try again.")
            else ->
                AppError.Unexpected(
                    message = "Something went wrong while communicating with SONIQ.",
                    cause = cause
                )
        }
    }
}
