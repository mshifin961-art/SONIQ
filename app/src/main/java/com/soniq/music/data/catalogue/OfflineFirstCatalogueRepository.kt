package com.soniq.music.data.catalogue

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.common.DispatcherProvider
import com.soniq.music.core.common.runCatchingResult
import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.CatalogueSearchResult
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.RecentSearchQuery
import com.soniq.music.domain.model.SearchScope
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.repository.CatalogueRepository
import com.soniq.music.domain.search.CatalogueSearchEngine
import com.soniq.music.network.AuthErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first [CatalogueRepository] combining:
 * 1. Bundled 10-track licensed Starter Collection (`assets/music/licenses.json`), always available without an account or internet.
 * 2. Local Room cache for instant offline browsing and multi-entity search.
 * 3. Optional read-only Supabase authorized catalogue sync when configured.
 */
@Singleton
class OfflineFirstCatalogueRepository @Inject constructor(
    private val starterLoader: StarterCollectionLoader,
    private val localDataSource: CatalogueLocalDataSource,
    private val remoteDataSource: CatalogueRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : CatalogueRepository {

    override fun observeStarterCollection(): Flow<List<Track>> = localDataSource.observeStarterTracks()
    override fun observeAllTracks(): Flow<List<Track>> = localDataSource.observeAllTracks()
    override fun observeArtists(): Flow<List<Artist>> = localDataSource.observeArtists()
    override fun observeAlbums(): Flow<List<Album>> = localDataSource.observeAlbums()
    override fun observeGenres(): Flow<List<Genre>> = localDataSource.observeGenres()
    override fun observeRecentSearches(): Flow<List<RecentSearchQuery>> = localDataSource.observeRecentSearches()

    override suspend fun ensureStarterCollectionLoaded(): AppResult<Int> =
        withContext(dispatchers.io) {
            runCatchingResult {
                val bundle = starterLoader.loadBundle()
                localDataSource.saveCatalogue(
                    genres = bundle.genres,
                    artists = bundle.artists,
                    albums = bundle.albums,
                    tracks = bundle.tracks
                )
                bundle.tracks.size
            }
        }

    override suspend fun syncAuthorizedCatalogue(): AppResult<Int> =
        withContext(dispatchers.io) {
            if (!remoteDataSource.isConfigured) {
                return@withContext AppResult.Error(
                    AppError.Configuration("Supabase is not configured. Using local Starter Collection.")
                )
            }
            runCatchingResult(errorMapper = AuthErrorMapper::map) {
                val payload = remoteDataSource.fetchAuthorizedCatalogue()
                localDataSource.saveCatalogue(
                    genres = payload.genres,
                    artists = payload.artists,
                    albums = payload.albums,
                    tracks = payload.tracks
                )
                payload.tracks.size
            }
        }

    override suspend fun search(query: String, scope: SearchScope): CatalogueSearchResult =
        withContext(dispatchers.default) {
            val tracks = localDataSource.getAllTracksOnce()
            val artists = localDataSource.getAllArtistsOnce()
            val albums = localDataSource.getAllAlbumsOnce()
            val genres = localDataSource.getAllGenresOnce()
            CatalogueSearchEngine.search(
                rawQuery = query,
                scope = scope,
                tracks = tracks,
                artists = artists,
                albums = albums,
                genres = genres
            )
        }

    override suspend fun recordRecentSearch(query: String) {
        withContext(dispatchers.io) {
            localDataSource.recordRecentSearch(query, System.currentTimeMillis())
        }
    }

    override suspend fun clearRecentSearches() {
        withContext(dispatchers.io) {
            localDataSource.clearRecentSearches()
        }
    }
}
