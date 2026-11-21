package com.soniq.music.audio

import com.soniq.music.domain.model.PlaybackStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSnapshotTest {

    @Test
    fun `default snapshot has no active media`() {
        val snapshot = PlaybackSnapshot()
        assertFalse(snapshot.hasActiveMedia)
    }

    @Test
    fun `snapshot with track id and playing status reports active media`() {
        val snapshot = PlaybackSnapshot(
            status = PlaybackStatus.Playing,
            activeTrackId = "starter-01",
            activeTitle = "Sample",
            activeArtist = "Artist"
        )
        assertTrue(snapshot.hasActiveMedia)
    }
}
