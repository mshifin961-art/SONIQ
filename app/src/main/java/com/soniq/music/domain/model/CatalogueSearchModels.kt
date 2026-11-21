package com.soniq.music.domain.model

enum class SearchScope(val label: String) {
    All("All"),
    Songs("Songs"),
    Artists("Artists"),
    Albums("Albums"),
    Genres("Genres")
}

data class RecentSearchQuery(
    val query: String,
    val searchedAtEpochMs: Long
)

data class CatalogueSearchResult(
    val query: String,
    val scope: SearchScope,
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val genres: List<Genre> = emptyList()
) {
    val totalMatches: Int
        get() = tracks.size + artists.size + albums.size + genres.size

    val isEmpty: Boolean
        get() = totalMatches == 0
}
