package com.soniq.music.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Core SONIQ color tokens following Premium Dynamic Dark Glassmorphism.
 */
object SoniqColors {
    // Primary & secondary backgrounds
    val MidnightBlack = Color(0xFF090B12)
    val DeepNavy = Color(0xFF111525)
    val ElevatedNavy = Color(0xFF181E34)

    // Translucent dark glass layers
    val GlassSurface = Color(0xB8141A2E)
    val GlassSurfaceElevated = Color(0xCC1B223B)
    val GlassBorderSubtle = Color(0x24FFFFFF)
    val GlassHighlight = Color(0x14FFFFFF)

    // Signature neon accents
    val DynamicPurple = Color(0xFF9D4EDD)
    val ElectricBlue = Color(0xFF3A86FF)
    val NeonPink = Color(0xFFFF2E93)

    // Text hierarchy
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    // Semantic feedback
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFF43F5E)
}
