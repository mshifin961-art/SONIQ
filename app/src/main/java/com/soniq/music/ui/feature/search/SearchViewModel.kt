package com.soniq.music.ui.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniq.music.domain.model.CatalogueSearchResult
import com.soniq.music.domain.model.RecentSearchQuery
import com.soniq.music.domain.model.SearchScope
import com.soniq.music.domain.repository.CatalogueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val selectedScope: SearchScope = SearchScope.All,
    val isSearching: Boolean = false,
    val result: CatalogueSearchResult = CatalogueSearchResult(query = "", scope = SearchScope.All)
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val catalogueRepository: CatalogueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val recentSearches: StateFlow<List<RecentSearchQuery>> = catalogueRepository
        .observeRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChanged(value: String) {
        _uiState.update { it.copy(query = value) }
        executeSearch(query = value, scope = _uiState.value.selectedScope)
    }

    fun onScopeSelected(scope: SearchScope) {
        _uiState.update { it.copy(selectedScope = scope) }
        executeSearch(query = _uiState.value.query, scope = scope)
    }

    fun submitCurrentQuery() {
        val trimmed = _uiState.value.query.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            catalogueRepository.recordRecentSearch(trimmed)
        }
        executeSearch(query = trimmed, scope = _uiState.value.selectedScope)
    }

    fun selectRecentSearch(query: String) {
        _uiState.update { it.copy(query = query) }
        executeSearch(query = query, scope = _uiState.value.selectedScope)
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            catalogueRepository.clearRecentSearches()
        }
    }

    private fun executeSearch(query: String, scope: SearchScope) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        result = CatalogueSearchResult(query = "", scope = scope)
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isSearching = true) }
            val result = catalogueRepository.search(query, scope)
            _uiState.update { it.copy(isSearching = false, result = result) }
        }
    }
}
