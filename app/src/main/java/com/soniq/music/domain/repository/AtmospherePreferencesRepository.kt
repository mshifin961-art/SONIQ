package com.soniq.music.domain.repository

import com.soniq.music.domain.model.AtmosphereAccent
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain boundary for reading and updating SONIQ's Dynamic Atmosphere preferences.
 */
interface AtmospherePreferencesRepository {
    val activeAccent: StateFlow<AtmosphereAccent>
    fun setAccent(accent: AtmosphereAccent)
}
