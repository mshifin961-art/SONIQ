package com.soniq.music.ui.feature.catalogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.repository.CatalogueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Coordinates offline-first Starter Collection seeding and reactive catalogue streams
 * shared across Home, Explore, and Library screens.
 */
@HiltViewModel
class CatalogueViewModel @Inject constructor(
    private val catalogueRepository: CatalogueRepository
) : ViewModel() {

    val starterTracks: StateFlow<List<Track>> = catalogueRepository
        .observeStarterCollection()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTracks: StateFlow<List<Track>> = catalogueRepository
        .observeAllTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val artists: StateFlow<List<Artist>> = catalogueRepository
        .observeArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums: StateFlow<List<Album>> = catalogueRepository
        .observeAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val genres: StateFlow<List<Genre>> = catalogueRepository
        .observeGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoadingStarter = MutableStateFlow(true)
    val isLoadingStarter: StateFlow<Boolean> = _isLoadingStarter.asStateFlow()

    init {
        initializeCatalogue()
    }

    private fun initializeCatalogue() {
        viewModelScope.launch {
            _isLoadingStarter.value = true
            catalogueRepository.ensureStarterCollectionLoaded()
            _isLoadingStarter.value = false
            // Best-effort read-only sync if Supabase is configured; fails silently offline
            catalogueRepository.syncAuthorizedCatalogue()
        }
    }
}
