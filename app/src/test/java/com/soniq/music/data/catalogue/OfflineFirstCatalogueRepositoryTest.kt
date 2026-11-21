package com.soniq.music.data.catalogue

import com.soniq.music.core.common.AppError
import com.soniq.music.core.common.AppResult
import com.soniq.music.core.common.DispatcherProvider
import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.RecentSearchQuery
import com.soniq.music.domain.model.SearchScope
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.model.TrackAudioSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstCatalogueRepositoryTest {

    private fun testDispatchers(dispatcher: CoroutineDispatcher): DispatcherProvider =
        object : DispatcherProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val default: CoroutineDispatcher = dispatcher
            override val main: CoroutineDispatcher = dispatcher
        }

    @Test
    fun `ensureStarterCollectionLoaded seeds local store offline without Supabase`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val local = FakeLocalCatalogueDataSource()
        val remote = FakeRemoteCatalogueDataSource(isConfigured = false)
        val loader = FakeStarterLoader()

        val repo = OfflineFirstCatalogueRepository(loader, local, remote, testDispatchers(dispatcher))

        val loaded = repo.ensureStarterCollectionLoaded()
        assertTrue(loaded.isSuccess)
        assertEquals(1, loaded.getOrNull())
        assertEquals(1, repo.observeStarterCollection().first().size)

        val syncRes = repo.syncAuthorizedCatalogue()
        assertTrue(syncRes is AppResult.Error && syncRes.error is AppError.Configuration)
    }

    @Test
    fun `search and recent search history work against local catalogue`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val local = FakeLocalCatalogueDataSource()
        val remote = FakeRemoteCatalogueDataSource(isConfigured = false)
        val repo = OfflineFirstCatalogueRepository(FakeStarterLoader(), local, remote, testDispatchers(dispatcher))

        repo.ensureStarterCollectionLoaded()
        val searchResult = repo.search("Synth", SearchScope.Songs)
        assertEquals(1, searchResult.tracks.size)
        assertEquals("80's Synth Wave", searchResult.tracks.first().title)

        repo.recordRecentSearch("Synth")
        assertEquals(1, repo.observeRecentSearches().first().size)

        repo.clearRecentSearches()
        assertTrue(repo.observeRecentSearches().first().isEmpty())
    }

    private class FakeStarterLoader : StarterCollectionLoader {
        override fun loadBundle(): StarterCollectionBundle = StarterCollectionBundle(
            genres = listOf(Genre("g-elec", "Electronic")),
            artists = listOf(Artist("a-1", "Alex McCulloch")),
            albums = listOf(Album("alb-1", "SONIQ Starter: Electronic", "a-1", "Alex McCulloch", "g-elec")),
            tracks = listOf(
                Track(
                    id = "starter-electronic-01",
                    title = "80's Synth Wave",
                    artistId = "a-1",
                    artistName = "Alex McCulloch",
                    albumId = "alb-1",
                    albumTitle = "SONIQ Starter: Electronic",
                    genreId = "g-elec",
                    categoryName = "Electronic",
                    durationMs = 148_000,
                    audioSource = TrackAudioSource.BundledAsset("music/audio/starter-electronic-01.ogg", false),
                    isStarterTrack = true
                )
            )
        )
    }

    private class FakeRemoteCatalogueDataSource(
        override val isConfigured: Boolean
    ) : CatalogueRemoteDataSource {
        override suspend fun fetchAuthorizedCatalogue(): RemoteCataloguePayload =
            RemoteCataloguePayload(emptyList(), emptyList(), emptyList(), emptyList())
    }

    private class FakeLocalCatalogueDataSource : CatalogueLocalDataSource {
        private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())
        private val artistsFlow = MutableStateFlow<List<Artist>>(emptyList())
        private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())
        private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())
        private val recentFlow = MutableStateFlow<List<RecentSearchQuery>>(emptyList())

        override fun observeStarterTracks(): Flow<List<Track>> = tracksFlow
        override fun observeAllTracks(): Flow<List<Track>> = tracksFlow
        override fun observeArtists(): Flow<List<Artist>> = artistsFlow
        override fun observeAlbums(): Flow<List<Album>> = albumsFlow
        override fun observeGenres(): Flow<List<Genre>> = genresFlow
        override fun observeRecentSearches(): Flow<List<RecentSearchQuery>> = recentFlow

        override suspend fun getAllTracksOnce(): List<Track> = tracksFlow.value
        override suspend fun getAllArtistsOnce(): List<Artist> = artistsFlow.value
        override suspend fun getAllAlbumsOnce(): List<Album> = albumsFlow.value
        override suspend fun getAllGenresOnce(): List<Genre> = genresFlow.value

        override suspend fun saveCatalogue(
            genres: List<Genre>,
            artists: List<Artist>,
            albums: List<Album>,
            tracks: List<Track>
        ) {
            genresFlow.value = genres
            artistsFlow.value = artists
            albumsFlow.value = albums
            tracksFlow.value = tracks
        }

        override suspend fun recordRecentSearch(query: String, timestampMs: Long) {
            recentFlow.value = listOf(RecentSearchQuery(query, timestampMs))
        }

        override suspend fun clearRecentSearches() {
            recentFlow.value = emptyList()
        }
    }
}
