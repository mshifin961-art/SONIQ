package com.soniq.music.ui.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.soniq.music.domain.model.Track
import com.soniq.music.ui.components.SoniqEmptyState
import com.soniq.music.ui.components.SoniqPillChip
import com.soniq.music.ui.components.SoniqTrackRow
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors

private val LibraryCollections = listOf(
    "Starter Collection",
    "Liked Songs",
    "Playlists",
    "Saved Albums",
    "Followed Artists",
    "Listening History"
)

@Composable
fun LibraryScreen(
    starterTracks: List<Track>,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSoniqSpacing.current
    var selectedCollection by rememberSaveable { mutableStateOf(LibraryCollections.first()) }

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
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoniqColors.TextPrimary
                )
                Text(
                    text = "Your licensed Starter Collection, saved music, and playlists.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                items(LibraryCollections) { collection ->
                    SoniqPillChip(
                        label = collection,
                        selected = selectedCollection == collection,
                        onClick = { selectedCollection = collection }
                    )
                }
            }
        }

        if (selectedCollection == "Starter Collection") {
            if (starterTracks.isEmpty()) {
                item {
                    SoniqEmptyState(
                        icon = Icons.Rounded.LibraryMusic,
                        badgeLabel = "Starter Collection",
                        title = "Starter Collection is empty",
                        subtitle = "Ensure assets/music/licenses.json is bundled with the application."
                    )
                }
            } else {
                items(starterTracks, key = { it.id }) { track ->
                    SoniqTrackRow(track = track)
                }
            }
        } else {
            item {
                SoniqEmptyState(
                    icon = Icons.Rounded.LibraryMusic,
                    badgeLabel = selectedCollection,
                    title = "$selectedCollection is ready",
                    subtitle = "Personal favorites, playlists, and listening history synchronization activate in Phase 7."
                )
            }
        }
    }
}
