package com.soniq.music.core.config

/**
 * Holds Supabase connection parameters injected via BuildConfig from local.properties
 * or CI environment variables. Never contains hardcoded secrets or service role keys.
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String
) {
    val hasUrl: Boolean
        get() = url.isNotBlank()

    val hasAnonKey: Boolean
        get() = anonKey.isNotBlank()

    val hasValidHttpsUrl: Boolean
        get() = url.startsWith("https://", ignoreCase = true) && url.length > "https://".length

    val isConfigured: Boolean
        get() = hasValidHttpsUrl && hasAnonKey
}
