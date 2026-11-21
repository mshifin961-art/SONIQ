package com.soniq.music.network

import com.soniq.music.core.config.SupabaseConfig

/**
 * Evaluates build-injected Supabase environment readiness without performing network I/O
 * or exposing credential values.
 */
enum class SupabaseEnvironmentStatus(val label: String, val detail: String) {
    Configured(
        label = "Environment Configured",
        detail = "Supabase URL and anon key detected via BuildConfig."
    ),
    Unconfigured(
        label = "Not Configured",
        detail = "Provide SUPABASE_URL and SUPABASE_ANON_KEY in local.properties before Phase 2."
    ),
    MissingUrl(
        label = "Missing Project URL",
        detail = "SUPABASE_URL is blank in local.properties or CI environment."
    ),
    MissingAnonKey(
        label = "Missing Anon Key",
        detail = "SUPABASE_ANON_KEY is blank in local.properties or CI environment."
    ),
    InvalidUrlScheme(
        label = "Invalid URL Scheme",
        detail = "SUPABASE_URL must start with https://."
    )
}

object SupabaseEnvironmentValidator {
    fun evaluate(config: SupabaseConfig): SupabaseEnvironmentStatus = when {
        !config.hasUrl && !config.hasAnonKey -> SupabaseEnvironmentStatus.Unconfigured
        !config.hasUrl -> SupabaseEnvironmentStatus.MissingUrl
        !config.hasValidHttpsUrl -> SupabaseEnvironmentStatus.InvalidUrlScheme
        !config.hasAnonKey -> SupabaseEnvironmentStatus.MissingAnonKey
        else -> SupabaseEnvironmentStatus.Configured
    }
}
