package com.soniq.music.di

import android.content.Context
import androidx.room.Room
import com.soniq.music.database.SoniqDatabase
import com.soniq.music.database.SoniqDatabaseContract
import com.soniq.music.database.dao.CatalogueDao
import com.soniq.music.database.dao.RecentSearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSoniqDatabase(
        @ApplicationContext context: Context
    ): SoniqDatabase = Room.databaseBuilder(
        context,
        SoniqDatabase::class.java,
        SoniqDatabaseContract.DATABASE_NAME
    ).build()

    @Provides
    fun provideCatalogueDao(database: SoniqDatabase): CatalogueDao = database.catalogueDao()

    @Provides
    fun provideRecentSearchDao(database: SoniqDatabase): RecentSearchDao = database.recentSearchDao()
}
