package com.soniq.music.data.catalogue

import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.model.TrackAudioSource
import com.soniq.music.domain.model.TrackLicenseInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StarterLicenseManifest(
    val schemaVersion: Int,
    val collectionName: String,
    val note: String = "",
    val tracks: List<StarterLicenseTrackEntry>
)

@Serializable
data class StarterLicenseTrackEntry(
    val trackId: String,
    val title: String,
    val artistId: String,
    val artist: String,
    val albumId: String,
    val album: String,
    val category: String,
    val genreId: String,
    val durationMs: Long,
    val bundledAssetPath: String,
    val assetStatus: String,
    val sourceName: String,
    val sourceUrl: String,
    val licenseName: String,
    val licenseUrl: String,
    val attributionRequirements: String,
    val redistributionEvidence: String,
    val artworkLicense: String
)

data class StarterCollectionBundle(
    val genres: List<Genre>,
    val artists: List<Artist>,
    val albums: List<Album>,
    val tracks: List<Track>
)

object StarterLicenseManifestParser {
    private val json = Json { ignoreUnknownKeys = true }

    private val defaultGenres = listOf(
        Genre(id = "genre-electronic", name = "Electronic", moodTag = "Energetic", accentHex = "#3A86FF", sortOrder = 1),
        Genre(id = "genre-chill-lofi", name = "Chill / Lo-fi", moodTag = "Focus", accentHex = "#9D4EDD", sortOrder = 2),
        Genre(id = "genre-instrumental", name = "Instrumental", moodTag = "Timeless", accentHex = "#7B2CBF", sortOrder = 3),
        Genre(id = "genre-ambient", name = "Ambient", moodTag = "Atmospheric", accentHex = "#00D4FF", sortOrder = 4),
        Genre(id = "genre-pop-acoustic", name = "Pop / Acoustic", moodTag = "Warm", accentHex = "#FF2E93", sortOrder = 5)
    )

    fun parse(
        rawJson: String,
        assetExistsChecker: (String) -> Boolean = { false }
    ): StarterCollectionBundle {
        val manifest = json.decodeFromString<StarterLicenseManifest>(rawJson)

        val artists = manifest.tracks
            .distinctBy { it.artistId }
            .map { entry ->
                Artist(
                    id = entry.artistId,
                    name = entry.artist,
                    bio = "Featured in ${manifest.collectionName} (${entry.licenseName}).",
                    primaryGenreId = entry.genreId,
                    isFeatured = true
                )
            }

        val albums = manifest.tracks
            .distinctBy { it.albumId }
            .map { entry ->
                Album(
                    id = entry.albumId,
                    title = entry.album,
                    artistId = entry.artistId,
                    artistName = entry.artist,
                    genreId = entry.genreId,
                    isFeatured = true
                )
            }

        val tracks = manifest.tracks.mapIndexed { index, entry ->
            val binaryPresent = assetExistsChecker(entry.bundledAssetPath)
            Track(
                id = entry.trackId,
                title = entry.title,
                artistId = entry.artistId,
                artistName = entry.artist,
                albumId = entry.albumId,
                albumTitle = entry.album,
                genreId = entry.genreId,
                categoryName = entry.category,
                durationMs = entry.durationMs,
                trackNumber = index + 1,
                audioSource = TrackAudioSource.BundledAsset(
                    assetPath = entry.bundledAssetPath,
                    isBinaryBundled = binaryPresent
                ),
                isStarterTrack = true,
                licenseInfo = TrackLicenseInfo(
                    sourceName = entry.sourceName,
                    sourceUrl = entry.sourceUrl,
                    licenseName = entry.licenseName,
                    licenseUrl = entry.licenseUrl,
                    attributionRequirements = entry.attributionRequirements,
                    redistributionEvidence = entry.redistributionEvidence,
                    artworkLicense = entry.artworkLicense
                )
            )
        }

        return StarterCollectionBundle(
            genres = defaultGenres,
            artists = artists,
            albums = albums,
            tracks = tracks
        )
    }
}
