package com.soniq.music.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soniq.music.database.SoniqDatabaseContract
import com.soniq.music.database.entity.AlbumEntity
import com.soniq.music.database.entity.ArtistEntity
import com.soniq.music.database.entity.GenreEntity
import com.soniq.music.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogueDao {

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.GENRES} ORDER BY sort_order ASC, name ASC")
    fun observeGenres(): Flow<List<GenreEntity>>

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.ARTISTS} ORDER BY is_featured DESC, name ASC")
    fun observeArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.ALBUMS} ORDER BY is_featured DESC, title ASC")
    fun observeAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.TRACKS} WHERE is_starter_track = 1 ORDER BY id ASC")
    fun observeStarterTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.TRACKS} ORDER BY is_starter_track DESC, title ASC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.TRACKS}")
    suspend fun getAllTracksOnce(): List<TrackEntity>

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.ARTISTS}")
    suspend fun getAllArtistsOnce(): List<ArtistEntity>

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.ALBUMS}")
    suspend fun getAllAlbumsOnce(): List<AlbumEntity>

    @Query("SELECT * FROM ${SoniqDatabaseContract.Tables.GENRES}")
    suspend fun getAllGenresOnce(): List<GenreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArtists(artists: List<ArtistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlbums(albums: List<AlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<TrackEntity>)
}
