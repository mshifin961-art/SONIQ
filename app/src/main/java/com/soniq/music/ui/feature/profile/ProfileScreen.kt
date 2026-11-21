package com.soniq.music.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soniq.music.core.config.AppConfig
import com.soniq.music.domain.model.AtmosphereAccent
import com.soniq.music.network.SupabaseEnvironmentStatus
import com.soniq.music.ui.components.SoniqBrandMark
import com.soniq.music.ui.components.SoniqGlassCard
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.LocalSoniqSpacing
import com.soniq.music.ui.theme.SoniqColors

@Composable
fun ProfileScreen(
    activeAccent: AtmosphereAccent,
    onSelectAccent: (AtmosphereAccent) -> Unit,
    appConfig: AppConfig,
    supabaseStatus: SupabaseEnvironmentStatus,
    modifier: Modifier = Modifier
) {
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
                    text = "Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoniqColors.TextPrimary
                )
                Text(
                    text = "Appearance, environment readiness, and SONIQ identity.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }

        item {
            SoniqGlassCard(elevated = true) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = atmosphere.primaryAccent
                        )
                        Text(
                            text = "Dynamic Atmosphere Accent",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoniqColors.TextPrimary
                        )
                    }
                    Text(
                        text = "Preview SONIQ's live dark glassmorphism accent transitions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AtmosphereAccent.entries.forEach { preset ->
                            val isSelected = preset == activeAccent
                            SoniqGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = if (isSelected) {
                                    Color(preset.primaryHex).copy(alpha = 0.85f)
                                } else {
                                    SoniqColors.GlassBorderSubtle
                                },
                                contentPadding = PaddingValues(14.dp),
                                onClick = { onSelectAccent(preset) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(preset.primaryHex))
                                        )
                                        Column {
                                            Text(
                                                text = preset.displayName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = SoniqColors.TextPrimary
                                            )
                                            Text(
                                                text = preset.subtitle,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SoniqColors.TextSecondary
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(preset.primaryHex)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SoniqGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = atmosphere.secondaryAccent
                        )
                        Text(
                            text = "Backend Environment",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoniqColors.TextPrimary
                        )
                    }
                    Text(
                        text = supabaseStatus.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (supabaseStatus == SupabaseEnvironmentStatus.Configured) {
                            SoniqColors.Success
                        } else {
                            SoniqColors.Warning
                        }
                    )
                    Text(
                        text = supabaseStatus.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )
                }
            }
        }

        item {
            SoniqGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoniqBrandMark(showTagline = true)
                    Text(
                        text = "${appConfig.phaseLabel} · v${appConfig.versionName} (${appConfig.versionCode})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoniqColors.TextSecondary
                    )
                    Text(
                        text = "Package: ${appConfig.applicationId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SoniqColors.TextMuted
                    )
                }
            }
        }
    }
}
