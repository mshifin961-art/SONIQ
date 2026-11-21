package com.soniq.music.di

import com.soniq.music.core.common.DispatcherProvider
import com.soniq.music.core.common.SoniqDispatcher
import com.soniq.music.core.common.SoniqDispatcherType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @SoniqDispatcher(SoniqDispatcherType.IO)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @SoniqDispatcher(SoniqDispatcherType.Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @SoniqDispatcher(SoniqDispatcherType.Main)
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate

    @Provides
    @Singleton
    fun provideDispatcherProvider(
        @SoniqDispatcher(SoniqDispatcherType.IO) ioDispatcher: CoroutineDispatcher,
        @SoniqDispatcher(SoniqDispatcherType.Default) defaultDispatcher: CoroutineDispatcher,
        @SoniqDispatcher(SoniqDispatcherType.Main) mainDispatcher: CoroutineDispatcher
    ): DispatcherProvider = object : DispatcherProvider {
        override val io: CoroutineDispatcher = ioDispatcher
        override val default: CoroutineDispatcher = defaultDispatcher
        override val main: CoroutineDispatcher = mainDispatcher
    }
}
