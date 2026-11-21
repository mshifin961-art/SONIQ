package com.soniq.music.core.common

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {

    @Test
    fun `map transforms Success value and preserves Error`() {
        val success: AppResult<Int> = AppResult.Success(21)
        val mapped = success.map { it * 2 }

        assertTrue(mapped.isSuccess)
        assertEquals(42, mapped.getOrNull())
        assertNull(mapped.errorOrNull())

        val error: AppResult<Int> = AppResult.Error(AppError.NotFound("Missing item"))
        val mappedError = error.map { it * 2 }

        assertTrue(mappedError.isError)
        assertNull(mappedError.getOrNull())
        assertEquals("Missing item", mappedError.errorOrNull()?.message)
    }

    @Test
    fun `flatMap chains Success results`() {
        val result = AppResult.Success("soniq")
            .flatMap { AppResult.Success(it.uppercase()) }

        assertEquals("SONIQ", result.getOrNull())
    }

    @Test
    fun `runCatchingResult wraps standard exceptions as Unexpected error`() {
        val result = runCatchingResult<String> {
            throw IllegalStateException("Boom")
        }

        assertTrue(result.isError)
        val error = result.errorOrNull()
        assertTrue(error is AppError.Unexpected)
        assertEquals("Boom", error?.message)
    }

    @Test(expected = CancellationException::class)
    fun `runCatchingResult rethrows CancellationException`() {
        runCatchingResult {
            throw CancellationException("Cancelled")
        }
    }
}
