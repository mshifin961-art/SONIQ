package com.soniq.music.ui.feature.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.Track
import com.soniq.music.ui.components.SoniqEmptyState
import com.soniq.music.ui.components.SoniqGlassCard
import com.soniq.music.ui.components.SoniqPillChip
import com.soniq.music.ui.components.SoniqTrackRow
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors

@Composable
fun ExploreScreen(
    genres: List<Genre>,
    albums: List<Album>,
    artists: List<Artist>,
    tracks: List<Track>,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSoniqSpacing.current
    val atmosphere = LocalDynamicAtmosphere.current
    var selectedGenreId by rememberSaveable { mutableStateOf<String?>(null) }

    val filteredTracks = if (selectedGenreId == null) {
        tracks
    } else {
        tracks.filter { it.genreId == selectedGenreId }
    }

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
                    text = "Explore",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoniqColors.TextPrimary
                )
                Text(
                    text = "Browse verified genres, featured albums, and licensed releases.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }

        if (genres.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    item {
                        SoniqPillChip(
                            label = "All",
                            selected = selectedGenreId == null,
                            onClick = { selectedGenreId = null }
                        )
                    }
                    items(genres, key = { it.id }) { genre ->
                        SoniqPillChip(
                            label = genre.name,
                            selected = selectedGenreId == genre.id,
                            onClick = { selectedGenreId = genre.id }
                        )
                    }
                }
            }
        }

        if (albums.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = "Featured Collections",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoniqColors.TextPrimary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        items(albums, key = { it.id }) { album ->
                            SoniqGlassCard(
                                modifier = Modifier.width(180.dp),
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Rounded.Album,
                                        contentDescription = null,
                                        tint = atmosphere.primaryAccent
                                    )
                                    Text(
                                        text = album.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SoniqColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = album.artistName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SoniqColors.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (artists.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = "Featured Artists",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoniqColors.TextPrimary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        items(artists, key = { it.id }) { artist ->
                            SoniqGlassCard(
                                modifier = Modifier.width(165.dp),
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = atmosphere.secondaryAccent
                                    )
                                    Text(
                                        text = artist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SoniqColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Available Tracks (${filteredTracks.size})",
                style = MaterialTheme.typography.titleLarge,
                color = SoniqColors.TextPrimary
            )
        }

        if (filteredTracks.isEmpty()) {
            item {
                SoniqEmptyState(
                    icon = Icons.Rounded.Explore,
                    badgeLabel = "No Matches",
                    title = "No tracks in this genre yet",
                    subtitle = "Select another genre or connect an authorized Supabase catalogue."
                )
            }
        } else {
            items(filteredTracks, key = { it.id }) { track ->
                SoniqTrackRow(track = track)
            }
        }
    }
}
