package com.soniq.music.audio

import com.soniq.music.domain.model.PlaybackStatus
import com.soniq.music.domain.model.RepeatMode

/**
 * Immutable snapshot of playback state owned by the audio engine layer,
 * decoupled from individual Compose screens and Activity recreation.
 */
data class PlaybackSnapshot(
    val status: PlaybackStatus = PlaybackStatus.Idle,
    val activeTrackId: String? = null,
    val activeTitle: String? = null,
    val activeArtist: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off
) {
    val hasActiveMedia: Boolean
        get() = activeTrackId != null && status != PlaybackStatus.Idle
}
