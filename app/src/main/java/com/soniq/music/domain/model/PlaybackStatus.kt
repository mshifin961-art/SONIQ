package com.soniq.music.domain.model

enum class PlaybackStatus {
    Idle,
    Buffering,
    Playing,
    Paused,
    Error
}

enum class RepeatMode {
    Off,
    All,
    One
}
