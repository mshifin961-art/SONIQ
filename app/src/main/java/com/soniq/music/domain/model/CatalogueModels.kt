package com.soniq.music.domain.model

/**
 * Core SONIQ Music Catalogue domain models.
 */
data class Genre(
    val id: String,
    val name: String,
    val moodTag: String = "",
    val accentHex: String = "#9D4EDD",
    val sortOrder: Int = 0
)

data class Artist(
    val id: String,
    val name: String,
    val bio: String = "",
    val artworkUrl: String? = null,
    val primaryGenreId: String? = null,
    val isFeatured: Boolean = false
)

data class Album(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val genreId: String? = null,
    val releaseYear: Int? = null,
    val artworkUrl: String? = null,
    val isFeatured: Boolean = false
)

data class TrackLicenseInfo(
    val sourceName: String,
    val sourceUrl: String,
    val licenseName: String,
    val licenseUrl: String,
    val attributionRequirements: String,
    val redistributionEvidence: String,
    val artworkLicense: String
)

sealed interface TrackAudioSource {
    data class BundledAsset(
        val assetPath: String,
        val isBinaryBundled: Boolean
    ) : TrackAudioSource

    data class RemoteStream(
        val streamUrl: String
    ) : TrackAudioSource
}

data class Track(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String?,
    val albumTitle: String,
    val genreId: String?,
    val categoryName: String,
    val durationMs: Long,
    val trackNumber: Int = 1,
    val artworkUrl: String? = null,
    val audioSource: TrackAudioSource,
    val isStarterTrack: Boolean = false,
    val licenseInfo: TrackLicenseInfo? = null
) {
    val isPlayableOffline: Boolean
        get() = audioSource is TrackAudioSource.BundledAsset && audioSource.isBinaryBundled
}
