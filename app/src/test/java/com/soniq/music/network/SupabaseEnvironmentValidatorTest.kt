package com.soniq.music.network

import com.soniq.music.core.config.SupabaseConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseEnvironmentValidatorTest {

    @Test
    fun `blank config reports Unconfigured`() {
        val config = SupabaseConfig(url = "", anonKey = "")
        assertFalse(config.isConfigured)
        assertEquals(
            SupabaseEnvironmentStatus.Unconfigured,
            SupabaseEnvironmentValidator.evaluate(config)
        )
    }

    @Test
    fun `non-https url reports InvalidUrlScheme`() {
        val config = SupabaseConfig(url = "http://example.supabase.co", anonKey = "test-anon-key")
        assertFalse(config.isConfigured)
        assertEquals(
            SupabaseEnvironmentStatus.InvalidUrlScheme,
            SupabaseEnvironmentValidator.evaluate(config)
        )
    }

    @Test
    fun `missing anon key reports MissingAnonKey`() {
        val config = SupabaseConfig(url = "https://example.supabase.co", anonKey = "   ")
        assertFalse(config.isConfigured)
        assertEquals(
            SupabaseEnvironmentStatus.MissingAnonKey,
            SupabaseEnvironmentValidator.evaluate(config)
        )
    }

    @Test
    fun `valid https url and anon key reports Configured`() {
        val config = SupabaseConfig(url = "https://example.supabase.co", anonKey = "public-anon-key")
        assertTrue(config.isConfigured)
        assertEquals(
            SupabaseEnvironmentStatus.Configured,
            SupabaseEnvironmentValidator.evaluate(config)
        )
    }
}
