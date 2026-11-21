package com.soniq.music.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soniq.music.ui.components.SoniqAtmosphereBackground
import com.soniq.music.ui.components.SoniqBottomBar
import com.soniq.music.ui.feature.auth.AuthScreen
import com.soniq.music.ui.feature.explore.ExploreScreen
import com.soniq.music.ui.feature.home.HomeScreen
import com.soniq.music.ui.feature.library.LibraryScreen
import com.soniq.music.ui.feature.profile.ProfileScreen
import com.soniq.music.ui.feature.search.SearchScreen
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.SoniqColors
import com.soniq.music.ui.theme.SoniqTheme

@Composable
fun SoniqAppShell(
    viewModel: SoniqShellViewModel = hiltViewModel()
) {
    val activeAccent by viewModel.activeAccent.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val isAuthRoute = backStackEntry?.destination?.hasRoute(SoniqDestination.Auth::class) == true
    val currentDestination = SoniqNavigationSpec.primaryDestinations
        .firstOrNull { item ->
            backStackEntry?.destination?.hasRoute(item.route::class) == true
        }
        ?.route ?: SoniqDestination.Home

    SoniqTheme(accent = activeAccent) {
        SoniqAtmosphereBackground {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp
                val atmosphere = LocalDynamicAtmosphere.current

                if (useNavigationRail) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (!isAuthRoute) {
                            NavigationRail(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .statusBarsPadding(),
                                containerColor = SoniqColors.DeepNavy.copy(alpha = 0.85f),
                                contentColor = SoniqColors.TextPrimary
                            ) {
                                SoniqNavigationSpec.primaryDestinations.forEach { item ->
                                    val selected = currentDestination == item.route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navController.navigatePrimary(item.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = { Text(item.label) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = atmosphere.primaryAccent,
                                            selectedTextColor = SoniqColors.TextPrimary,
                                            indicatorColor = atmosphere.primaryAccent.copy(alpha = 0.22f),
                                            unselectedIconColor = SoniqColors.TextMuted,
                                            unselectedTextColor = SoniqColors.TextMuted
                                        )
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            SoniqNavHost(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                } else {
                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = {
                            if (!isAuthRoute) {
                                SoniqBottomBar(
                                    destinations = SoniqNavigationSpec.primaryDestinations,
                                    currentDestination = currentDestination,
                                    onNavigate = { destination ->
                                        navController.navigatePrimary(destination)
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        SoniqNavHost(
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoniqNavHost(
    navController: NavHostController,
    viewModel: SoniqShellViewModel,
    modifier: Modifier = Modifier
) {
    val activeAccent by viewModel.activeAccent.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = SoniqDestination.Home,
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        composable<SoniqDestination.Home> {
            HomeScreen(
                sessionState = sessionState,
                onOpenExplore = { navController.navigatePrimary(SoniqDestination.Explore) },
                onOpenLibrary = { navController.navigatePrimary(SoniqDestination.Library) },
                onOpenAuth = { navController.navigate(SoniqDestination.Auth) { launchSingleTop = true } }
            )
        }
        composable<SoniqDestination.Explore> {
            ExploreScreen()
        }
        composable<SoniqDestination.Search> {
            SearchScreen()
        }
        composable<SoniqDestination.Library> {
            LibraryScreen()
        }
        composable<SoniqDestination.Profile> {
            ProfileScreen(
                activeAccent = activeAccent,
                onSelectAccent = viewModel::selectAccent,
                onOpenAuth = { navController.navigate(SoniqDestination.Auth) { launchSingleTop = true } },
                appConfig = viewModel.appConfig,
                supabaseStatus = viewModel.supabaseStatus
            )
        }
        composable<SoniqDestination.Auth> {
            AuthScreen(
                onAuthenticated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun NavHostController.navigatePrimary(destination: SoniqDestination) {
    navigate(destination) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
