package com.soniq.music.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.config.AppConfig
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.SignUpOutcome
import com.soniq.music.domain.repository.AuthRepository
import com.soniq.music.domain.validation.AuthInputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    appConfig: AppConfig
) : ViewModel() {

    val sessionState: StateFlow<AuthSessionState> = authRepository.sessionState

    private val _uiState = MutableStateFlow(
        AuthUiState(isSupabaseConfigured = appConfig.supabase.isConfigured)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                displayNameError = null,
                emailError = null,
                passwordError = null,
                generalError = null,
                confirmationEmailSentTo = null
            )
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update {
            it.copy(displayName = value, displayNameError = null, generalError = null)
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(email = value, emailError = null, generalError = null)
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(password = value, passwordError = null, generalError = null)
        }
    }

    fun submit() {
        val current = _uiState.value
        if (current.isSubmitting) return

        val emailResult = AuthInputValidator.validateEmail(current.email)
        val passwordResult = when (current.mode) {
            AuthMode.SignIn -> AuthInputValidator.validateLoginPassword(current.password)
            AuthMode.SignUp -> AuthInputValidator.validateRegistrationPassword(current.password)
        }
        val nameResult = if (current.mode == AuthMode.SignUp) {
            AuthInputValidator.validateDisplayName(current.displayName)
        } else {
            AppResult.Success("")
        }

        val emailError = emailResult.errorOrNull()?.message
        val passwordError = passwordResult.errorOrNull()?.message
        val displayNameError = nameResult.errorOrNull()?.message

        if (emailError != null || passwordError != null || displayNameError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    displayNameError = displayNameError,
                    generalError = null
                )
            }
            return
        }

        val validEmail = emailResult.getOrNull() ?: return
        val validPassword = passwordResult.getOrNull() ?: return
        val validName = nameResult.getOrNull().orEmpty()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    generalError = null,
                    confirmationEmailSentTo = null
                )
            }

            when (current.mode) {
                AuthMode.SignIn -> {
                    when (val result = authRepository.signIn(validEmail, validPassword)) {
                        is AppResult.Success -> {
                            _uiState.update { it.copy(isSubmitting = false, password = "") }
                        }
                        is AppResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    generalError = result.error.message
                                )
                            }
                        }
                    }
                }

                AuthMode.SignUp -> {
                    when (val result = authRepository.signUp(validEmail, validPassword, validName)) {
                        is AppResult.Success -> {
                            when (val outcome = result.data) {
                                is SignUpOutcome.SignedIn -> {
                                    _uiState.update { it.copy(isSubmitting = false, password = "") }
                                }
                                is SignUpOutcome.AwaitingEmailConfirmation -> {
                                    _uiState.update {
                                        it.copy(
                                            isSubmitting = false,
                                            password = "",
                                            confirmationEmailSentTo = outcome.email
                                        )
                                    }
                                }
                            }
                        }
                        is AppResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    generalError = result.error.message
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun continueAsGuest() {
        authRepository.continueAsGuest()
    }
}
