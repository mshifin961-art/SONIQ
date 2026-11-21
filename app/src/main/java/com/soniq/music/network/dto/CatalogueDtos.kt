package com.soniq.music.network.dto

import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.model.TrackAudioSource
import com.soniq.music.domain.model.TrackLicenseInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenreDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("mood_tag") val moodTag: String = "",
    @SerialName("accent_hex") val accentHex: String = "#9D4EDD",
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class ArtistDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("bio") val bio: String = "",
    @SerialName("artwork_url") val artworkUrl: String? = null,
    @SerialName("primary_genre_id") val primaryGenreId: String? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false
)

@Serializable
data class AlbumDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("artist_id") val artistId: String,
    @SerialName("genre_id") val genreId: String? = null,
    @SerialName("release_year") val releaseYear: Int? = null,
    @SerialName("artwork_url") val artworkUrl: String? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false
)

@Serializable
data class TrackDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("artist_id") val artistId: String,
    @SerialName("album_id") val albumId: String? = null,
    @SerialName("genre_id") val genreId: String? = null,
    @SerialName("duration_ms") val durationMs: Long,
    @SerialName("track_number") val trackNumber: Int = 1,
    @SerialName("stream_url") val streamUrl: String,
    @SerialName("artwork_url") val artworkUrl: String? = null,
    @SerialName("license_name") val licenseName: String,
    @SerialName("license_url") val licenseUrl: String
)

fun GenreDto.toDomain(): Genre = Genre(
    id = id,
    name = name,
    moodTag = moodTag,
    accentHex = accentHex,
    sortOrder = sortOrder
)

fun ArtistDto.toDomain(): Artist = Artist(
    id = id,
    name = name,
    bio = bio,
    artworkUrl = artworkUrl?.takeIf { it.isNotBlank() },
    primaryGenreId = primaryGenreId,
    isFeatured = isFeatured
)

fun AlbumDto.toDomain(artistName: String): Album = Album(
    id = id,
    title = title,
    artistId = artistId,
    artistName = artistName,
    genreId = genreId,
    releaseYear = releaseYear,
    artworkUrl = artworkUrl?.takeIf { it.isNotBlank() },
    isFeatured = isFeatured
)

fun TrackDto.toDomain(
    artistName: String,
    albumTitle: String,
    categoryName: String
): Track = Track(
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
    artworkUrl = artworkUrl?.takeIf { it.isNotBlank() },
    audioSource = TrackAudioSource.RemoteStream(streamUrl = streamUrl),
    isStarterTrack = false,
    licenseInfo = TrackLicenseInfo(
        sourceName = "SONIQ Authorized Catalogue",
        sourceUrl = licenseUrl,
        licenseName = licenseName,
        licenseUrl = licenseUrl,
        attributionRequirements = "Authorized streaming metadata.",
        redistributionEvidence = "Served via authorized SONIQ catalogue backend.",
        artworkLicense = "Authorized catalogue artwork."
    )
)
