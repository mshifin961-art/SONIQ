package com.soniq.music.network

import com.soniq.music.core.config.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lazily initializes and exposes a singleton [SupabaseClient] when valid
 * BuildConfig credentials are present. Returns null safely when unconfigured
 * so SONIQ can launch offline without crashing.
 *
 * Session persistence uses supabase-kt's Android default SettingsSessionManager
 * (backed by private SharedPreferences) with explicit auto-load, auto-save,
 * and proactive token refresh enabled.
 */
@Singleton
class SupabaseClientProvider @Inject constructor(
    private val config: SupabaseConfig
) {
    val isConfigured: Boolean
        get() = config.isConfigured

    val client: SupabaseClient? by lazy {
        if (!config.isConfigured) {
            null
        } else {
            createSupabaseClient(
                supabaseUrl = config.url,
                supabaseKey = config.anonKey
            ) {
                install(Auth) {
                    autoLoadFromStorage = true
                    autoSaveToStorage = true
                    alwaysAutoRefresh = true
                }
                install(Postgrest)
            }
        }
    }
}
