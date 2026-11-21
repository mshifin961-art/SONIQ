package com.soniq.music.ui.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soniq.music.ui.components.SoniqEmptyState
import com.soniq.music.ui.components.SoniqPillChip
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors

private val SearchScopes = listOf("All", "Songs", "Artists", "Albums", "Playlists", "Genres")

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSoniqSpacing.current
    val atmosphere = LocalDynamicAtmosphere.current
    var query by rememberSaveable { mutableStateOf("") }
    var selectedScope by rememberSaveable { mutableStateOf(SearchScopes.first()) }

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
                    text = "Find songs, artists, albums, playlists, and genres.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
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
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = SoniqColors.TextSecondary
                            )
                        }
                    }
                },
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
                items(SearchScopes) { scope ->
                    SoniqPillChip(
                        label = scope,
                        selected = selectedScope == scope,
                        onClick = { selectedScope = scope }
                    )
                }
            }
        }

        item {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                SoniqEmptyState(
                    icon = Icons.Rounded.Search,
                    badgeLabel = "Scope: $selectedScope",
                    title = "Start exploring SONIQ",
                    subtitle = "Search indexing for the local Starter Collection and authorized Supabase catalogue will be enabled in Phase 3."
                )
            } else {
                SoniqEmptyState(
                    icon = Icons.Rounded.Search,
                    badgeLabel = "0 Matches in $selectedScope",
                    title = "No catalogue results for \"$trimmed\"",
                    subtitle = "SONIQ only displays verified available tracks. Catalogue search queries connect in Phase 3."
                )
            }
        }
    }
}
