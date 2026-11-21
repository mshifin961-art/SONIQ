package com.soniq.music.ui.navigation

import androidx.lifecycle.ViewModel
import com.soniq.music.audio.PlaybackController
import com.soniq.music.audio.PlaybackSnapshot
import com.soniq.music.core.config.AppConfig
import com.soniq.music.domain.model.AtmosphereAccent
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.repository.AtmospherePreferencesRepository
import com.soniq.music.domain.repository.AuthRepository
import com.soniq.music.network.SupabaseEnvironmentStatus
import com.soniq.music.network.SupabaseEnvironmentValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Shell-level ViewModel coordinating theme atmosphere, authentication session state,
 * build config visibility, and UI-independent playback state observation.
 */
@HiltViewModel
class SoniqShellViewModel @Inject constructor(
    private val atmosphereRepository: AtmospherePreferencesRepository,
    authRepository: AuthRepository,
    playbackController: PlaybackController,
    val appConfig: AppConfig
) : ViewModel() {

    val activeAccent: StateFlow<AtmosphereAccent> = atmosphereRepository.activeAccent
    val sessionState: StateFlow<AuthSessionState> = authRepository.sessionState
    val playbackSnapshot: StateFlow<PlaybackSnapshot> = playbackController.snapshot

    val supabaseStatus: SupabaseEnvironmentStatus =
        SupabaseEnvironmentValidator.evaluate(appConfig.supabase)

    fun selectAccent(accent: AtmosphereAccent) {
        atmosphereRepository.setAccent(accent)
    }
}
