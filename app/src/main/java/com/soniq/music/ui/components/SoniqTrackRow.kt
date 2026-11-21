package com.soniq.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soniq.music.core.common.TimeFormatter
import com.soniq.music.domain.model.Track
import com.soniq.music.domain.model.TrackAudioSource
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.SoniqColors

/**
 * Glassmorphic track list item with procedural SONIQ artwork emblem,
 * duration formatting, and transparent audio-asset readiness indicator.
 */
@Composable
fun SoniqTrackRow(
    track: Track,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val atmosphere = LocalDynamicAtmosphere.current
    val categoryColor = when (track.categoryName.lowercase()) {
        "electronic" -> SoniqColors.ElectricBlue
        "chill / lo-fi" -> SoniqColors.DynamicPurple
        "instrumental" -> Color(0xFF7B2CBF)
        "ambient" -> Color(0xFF00B4D8)
        "pop / acoustic" -> SoniqColors.NeonPink
        else -> atmosphere.primaryAccent
    }

    val statusBadge = when (val src = track.audioSource) {
        is TrackAudioSource.BundledAsset -> if (src.isBinaryBundled) "Offline Ready" else "Audio Pending"
        is TrackAudioSource.RemoteStream -> "Stream Ready"
    }

    SoniqGlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                categoryColor.copy(alpha = 0.72f),
                                SoniqColors.DeepNavy
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = SoniqColors.TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SoniqColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artistName} · ${track.categoryName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val licenseShort = track.licenseInfo?.licenseName?.substringBefore(" (") ?: "Licensed"
                Text(
                    text = "$licenseShort · $statusBadge",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (statusBadge == "Audio Pending") SoniqColors.Warning else atmosphere.secondaryAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = TimeFormatter.formatDurationMs(track.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = SoniqColors.TextSecondary
            )
        }
    }
}
