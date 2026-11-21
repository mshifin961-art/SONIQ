package com.soniq.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.SoniqColors

/**
 * SONIQ signature backdrop: Midnight Black (#090B12) canvas layered with a restrained,
 * performance-conscious ambient glow drawn in a single Canvas pass.
 */
@Composable
fun SoniqAtmosphereBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val atmosphere = LocalDynamicAtmosphere.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoniqColors.MidnightBlack)
            .drawBehind {
                // Top-left subtle dynamic atmosphere aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            atmosphere.ambientGlow.copy(alpha = 0.22f),
                            SoniqColors.DeepNavy.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.18f, size.height * 0.08f),
                        radius = size.maxDimension * 0.52f
                    ),
                    radius = size.maxDimension * 0.52f,
                    center = Offset(size.width * 0.18f, size.height * 0.08f)
                )
                // Bottom-right restrained secondary depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            atmosphere.secondaryAccent.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.86f),
                        radius = size.maxDimension * 0.45f
                    ),
                    radius = size.maxDimension * 0.45f,
                    center = Offset(size.width * 0.88f, size.height * 0.86f)
                )
            },
        content = content
    )
}
