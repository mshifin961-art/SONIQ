package com.soniq.music.ui.feature.auth

enum class AuthMode {
    SignIn,
    SignUp
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSubmitting: Boolean = false,
    val generalError: String? = null,
    val confirmationEmailSentTo: String? = null,
    val isSupabaseConfigured: Boolean = true
)
