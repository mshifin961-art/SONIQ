package com.soniq.music.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatterTest {

    @Test
    fun `formatDurationMs formats seconds minutes and hours accurately`() {
        assertEquals("0:00", TimeFormatter.formatDurationMs(0L))
        assertEquals("0:00", TimeFormatter.formatDurationMs(-5_000L))
        assertEquals("0:09", TimeFormatter.formatDurationMs(9_000L))
        assertEquals("3:45", TimeFormatter.formatDurationMs(225_000L))
        assertEquals("1:05:09", TimeFormatter.formatDurationMs(3_909_000L))
    }

    @Test
    fun `greetingForHour returns time-appropriate greeting`() {
        assertEquals("Good morning", TimeFormatter.greetingForHour(7))
        assertEquals("Good afternoon", TimeFormatter.greetingForHour(14))
        assertEquals("Good evening", TimeFormatter.greetingForHour(19))
        assertEquals("Late night frequencies", TimeFormatter.greetingForHour(23))
        assertEquals("Late night frequencies", TimeFormatter.greetingForHour(2))
    }
}
