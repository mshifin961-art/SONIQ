package com.soniq.music.network.dto

import com.soniq.music.database.entity.toDomain
import com.soniq.music.database.entity.toEntity
import com.soniq.music.domain.model.TrackAudioSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogueDtosMapperTest {

    @Test
    fun `TrackDto maps to RemoteStream domain model and round-trips through TrackEntity`() {
        val dto = TrackDto(
            id = "remote-01",
            title = "Midnight Horizon",
            artistId = "art-1",
            albumId = "alb-1",
            genreId = "g-ambient",
            durationMs = 215_000,
            trackNumber = 2,
            streamUrl = "https://cdn.example.com/audio/midnight.mp3",
            artworkUrl = "https://cdn.example.com/art/midnight.jpg",
            licenseName = "CC0 1.0 Universal",
            licenseUrl = "https://creativecommons.org/publicdomain/zero/1.0/"
        )

        val domain = dto.toDomain(
            artistName = "Studio Artist",
            albumTitle = "Horizon EP",
            categoryName = "Ambient"
        )
        assertFalse(domain.isStarterTrack)
        assertTrue(domain.audioSource is TrackAudioSource.RemoteStream)

        val roundTrip = domain.toEntity().toDomain()
        assertEquals(domain.id, roundTrip.id)
        assertEquals(domain.title, roundTrip.title)
        assertEquals(domain.audioSource, roundTrip.audioSource)
        assertEquals(domain.licenseInfo?.licenseName, roundTrip.licenseInfo?.licenseName)
    }
}
