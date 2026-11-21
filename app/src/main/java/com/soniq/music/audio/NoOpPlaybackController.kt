package com.soniq.music.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Honest idle Phase 1 binding for [PlaybackController].
 * Keeps the player state strictly Idle until the Media3 engine is wired in Phase 4.
 */
@Singleton
class NoOpPlaybackController @Inject constructor() : PlaybackController {
    private val _snapshot = MutableStateFlow(PlaybackSnapshot())
    override val snapshot: StateFlow<PlaybackSnapshot> = _snapshot.asStateFlow()

    override fun play() = Unit
    override fun pause() = Unit
    override fun skipNext() = Unit
    override fun skipPrevious() = Unit
    override fun seekTo(positionMs: Long) = Unit
}
