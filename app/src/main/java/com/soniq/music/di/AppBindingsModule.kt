package com.soniq.music.di

import com.soniq.music.audio.NoOpPlaybackController
import com.soniq.music.audio.PlaybackController
import com.soniq.music.data.preferences.InMemoryAtmospherePreferencesRepository
import com.soniq.music.domain.repository.AtmospherePreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAtmospherePreferencesRepository(
        impl: InMemoryAtmospherePreferencesRepository
    ): AtmospherePreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackController(
        impl: NoOpPlaybackController
    ): PlaybackController
}
