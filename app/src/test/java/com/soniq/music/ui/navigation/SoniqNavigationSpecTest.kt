package com.soniq.music.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class SoniqNavigationSpecTest {

    @Test
    fun `primary navigation contains exactly five ordered destinations`() {
        val labels = SoniqNavigationSpec.primaryDestinations.map { it.label }
        assertEquals(
            listOf("Home", "Explore", "Search", "Library", "Profile"),
            labels
        )
    }

    @Test
    fun `primary navigation routes are unique`() {
        val routes = SoniqNavigationSpec.primaryDestinations.map { it.route }.toSet()
        assertEquals(5, routes.size)
    }
}
