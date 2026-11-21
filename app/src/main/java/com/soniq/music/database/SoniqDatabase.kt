package com.soniq.music.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soniq.music.database.dao.CatalogueDao
import com.soniq.music.database.dao.RecentSearchDao
import com.soniq.music.database.entity.AlbumEntity
import com.soniq.music.database.entity.ArtistEntity
import com.soniq.music.database.entity.GenreEntity
import com.soniq.music.database.entity.RecentSearchEntity
import com.soniq.music.database.entity.TrackEntity

@Database(
    entities = [
        GenreEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        TrackEntity::class,
        RecentSearchEntity::class
    ],
    version = SoniqDatabaseContract.INITIAL_SCHEMA_VERSION,
    exportSchema = false
)
abstract class SoniqDatabase : RoomDatabase() {
    abstract fun catalogueDao(): CatalogueDao
    abstract fun recentSearchDao(): RecentSearchDao
}
