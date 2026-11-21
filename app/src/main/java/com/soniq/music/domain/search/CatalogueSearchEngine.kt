package com.soniq.music.domain.search

import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.CatalogueSearchResult
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.SearchScope
import com.soniq.music.domain.model.Track

/**
 * Pure Kotlin deterministic search and relevance ranking across SONIQ's available catalogue.
 * Only returns entries actually present in the local/authorized catalogue.
 */
object CatalogueSearchEngine {

    fun search(
        rawQuery: String,
        scope: SearchScope,
        tracks: List<Track>,
        artists: List<Artist>,
        albums: List<Album>,
        genres: List<Genre>
    ): CatalogueSearchResult {
        val query = rawQuery.trim()
        if (query.isEmpty()) {
            return CatalogueSearchResult(query = "", scope = scope)
        }
        val needle = query.lowercase()

        val matchedTracks = if (scope == SearchScope.All || scope == SearchScope.Songs) {
            tracks
                .filter { track ->
                    track.title.contains(needle, ignoreCase = true) ||
                        track.artistName.contains(needle, ignoreCase = true) ||
                        track.albumTitle.contains(needle, ignoreCase = true) ||
                        track.categoryName.contains(needle, ignoreCase = true)
                }
                .sortedWith(
                    compareByDescending<Track> { it.title.startsWith(needle, ignoreCase = true) }
                        .thenByDescending { it.title.contains(needle, ignoreCase = true) }
                        .thenBy { it.title.lowercase() }
                )
        } else {
            emptyList()
        }

        val matchedArtists = if (scope == SearchScope.All || scope == SearchScope.Artists) {
            artists
                .filter { artist ->
                    artist.name.contains(needle, ignoreCase = true) ||
                        artist.bio.contains(needle, ignoreCase = true)
                }
                .sortedWith(
                    compareByDescending<Artist> { it.name.startsWith(needle, ignoreCase = true) }
                        .thenBy { it.name.lowercase() }
                )
        } else {
            emptyList()
        }

        val matchedAlbums = if (scope == SearchScope.All || scope == SearchScope.Albums) {
            albums
                .filter { album ->
                    album.title.contains(needle, ignoreCase = true) ||
                        album.artistName.contains(needle, ignoreCase = true)
                }
                .sortedWith(
                    compareByDescending<Album> { it.title.startsWith(needle, ignoreCase = true) }
                        .thenBy { it.title.lowercase() }
                )
        } else {
            emptyList()
        }

        val matchedGenres = if (scope == SearchScope.All || scope == SearchScope.Genres) {
            genres
                .filter { genre ->
                    genre.name.contains(needle, ignoreCase = true) ||
                        genre.moodTag.contains(needle, ignoreCase = true)
                }
                .sortedBy { it.sortOrder }
        } else {
            emptyList()
        }

        return CatalogueSearchResult(
            query = query,
            scope = scope,
            tracks = matchedTracks,
            artists = matchedArtists,
            albums = matchedAlbums,
            genres = matchedGenres
        )
    }
}
