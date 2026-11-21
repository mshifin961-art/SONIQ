package com.soniq.music.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * UI-independent contract for controlling SONIQ's audio playback engine.
 * Implemented by AndroidX Media3 + MediaSessionService in Phase 4.
 */
interface PlaybackController {
    val snapshot: StateFlow<PlaybackSnapshot>
    fun play()
    fun pause()
    fun skipNext()
    fun skipPrevious()
    fun seekTo(positionMs: Long)
}
