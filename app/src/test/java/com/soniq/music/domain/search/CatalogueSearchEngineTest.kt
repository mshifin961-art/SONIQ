package com.soniq.music.domain.search

import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.SearchScope
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.model.TrackAudioSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogueSearchEngineTest {

    private val genres = listOf(
        Genre("g-elec", "Electronic", "Energetic", "#3A86FF", 1),
        Genre("g-lofi", "Chill / Lo-fi", "Focus", "#9D4EDD", 2)
    )
    private val artists = listOf(
        Artist("a-1", "HoliznaCC0", "CC0 lo-fi producer", primaryGenreId = "g-lofi"),
        Artist("a-2", "Umplix", "Electronic composer", primaryGenreId = "g-elec")
    )
    private val albums = listOf(
        Album("alb-1", "SONIQ Starter: Electronic", "a-2", "Umplix", "g-elec"),
        Album("alb-2", "SONIQ Starter: Chill / Lo-fi", "a-1", "HoliznaCC0", "g-lofi")
    )
    private val tracks = listOf(
        Track(
            id = "t-1",
            title = "Victory",
            artistId = "a-2",
            artistName = "Umplix",
            albumId = "alb-1",
            albumTitle = "SONIQ Starter: Electronic",
            genreId = "g-elec",
            categoryName = "Electronic",
            durationMs = 124_000,
            audioSource = TrackAudioSource.BundledAsset("music/audio/t-1.ogg", false),
            isStarterTrack = true
        ),
        Track(
            id = "t-2",
            title = "Glad To Be Stuck Inside",
            artistId = "a-1",
            artistName = "HoliznaCC0",
            albumId = "alb-2",
            albumTitle = "SONIQ Starter: Chill / Lo-fi",
            genreId = "g-lofi",
            categoryName = "Chill / Lo-fi",
            durationMs = 162_000,
            audioSource = TrackAudioSource.BundledAsset("music/audio/t-2.mp3", false),
            isStarterTrack = true
        )
    )

    @Test
    fun `blank query returns empty result`() {
        val result = CatalogueSearchEngine.search("   ", SearchScope.All, tracks, artists, albums, genres)
        assertTrue(result.isEmpty)
    }

    @Test
    fun `multi-entity search matches songs, artists, albums, and genres case-insensitively`() {
        val result = CatalogueSearchEngine.search("electronic", SearchScope.All, tracks, artists, albums, genres)
        assertEquals(1, result.tracks.size)
        assertEquals(1, result.artists.size)
        assertEquals(1, result.albums.size)
        assertEquals(1, result.genres.size)
    }

    @Test
    fun `scope filtering restricts results to selected entity type`() {
        val songsOnly = CatalogueSearchEngine.search("holizna", SearchScope.Songs, tracks, artists, albums, genres)
        assertEquals(1, songsOnly.tracks.size)
        assertTrue(songsOnly.artists.isEmpty())
        assertTrue(songsOnly.albums.isEmpty())
        assertTrue(songsOnly.genres.isEmpty())

        val artistsOnly = CatalogueSearchEngine.search("holizna", SearchScope.Artists, tracks, artists, albums, genres)
        assertTrue(artistsOnly.tracks.isEmpty())
        assertEquals(1, artistsOnly.artists.size)
    }
}
