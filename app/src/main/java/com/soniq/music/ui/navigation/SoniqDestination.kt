package com.soniq.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * Type-safe top-level navigation destinations for SONIQ.
 */
@Serializable
sealed interface SoniqDestination {
    @Serializable
    data object Home : SoniqDestination

    @Serializable
    data object Explore : SoniqDestination

    @Serializable
    data object Search : SoniqDestination

    @Serializable
    data object Library : SoniqDestination

    @Serializable
    data object Profile : SoniqDestination
}

data class PrimaryDestinationItem(
    val route: SoniqDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

object SoniqNavigationSpec {
    val primaryDestinations: List<PrimaryDestinationItem> = listOf(
        PrimaryDestinationItem(
            route = SoniqDestination.Home,
            label = "Home",
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        PrimaryDestinationItem(
            route = SoniqDestination.Explore,
            label = "Explore",
            selectedIcon = Icons.Rounded.Explore,
            unselectedIcon = Icons.Outlined.Explore
        ),
        PrimaryDestinationItem(
            route = SoniqDestination.Search,
            label = "Search",
            selectedIcon = Icons.Rounded.Search,
            unselectedIcon = Icons.Outlined.Search
        ),
        PrimaryDestinationItem(
            route = SoniqDestination.Library,
            label = "Library",
            selectedIcon = Icons.Rounded.LibraryMusic,
            unselectedIcon = Icons.Outlined.LibraryMusic
        ),
        PrimaryDestinationItem(
            route = SoniqDestination.Profile,
            label = "Profile",
            selectedIcon = Icons.Rounded.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )
}
