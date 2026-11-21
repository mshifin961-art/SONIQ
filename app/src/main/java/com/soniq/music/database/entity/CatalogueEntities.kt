package com.soniq.music.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soniq.music.database.SoniqDatabaseContract
import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.RecentSearchQuery
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.model.TrackAudioSource
import com.soniq.music.domain.model.TrackLicenseInfo

@Entity(tableName = SoniqDatabaseContract.Tables.GENRES)
data class GenreEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "mood_tag") val moodTag: String,
    @ColumnInfo(name = "accent_hex") val accentHex: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int
)

@Entity(tableName = SoniqDatabaseContract.Tables.ARTISTS)
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bio: String,
    @ColumnInfo(name = "artwork_url") val artworkUrl: String?,
    @ColumnInfo(name = "primary_genre_id") val primaryGenreId: String?,
    @ColumnInfo(name = "is_featured") val isFeatured: Boolean
)

@Entity(tableName = SoniqDatabaseContract.Tables.ALBUMS)
data class AlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "artist_id") val artistId: String,
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "genre_id") val genreId: String?,
    @ColumnInfo(name = "release_year") val releaseYear: Int?,
    @ColumnInfo(name = "artwork_url") val artworkUrl: String?,
    @ColumnInfo(name = "is_featured") val isFeatured: Boolean
)

@Entity(tableName = SoniqDatabaseContract.Tables.TRACKS)
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "artist_id") val artistId: String,
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "album_id") val albumId: String?,
    @ColumnInfo(name = "album_title") val albumTitle: String,
    @ColumnInfo(name = "genre_id") val genreId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "track_number") val trackNumber: Int,
    @ColumnInfo(name = "artwork_url") val artworkUrl: String?,
    @ColumnInfo(name = "stream_url") val streamUrl: String?,
    @ColumnInfo(name = "bundled_asset_path") val bundledAssetPath: String?,
    @ColumnInfo(name = "is_binary_bundled") val isBinaryBundled: Boolean,
    @ColumnInfo(name = "is_starter_track") val isStarterTrack: Boolean,
    @ColumnInfo(name = "license_source_name") val licenseSourceName: String?,
    @ColumnInfo(name = "license_source_url") val licenseSourceUrl: String?,
    @ColumnInfo(name = "license_name") val licenseName: String?,
    @ColumnInfo(name = "license_url") val licenseUrl: String?,
    @ColumnInfo(name = "attribution_requirements") val attributionRequirements: String?,
    @ColumnInfo(name = "redistribution_evidence") val redistributionEvidence: String?,
    @ColumnInfo(name = "artwork_license") val artworkLicense: String?
)

@Entity(tableName = SoniqDatabaseContract.Tables.RECENT_SEARCHES)
data class RecentSearchEntity(
    @PrimaryKey val normalizedQuery: String,
    @ColumnInfo(name = "display_query") val displayQuery: String,
    @ColumnInfo(name = "searched_at_epoch_ms") val searchedAtEpochMs: Long
)

fun GenreEntity.toDomain(): Genre = Genre(id, name, moodTag, accentHex, sortOrder)
fun Genre.toEntity(): GenreEntity = GenreEntity(id, name, moodTag, accentHex, sortOrder)

fun ArtistEntity.toDomain(): Artist = Artist(id, name, bio, artworkUrl, primaryGenreId, isFeatured)
fun Artist.toEntity(): ArtistEntity = ArtistEntity(id, name, bio, artworkUrl, primaryGenreId, isFeatured)

fun AlbumEntity.toDomain(): Album = Album(id, title, artistId, artistName, genreId, releaseYear, artworkUrl, isFeatured)
fun Album.toEntity(): AlbumEntity = AlbumEntity(id, title, artistId, artistName, genreId, releaseYear, artworkUrl, isFeatured)

fun TrackEntity.toDomain(): Track {
    val source = if (bundledAssetPath != null) {
        TrackAudioSource.BundledAsset(
            assetPath = bundledAssetPath,
            isBinaryBundled = isBinaryBundled
        )
    } else {
        TrackAudioSource.RemoteStream(streamUrl = streamUrl.orEmpty())
    }
    val license = if (!licenseName.isNullOrBlank() && !licenseUrl.isNullOrBlank()) {
        TrackLicenseInfo(
            sourceName = licenseSourceName.orEmpty(),
            sourceUrl = licenseSourceUrl.orEmpty(),
            licenseName = licenseName,
            licenseUrl = licenseUrl,
            attributionRequirements = attributionRequirements.orEmpty(),
            redistributionEvidence = redistributionEvidence.orEmpty(),
            artworkLicense = artworkLicense.orEmpty()
        )
    } else {
        null
    }
    return Track(
        id = id,
        title = title,
        artistId = artistId,
        artistName = artistName,
        albumId = albumId,
        albumTitle = albumTitle,
        genreId = genreId,
        categoryName = categoryName,
        durationMs = durationMs,
        trackNumber = trackNumber,
        artworkUrl = artworkUrl,
        audioSource = source,
        isStarterTrack = isStarterTrack,
        licenseInfo = license
    )
}

fun Track.toEntity(): TrackEntity {
    val bundled = audioSource as? TrackAudioSource.BundledAsset
    val remote = audioSource as? TrackAudioSource.RemoteStream
    return TrackEntity(
        id = id,
        title = title,
        artistId = artistId,
        artistName = artistName,
        albumId = albumId,
        albumTitle = albumTitle,
        genreId = genreId,
        categoryName = categoryName,
        durationMs = durationMs,
        trackNumber = trackNumber,
        artworkUrl = artworkUrl,
        streamUrl = remote?.streamUrl,
        bundledAssetPath = bundled?.assetPath,
        isBinaryBundled = bundled?.isBinaryBundled ?: false,
        isStarterTrack = isStarterTrack,
        licenseSourceName = licenseInfo?.sourceName,
        licenseSourceUrl = licenseInfo?.sourceUrl,
        licenseName = licenseInfo?.licenseName,
        licenseUrl = licenseInfo?.licenseUrl,
        attributionRequirements = licenseInfo?.attributionRequirements,
        redistributionEvidence = licenseInfo?.redistributionEvidence,
        artworkLicense = licenseInfo?.artworkLicense
    )
}

fun RecentSearchEntity.toDomain(): RecentSearchQuery =
    RecentSearchQuery(query = displayQuery, searchedAtEpochMs = searchedAtEpochMs)
