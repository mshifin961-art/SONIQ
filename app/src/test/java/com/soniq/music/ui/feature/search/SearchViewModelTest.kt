package com.soniq.music.ui.feature.search

import com.soniq.music.core.common.AppResult
import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.CatalogueSearchResult
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.RecentSearchQuery
import com.soniq.music.domain.model.SearchScope
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.repository.CatalogueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `query and scope updates invoke search and submit records recent query`() = runTest(testDispatcher) {
        val repo = FakeCatalogueRepo()
        val viewModel = SearchViewModel(repo)

        viewModel.onQueryChanged("Chopin")
        advanceUntilIdle()
        assertEquals("Chopin", viewModel.uiState.value.result.query)

        viewModel.onScopeSelected(SearchScope.Songs)
        advanceUntilIdle()
        assertEquals(SearchScope.Songs, viewModel.uiState.value.result.scope)

        viewModel.submitCurrentQuery()
        advanceUntilIdle()
        assertEquals(listOf("Chopin"), repo.recorded)
    }

    private class FakeCatalogueRepo : CatalogueRepository {
        val recorded = mutableListOf<String>()
        private val recent = MutableStateFlow<List<RecentSearchQuery>>(emptyList())

        override fun observeStarterCollection(): Flow<List<Track>> = emptyFlow()
        override fun observeAllTracks(): Flow<List<Track>> = emptyFlow()
        override fun observeArtists(): Flow<List<Artist>> = emptyFlow()
        override fun observeAlbums(): Flow<List<Album>> = emptyFlow()
        override fun observeGenres(): Flow<List<Genre>> = emptyFlow()
        override fun observeRecentSearches(): Flow<List<RecentSearchQuery>> = recent
        override suspend fun ensureStarterCollectionLoaded(): AppResult<Int> = AppResult.Success(0)
        override suspend fun syncAuthorizedCatalogue(): AppResult<Int> = AppResult.Success(0)
        override suspend fun search(query: String, scope: SearchScope): CatalogueSearchResult =
            CatalogueSearchResult(query = query, scope = scope)
        override suspend fun recordRecentSearch(query: String) {
            recorded.add(query)
        }
        override suspend fun clearRecentSearches() {
            recorded.clear()
        }
    }
}
