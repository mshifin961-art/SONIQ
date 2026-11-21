package com.soniq.music.core.common

import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier

enum class SoniqDispatcherType {
    IO,
    Default,
    Main
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SoniqDispatcher(val dispatcher: SoniqDispatcherType)

/**
 * Abstraction over coroutine dispatchers for deterministic unit testing.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
