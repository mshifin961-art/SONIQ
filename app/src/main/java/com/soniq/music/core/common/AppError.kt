package com.soniq.music.core.common

/**
 * Structured domain and infrastructure error hierarchy for SONIQ.
 */
sealed interface AppError {
    val message: String

    data class Network(
        override val message: String,
        val statusCode: Int? = null,
        val isRetryable: Boolean = true
    ) : AppError

    data class Authentication(
        override val message: String
    ) : AppError

    data class NotFound(
        override val message: String
    ) : AppError

    data class Validation(
        override val message: String,
        val field: String? = null
    ) : AppError

    data class Configuration(
        override val message: String
    ) : AppError

    data class Unexpected(
        override val message: String,
        val cause: Throwable? = null
    ) : AppError
}
