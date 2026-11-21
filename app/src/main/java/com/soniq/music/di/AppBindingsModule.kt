package com.soniq.music.di

import com.soniq.music.audio.NoOpPlaybackController
import com.soniq.music.audio.PlaybackController
import com.soniq.music.data.auth.AuthRemoteDataSource
import com.soniq.music.data.auth.SupabaseAuthRemoteDataSource
import com.soniq.music.data.auth.SupabaseAuthRepository
import com.soniq.music.data.catalogue.CatalogueLocalDataSource
import com.soniq.music.data.catalogue.CatalogueRemoteDataSource
import com.soniq.music.data.catalogue.OfflineFirstCatalogueRepository
import com.soniq.music.data.catalogue.RoomCatalogueLocalDataSource
import com.soniq.music.data.catalogue.StarterCollectionAssetLoader
import com.soniq.music.data.catalogue.StarterCollectionLoader
import com.soniq.music.data.catalogue.SupabaseCatalogueRemoteDataSource
import com.soniq.music.data.preferences.InMemoryAtmospherePreferencesRepository
import com.soniq.music.data.profile.ProfileRemoteDataSource
import com.soniq.music.data.profile.SupabaseProfileRemoteDataSource
import com.soniq.music.data.profile.SupabaseProfileRepository
import com.soniq.music.domain.repository.AtmospherePreferencesRepository
import com.soniq.music.domain.repository.AuthRepository
import com.soniq.music.domain.repository.CatalogueRepository
import com.soniq.music.domain.repository.ProfileRepository
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

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: SupabaseAuthRemoteDataSource
    ): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: SupabaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRemoteDataSource(
        impl: SupabaseProfileRemoteDataSource
    ): ProfileRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: SupabaseProfileRepository
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindStarterCollectionLoader(
        impl: StarterCollectionAssetLoader
    ): StarterCollectionLoader

    @Binds
    @Singleton
    abstract fun bindCatalogueLocalDataSource(
        impl: RoomCatalogueLocalDataSource
    ): CatalogueLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCatalogueRemoteDataSource(
        impl: SupabaseCatalogueRemoteDataSource
    ): CatalogueRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCatalogueRepository(
        impl: OfflineFirstCatalogueRepository
    ): CatalogueRepository
}
