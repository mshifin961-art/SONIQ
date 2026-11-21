package com.soniq.music.core.state

import com.soniq.music.core.common.AppError

/**
 * Standardized screen and section UI state representation.
 * Enforces explicit loading, empty, error, and content states across SONIQ.
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Empty(
        val title: String,
        val subtitle: String
    ) : UiState<Nothing>

    data class Error(
        val error: AppError,
        val isRetryable: Boolean = true
    ) : UiState<Nothing>

    val isLoading: Boolean
        get() = this is Loading

    fun dataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}

inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> = when (this) {
    UiState.Idle -> UiState.Idle
    UiState.Loading -> UiState.Loading
    is UiState.Success -> UiState.Success(transform(data))
    is UiState.Empty -> this
    is UiState.Error -> this
}
