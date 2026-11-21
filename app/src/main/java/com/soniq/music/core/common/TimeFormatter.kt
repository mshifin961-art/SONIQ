package com.soniq.music.core.common

/**
 * Pure formatting utilities for playback durations and time-of-day greetings.
 */
object TimeFormatter {

    /**
     * Formats a duration in milliseconds into `m:ss` or `h:mm:ss`.
     * Negative values are clamped to `0:00`.
     */
    fun formatDurationMs(durationMs: Long): String {
        val safeSeconds = (durationMs.coerceAtLeast(0L)) / 1_000L
        val hours = safeSeconds / 3_600L
        val minutes = (safeSeconds % 3_600L) / 60L
        val seconds = safeSeconds % 60L

        return if (hours > 0L) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }

    /**
     * Returns a natural, time-aware greeting for the Home dashboard.
     */
    fun greetingForHour(hourOfDay: Int): String {
        val normalized = hourOfDay.coerceIn(0, 23)
        return when (normalized) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Late night frequencies"
        }
    }
}
