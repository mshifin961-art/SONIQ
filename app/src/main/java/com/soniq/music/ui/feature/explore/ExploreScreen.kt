package com.soniq.music.ui.feature.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.soniq.music.ui.components.SoniqEmptyState
import com.soniq.music.ui.components.SoniqPillChip
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors

private val ExploreCategories = listOf(
    "All",
    "Electronic",
    "Chill / Lo-fi",
    "Instrumental",
    "Ambient",
    "Pop / Acoustic"
)

@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSoniqSpacing.current
    var selectedCategory by rememberSaveable { mutableStateOf(ExploreCategories.first()) }

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
                    text = "Browse curated moods, genres, and authorized releases.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                items(ExploreCategories) { category ->
                    SoniqPillChip(
                        label = category,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }
        }

        item {
            SoniqEmptyState(
                icon = Icons.Rounded.Explore,
                badgeLabel = "Category: $selectedCategory",
                title = "Authorized catalogue not yet connected",
                subtitle = "No placeholder tracks are displayed. Curated genres, moods, featured artists, and the Starter Collection connect in Phase 3."
            )
        }
    }
}
