package com.soniq.music.domain.model

/**
 * Audio streaming and playback quality tiers used across Player and Audio Studio.
 */
enum class AudioQualityTier(
    val label: String,
    val detail: String
) {
    Standard(label = "Standard", detail = "128 kbps AAC"),
    High(label = "High", detail = "256 kbps AAC"),
    Studio(label = "Studio", detail = "320 kbps / Lossless when available")
}
