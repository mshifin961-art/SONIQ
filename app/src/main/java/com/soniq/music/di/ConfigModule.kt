package com.soniq.music.di

import com.soniq.music.BuildConfig
import com.soniq.music.core.config.AppConfig
import com.soniq.music.core.config.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig = SupabaseConfig(
        url = BuildConfig.SUPABASE_URL,
        anonKey = BuildConfig.SUPABASE_ANON_KEY
    )

    @Provides
    @Singleton
    fun provideAppConfig(
        supabaseConfig: SupabaseConfig
    ): AppConfig = AppConfig(
        applicationId = BuildConfig.APPLICATION_ID,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        phaseLabel = BuildConfig.SONIQ_PHASE,
        isDebug = BuildConfig.DEBUG,
        supabase = supabaseConfig
    )
}
