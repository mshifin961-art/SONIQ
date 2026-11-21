package com.soniq.music.core.config

/**
 * Immutable runtime configuration for the SONIQ Android client.
 */
data class AppConfig(
    val applicationId: String,
    val versionName: String,
    val versionCode: Int,
    val phaseLabel: String,
    val isDebug: Boolean,
    val supabase: SupabaseConfig
)
