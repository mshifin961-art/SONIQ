package com.soniq.music.core.state

import com.soniq.music.core.common.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStateTest {

    @Test
    fun `UiState helpers expose loading and data states accurately`() {
        val loading: UiState<String> = UiState.Loading
        assertTrue(loading.isLoading)
        assertNull(loading.dataOrNull())

        val success: UiState<String> = UiState.Success("Ready")
        assertFalse(success.isLoading)
        assertEquals("Ready", success.dataOrNull())
    }

    @Test
    fun `map transforms Success and preserves Empty and Error`() {
        val mapped = UiState.Success(5).map { it + 5 }
        assertEquals(10, mapped.dataOrNull())

        val empty: UiState<Int> = UiState.Empty(title = "Empty", subtitle = "Nothing here")
        assertEquals(empty, empty.map { it + 5 })

        val error: UiState<Int> = UiState.Error(AppError.Network("Offline"))
        assertEquals(error, error.map { it + 5 })
    }
}
