package com.soniq.music.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.soniq.music.ui.theme.LocalDynamicAtmosphere

/**
 * Translucent dark glass container with hairline luminance border.
 */
@Composable
fun SoniqGlassCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val atmosphere = LocalDynamicAtmosphere.current
    val containerColor = if (elevated) {
        atmosphere.glassSurfaceElevated
    } else {
        atmosphere.glassSurface
    }
    val resolvedBorder = borderColor ?: atmosphere.glassBorder

    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = containerColor,
        border = BorderStroke(width = 1.dp, color = resolvedBorder)
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
