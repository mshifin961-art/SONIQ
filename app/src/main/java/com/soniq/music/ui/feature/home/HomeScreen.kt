package com.soniq.music.ui.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.SpatialAudioOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soniq.music.core.common.TimeFormatter
import com.soniq.music.domain.model.AuthSessionState
import com.soniq.music.domain.model.Track
import com.soniq.music.ui.components.SoniqBrandMark
import com.soniq.music.ui.components.SoniqEmptyState
import com.soniq.music.ui.components.SoniqGlassCard
import com.soniq.music.ui.components.SoniqLoadingView
import com.soniq.music.ui.components.SoniqPillChip
import com.soniq.music.ui.components.SoniqTrackRow
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors
import java.util.Calendar

@Composable
fun HomeScreen(
    sessionState: AuthSessionState,
    starterTracks: List<Track>,
    isLoadingCatalogue: Boolean,
    onOpenExplore: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSoniqSpacing.current
    val atmosphere = LocalDynamicAtmosphere.current
    val baseGreeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        TimeFormatter.greetingForHour(hour)
    }
    val greeting = when (sessionState) {
        is AuthSessionState.Authenticated -> {
            val firstName = sessionState.user.displayName
                ?.trim()
                ?.substringBefore(" ")
                ?.takeIf { it.isNotEmpty() }
            if (firstName != null) "$baseGreeting, $firstName" else baseGreeting
        }
        else -> baseGreeting
    }

    val showSignInAction = sessionState is AuthSessionState.Unauthenticated ||
        sessionState is AuthSessionState.Guest ||
        sessionState is AuthSessionState.Unconfigured

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.lg,
            bottom = spacing.bottomBarClearance
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SoniqBrandMark(showTagline = true)
                    if (showSignInAction) {
                        SoniqPillChip(
                            label = "Sign In",
                            selected = false,
                            onClick = onOpenAuth
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoniqColors.TextPrimary
                )
            }
        }

        item {
            SoniqGlassCard(
                elevated = true,
                borderColor = atmosphere.primaryAccent.copy(alpha = 0.40f),
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = atmosphere.primaryAccent
                        )
                        Text(
                            text = "SONIQ FLOW",
                            style = MaterialTheme.typography.labelLarge,
                            color = atmosphere.secondaryAccent
                        )
                    }
                    Text(
                        text = "Your personalized listening atmosphere",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoniqColors.TextPrimary
                    )
                    Text(
                        text = "Explore SONIQ's verified 10-track CC0 / Public Domain Starter Collection right now. Background Media3 playback arrives in Phase 4.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SoniqPillChip(
                            label = "Explore Catalogue",
                            selected = true,
                            onClick = onOpenExplore
                        )
                        SoniqPillChip(
                            label = "Open Library",
                            selected = false,
                            onClick = onOpenLibrary
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Starter Collection (${starterTracks.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoniqColors.TextPrimary
                )
                if (starterTracks.isNotEmpty()) {
                    TextButton(onClick = onOpenLibrary) {
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.labelLarge,
                            color = atmosphere.secondaryAccent
                        )
                    }
                }
            }
        }

        if (isLoadingCatalogue && starterTracks.isEmpty()) {
            item { SoniqLoadingView(label = "Loading Starter Collection...") }
        } else if (starterTracks.isEmpty()) {
            item {
                SoniqEmptyState(
                    icon = Icons.Rounded.LibraryMusic,
                    badgeLabel = "Starter Collection",
                    title = "No starter tracks loaded",
                    subtitle = "Verify assets/music/licenses.json is present in the APK bundle."
                )
            }
        } else {
            items(starterTracks.take(5), key = { it.id }) { track ->
                SoniqTrackRow(track = track)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(
                    text = "Recently Played",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoniqColors.TextPrimary
                )
                SoniqEmptyState(
                    icon = Icons.Rounded.SpatialAudioOff,
                    badgeLabel = "Phase 4 Streaming Engine",
                    title = "No listening history yet",
                    subtitle = "Once Media3 playback is wired in Phase 4, your recently played tracks will appear here automatically."
                )
            }
        }
    }
}
