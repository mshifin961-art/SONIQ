package com.soniq.music.data.preferences

import com.soniq.music.domain.model.AtmosphereAccent
import com.soniq.music.domain.repository.AtmospherePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 1 reactive implementation of [AtmospherePreferencesRepository].
 * Will be backed by AndroidX DataStore in Phase 7 without changing domain or UI callers.
 */
@Singleton
class InMemoryAtmospherePreferencesRepository @Inject constructor() :
    AtmospherePreferencesRepository {

    private val _activeAccent = MutableStateFlow(AtmosphereAccent.DynamicPurple)
    override val activeAccent: StateFlow<AtmosphereAccent> = _activeAccent.asStateFlow()

    override fun setAccent(accent: AtmosphereAccent) {
        _activeAccent.value = accent
    }
}
