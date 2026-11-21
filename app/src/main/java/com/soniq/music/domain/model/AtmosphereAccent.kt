package com.soniq.music.domain.model

/**
 * Signature accent presets for SONIQ's Dynamic Dark Glassmorphism atmosphere.
 * In Phase 6, artwork-extracted palettes will also drive this atmosphere layer.
 */
enum class AtmosphereAccent(
    val displayName: String,
    val subtitle: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val glowHex: Long
) {
    DynamicPurple(
        displayName = "Dynamic Purple",
        subtitle = "Signature SONIQ neon resonance",
        primaryHex = 0xFF9D4EDD,
        secondaryHex = 0xFF3A86FF,
        glowHex = 0xFF7B2CBF
    ),
    ElectricBlue(
        displayName = "Electric Blue",
        subtitle = "Crisp studio clarity",
        primaryHex = 0xFF3A86FF,
        secondaryHex = 0xFF00D4FF,
        glowHex = 0xFF1D4ED8
    ),
    NeonPink(
        displayName = "Neon Pink",
        subtitle = "Warm after-hours pulse",
        primaryHex = 0xFFFF2E93,
        secondaryHex = 0xFF9D4EDD,
        glowHex = 0xFFBE185D
    )
}
