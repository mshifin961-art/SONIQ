package com.soniq.music.database

/**
 * Local Room persistence contract defining database identity and table names.
 */
object SoniqDatabaseContract {
    const val DATABASE_NAME = "soniq_music.db"
    const val INITIAL_SCHEMA_VERSION = 1

    object Tables {
        const val GENRES = "genres"
        const val TRACKS = "tracks"
        const val ALBUMS = "albums"
        const val ARTISTS = "artists"
        const val PLAYLISTS = "playlists"
        const val PLAYLIST_TRACKS = "playlist_tracks"
        const val LIKED_TRACKS = "liked_tracks"
        const val LISTENING_HISTORY = "listening_history"
        const val RECENT_SEARCHES = "recent_searches"
    }
}
