package com.soniq.music.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.state.UiState
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.EmailUpdateOutcome
import com.soniq.music.domain.model.ProfileUpdateRequest
import com.soniq.music.domain.model.UserProfile
import com.soniq.music.domain.repository.AuthRepository
import com.soniq.music.domain.repository.ProfileRepository
import com.soniq.music.domain.validation.AuthInputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileEditState(
    val isEditing: Boolean = false,
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val displayNameError: String? = null,
    val usernameError: String? = null,
    val bioError: String? = null,
    val avatarUrlError: String? = null,
    val isSaving: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

data class EmailChangeState(
    val isExpanded: Boolean = false,
    val newEmail: String = "",
    val emailError: String? = null,
    val isSubmitting: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val sessionState: StateFlow<AuthSessionState> = authRepository.sessionState

    private val _profileState = MutableStateFlow<UiState<UserProfile>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserProfile>> = _profileState.asStateFlow()

    private val _editState = MutableStateFlow(ProfileEditState())
    val editState: StateFlow<ProfileEditState> = _editState.asStateFlow()

    private val _emailChangeState = MutableStateFlow(EmailChangeState())
    val emailChangeState: StateFlow<EmailChangeState> = _emailChangeState.asStateFlow()

    private val _isSigningOut = MutableStateFlow(false)
    val isSigningOut: StateFlow<Boolean> = _isSigningOut.asStateFlow()

    init {
        observeSessionForProfile()
    }

    private fun observeSessionForProfile() {
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                when (session) {
                    is AuthSessionState.Authenticated -> {
                        loadProfile(
                            userId = session.user.id,
                            email = session.user.email,
                            displayName = session.user.displayName
                        )
                    }
                    else -> {
                        profileRepository.clearCachedProfile()
                        _profileState.value = UiState.Idle
                        _editState.value = ProfileEditState()
                        _emailChangeState.value = EmailChangeState()
                    }
                }
            }
        }
    }

    fun refreshProfile() {
        val session = authRepository.sessionState.value as? AuthSessionState.Authenticated ?: return
        viewModelScope.launch {
            loadProfile(
                userId = session.user.id,
                email = session.user.email,
                displayName = session.user.displayName
            )
        }
    }

    private suspend fun loadProfile(userId: String, email: String, displayName: String?) {
        _profileState.value = UiState.Loading
        when (val result = profileRepository.loadProfile(userId, email, displayName)) {
            is AppResult.Success -> _profileState.value = UiState.Success(result.data)
            is AppResult.Error -> _profileState.value = UiState.Error(result.error)
        }
    }

    fun startEditing() {
        val profile = _profileState.value.dataOrNull() ?: return
        _editState.value = ProfileEditState(
            isEditing = true,
            displayName = profile.displayName,
            username = profile.username,
            bio = profile.bio,
            avatarUrl = profile.avatarUrl.orEmpty()
        )
    }

    fun cancelEditing() {
        _editState.update { it.copy(isEditing = false, errorMessage = null) }
    }

    fun onDisplayNameChanged(value: String) {
        _editState.update { it.copy(displayName = value, displayNameError = null, errorMessage = null) }
    }

    fun onUsernameChanged(value: String) {
        _editState.update { it.copy(username = value, usernameError = null, errorMessage = null) }
    }

    fun onBioChanged(value: String) {
        _editState.update { it.copy(bio = value, bioError = null, errorMessage = null) }
    }

    fun onAvatarUrlChanged(value: String) {
        _editState.update { it.copy(avatarUrl = value, avatarUrlError = null, errorMessage = null) }
    }

    fun saveProfile() {
        val currentProfile = _profileState.value.dataOrNull() ?: return
        val draft = _editState.value
        if (draft.isSaving) return

        val nameResult = AuthInputValidator.validateDisplayName(draft.displayName)
        val usernameResult = AuthInputValidator.validateUsernameOptional(draft.username)
        val bioResult = AuthInputValidator.validateBio(draft.bio)
        val avatarResult = AuthInputValidator.validateAvatarUrlOptional(draft.avatarUrl)

        val nameErr = nameResult.errorOrNull()?.message
        val userErr = usernameResult.errorOrNull()?.message
        val bioErr = bioResult.errorOrNull()?.message
        val avatarErr = avatarResult.errorOrNull()?.message

        if (nameErr != null || userErr != null || bioErr != null || avatarErr != null) {
            _editState.update {
                it.copy(
                    displayNameError = nameErr,
                    usernameError = userErr,
                    bioError = bioErr,
                    avatarUrlError = avatarErr
                )
            }
            return
        }

        val request = ProfileUpdateRequest(
            userId = currentProfile.id,
            displayName = nameResult.getOrNull().orEmpty(),
            username = usernameResult.getOrNull().orEmpty(),
            bio = bioResult.getOrNull().orEmpty(),
            avatarUrl = avatarResult.getOrNull()
        )

        viewModelScope.launch {
            _editState.update { it.copy(isSaving = true, errorMessage = null, statusMessage = null) }
            when (val result = profileRepository.updateProfile(request)) {
                is AppResult.Success -> {
                    _profileState.value = UiState.Success(result.data)
                    _editState.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            statusMessage = "Profile updated."
                        )
                    }
                }
                is AppResult.Error -> {
                    val fieldError = result.error as? AppError.Validation
                    _editState.update {
                        it.copy(
                            isSaving = false,
                            usernameError = if (fieldError?.field == "username") fieldError.message else it.usernameError,
                            errorMessage = if (fieldError?.field == "username") null else result.error.message
                        )
                    }
                }
            }
        }
    }

    fun toggleEmailChange(expanded: Boolean) {
        _emailChangeState.value = EmailChangeState(isExpanded = expanded)
    }

    fun onNewEmailChanged(value: String) {
        _emailChangeState.update { it.copy(newEmail = value, emailError = null, errorMessage = null) }
    }

    fun submitEmailChange() {
        val current = _emailChangeState.value
        if (current.isSubmitting) return

        val emailResult = AuthInputValidator.validateEmail(current.newEmail)
        val emailError = emailResult.errorOrNull()?.message
        if (emailError != null) {
            _emailChangeState.update { it.copy(emailError = emailError) }
            return
        }

        val normalizedEmail = emailResult.getOrNull() ?: return
        val existingEmail = _profileState.value.dataOrNull()?.email?.trim()?.lowercase()
        if (existingEmail == normalizedEmail) {
            _emailChangeState.update {
                it.copy(emailError = "Enter a different email address than your current one.")
            }
            return
        }

        viewModelScope.launch {
            _emailChangeState.update {
                it.copy(isSubmitting = true, errorMessage = null, statusMessage = null)
            }
            when (val result = authRepository.updateEmail(normalizedEmail)) {
                is AppResult.Success -> {
                    when (val outcome = result.data) {
                        is EmailUpdateOutcome.ConfirmationSent -> {
                            _emailChangeState.update {
                                it.copy(
                                    isSubmitting = false,
                                    newEmail = "",
                                    statusMessage = "Confirmation sent to ${outcome.pendingEmail}. Your profile email will sync automatically after verification."
                                )
                            }
                        }
                        is EmailUpdateOutcome.Updated -> {
                            val currentProfile = _profileState.value.dataOrNull()
                            if (currentProfile != null) {
                                _profileState.value = UiState.Success(
                                    currentProfile.copy(email = outcome.updatedEmail)
                                )
                            }
                            _emailChangeState.update {
                                it.copy(
                                    isSubmitting = false,
                                    isExpanded = false,
                                    newEmail = "",
                                    statusMessage = "Account email updated to ${outcome.updatedEmail}."
                                )
                            }
                        }
                    }
                }
                is AppResult.Error -> {
                    _emailChangeState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        if (_isSigningOut.value) return
        viewModelScope.launch {
            _isSigningOut.value = true
            val result = authRepository.signOut()
            if (result is AppResult.Error) {
                _editState.update { it.copy(errorMessage = result.error.message) }
            } else {
                profileRepository.clearCachedProfile()
            }
            _isSigningOut.value = false
        }
    }
}
