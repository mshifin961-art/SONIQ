package com.soniq.music.data.preferences

import app.cash.turbine.test
import com.soniq.music.domain.model.AtmosphereAccent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryAtmospherePreferencesRepositoryTest {

    @Test
    fun `default accent is DynamicPurple and updates emit reactively`() = runTest {
        val repository = InMemoryAtmospherePreferencesRepository()

        repository.activeAccent.test {
            assertEquals(AtmosphereAccent.DynamicPurple, awaitItem())

            repository.setAccent(AtmosphereAccent.ElectricBlue)
            assertEquals(AtmosphereAccent.ElectricBlue, awaitItem())

            repository.setAccent(AtmosphereAccent.NeonPink)
            assertEquals(AtmosphereAccent.NeonPink, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
