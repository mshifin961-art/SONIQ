package com.soniq.music.ui.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soniq.music.domain.model.SearchScope
import com.soniq.music.ui.components.SoniqEmptyState
import com.soniq.music.ui.components.SoniqGlassCard
import com.soniq.music.ui.components.SoniqLoadingView
import com.soniq.music.ui.components.SoniqPillChip
import com.soniq.music.ui.components.SoniqTrackRow
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val spacing = LocalSoniqSpacing.current
    val atmosphere = LocalDynamicAtmosphere.current

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
                    text = "Search",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoniqColors.TextPrimary
                )
                Text(
                    text = "Search available songs, artists, albums, and genres.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }

        item {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = {
                    Text(
                        text = "Search SONIQ catalogue...",
                        color = SoniqColors.TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = atmosphere.primaryAccent
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = SoniqColors.TextSecondary
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.submitCurrentQuery() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = atmosphere.glassSurfaceElevated,
                    unfocusedContainerColor = atmosphere.glassSurface,
                    focusedBorderColor = atmosphere.primaryAccent,
                    unfocusedBorderColor = atmosphere.glassBorder,
                    focusedTextColor = SoniqColors.TextPrimary,
                    unfocusedTextColor = SoniqColors.TextPrimary,
                    cursorColor = atmosphere.primaryAccent
                )
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                items(SearchScope.entries) { scope ->
                    SoniqPillChip(
                        label = scope.label,
                        selected = uiState.selectedScope == scope,
                        onClick = { viewModel.onScopeSelected(scope) }
                    )
                }
            }
        }

        val trimmed = uiState.query.trim()
        if (trimmed.isEmpty()) {
            if (recentSearches.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.titleMedium,
                                color = SoniqColors.TextPrimary
                            )
                            TextButton(onClick = viewModel::clearRecentSearches) {
                                Text("Clear", color = SoniqColors.TextSecondary)
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            items(recentSearches, key = { it.query }) { recent ->
                                SoniqPillChip(
                                    label = recent.query,
                                    selected = false,
                                    onClick = { viewModel.selectRecentSearch(recent.query) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SoniqEmptyState(
                    icon = Icons.Rounded.Search,
                    badgeLabel = "Offline & Authorized Search",
                    title = "Try searching Electronic, Chopin, or HoliznaCC0",
                    subtitle = "SONIQ searches your local Starter Collection and synced authorized catalogue instantly."
                )
            }
        } else if (uiState.isSearching) {
            item { SoniqLoadingView(label = "Searching catalogue...") }
        } else if (uiState.result.isEmpty) {
            item {
                SoniqEmptyState(
                    icon = Icons.Rounded.Search,
                    badgeLabel = "0 Matches in ${uiState.selectedScope.label}",
                    title = "No catalogue results for \"$trimmed\"",
                    subtitle = "SONIQ only displays tracks, artists, albums, and genres that are actually available."
                )
            }
        } else {
            if (uiState.result.tracks.isNotEmpty()) {
                item {
                    Text(
                        text = "Songs (${uiState.result.tracks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoniqColors.TextPrimary
                    )
                }
                items(uiState.result.tracks, key = { "track-${it.id}" }) { track ->
                    SoniqTrackRow(track = track)
                }
            }

            if (uiState.result.artists.isNotEmpty()) {
                item {
                    Text(
                        text = "Artists (${uiState.result.artists.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoniqColors.TextPrimary
                    )
                }
                items(uiState.result.artists, key = { "artist-${it.id}" }) { artist ->
                    SoniqGlassCard(contentPadding = PaddingValues(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = atmosphere.secondaryAccent
                            )
                            Column {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoniqColors.TextPrimary
                                )
                                Text(
                                    text = artist.bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoniqColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.result.albums.isNotEmpty()) {
                item {
                    Text(
                        text = "Albums (${uiState.result.albums.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoniqColors.TextPrimary
                    )
                }
                items(uiState.result.albums, key = { "album-${it.id}" }) { album ->
                    SoniqGlassCard(contentPadding = PaddingValues(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Album,
                                contentDescription = null,
                                tint = atmosphere.primaryAccent
                            )
                            Column {
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoniqColors.TextPrimary
                                )
                                Text(
                                    text = album.artistName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoniqColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.result.genres.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Text(
                            text = "Genres (${uiState.result.genres.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoniqColors.TextPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            items(uiState.result.genres, key = { "genre-${it.id}" }) { genre ->
                                SoniqPillChip(
                                    label = genre.name,
                                    selected = true,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
