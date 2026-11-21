package com.soniq.music.domain.repository

import com.soniq.music.core.common.AppResult
import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.CatalogueSearchResult
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.RecentSearchQuery
import com.soniq.music.domain.model.SearchScope
import com.soniq.music.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Domain boundary for SONIQ's offline-first music catalogue, Starter Collection,
 * and multi-entity search infrastructure.
 */
interface CatalogueRepository {
    fun observeStarterCollection(): Flow<List<Track>>
    fun observeAllTracks(): Flow<List<Track>>
    fun observeArtists(): Flow<List<Artist>>
    fun observeAlbums(): Flow<List<Album>>
    fun observeGenres(): Flow<List<Genre>>
    fun observeRecentSearches(): Flow<List<RecentSearchQuery>>

    suspend fun ensureStarterCollectionLoaded(): AppResult<Int>
    suspend fun syncAuthorizedCatalogue(): AppResult<Int>
    suspend fun search(query: String, scope: SearchScope): CatalogueSearchResult
    suspend fun recordRecentSearch(query: String)
    suspend fun clearRecentSearches()
}
