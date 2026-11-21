package com.soniq.music.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import com.soniq.music.domain.model.AtmosphereAccent

private const val ATMOSPHERE_TRANSITION_MS = 450

@Composable
fun SoniqTheme(
    accent: AtmosphereAccent = AtmosphereAccent.DynamicPurple,
    content: @Composable () -> Unit
) {
    val targetAtmosphere = DynamicAtmosphereColors.fromAccent(accent)

    val animatedPrimary by animateColorAsState(
        targetValue = targetAtmosphere.primaryAccent,
        animationSpec = tween(durationMillis = ATMOSPHERE_TRANSITION_MS),
        label = "soniqPrimaryAccent"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = targetAtmosphere.secondaryAccent,
        animationSpec = tween(durationMillis = ATMOSPHERE_TRANSITION_MS),
        label = "soniqSecondaryAccent"
    )
    val animatedGlow by animateColorAsState(
        targetValue = targetAtmosphere.ambientGlow,
        animationSpec = tween(durationMillis = ATMOSPHERE_TRANSITION_MS),
        label = "soniqAmbientGlow"
    )

    val dynamicAtmosphere = DynamicAtmosphereColors(
        primaryAccent = animatedPrimary,
        secondaryAccent = animatedSecondary,
        ambientGlow = animatedGlow
    )

    val colorScheme = darkColorScheme(
        primary = animatedPrimary,
        onPrimary = SoniqColors.TextPrimary,
        secondary = animatedSecondary,
        onSecondary = SoniqColors.TextPrimary,
        tertiary = SoniqColors.NeonPink,
        onTertiary = SoniqColors.TextPrimary,
        background = SoniqColors.MidnightBlack,
        onBackground = SoniqColors.TextPrimary,
        surface = SoniqColors.DeepNavy,
        onSurface = SoniqColors.TextPrimary,
        surfaceVariant = SoniqColors.ElevatedNavy,
        onSurfaceVariant = SoniqColors.TextSecondary,
        outline = SoniqColors.GlassBorderSubtle,
        error = SoniqColors.Error,
        onError = SoniqColors.TextPrimary
    )

    CompositionLocalProvider(
        LocalDynamicAtmosphere provides dynamicAtmosphere,
        LocalSoniqSpacing provides SoniqSpacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SoniqTypography,
            shapes = SoniqShapes,
            content = content
        )
    }
}
