package com.soniq.music.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.soniq.music.domain.model.AtmosphereAccent

/**
 * Dynamic ambient palette driving SONIQ's background glow, active navigation pills,
 * and future artwork-derived Now Playing atmosphere.
 */
@Immutable
data class DynamicAtmosphereColors(
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val ambientGlow: Color,
    val glassSurface: Color = SoniqColors.GlassSurface,
    val glassSurfaceElevated: Color = SoniqColors.GlassSurfaceElevated,
    val glassBorder: Color = SoniqColors.GlassBorderSubtle
) {
    companion object {
        fun fromAccent(accent: AtmosphereAccent): DynamicAtmosphereColors =
            DynamicAtmosphereColors(
                primaryAccent = Color(accent.primaryHex),
                secondaryAccent = Color(accent.secondaryHex),
                ambientGlow = Color(accent.glowHex)
            )
    }
}

val LocalDynamicAtmosphere = staticCompositionLocalOf {
    DynamicAtmosphereColors.fromAccent(AtmosphereAccent.DynamicPurple)
}
