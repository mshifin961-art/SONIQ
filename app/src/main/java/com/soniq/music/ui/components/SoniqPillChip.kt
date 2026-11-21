package com.soniq.music.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.SoniqColors

@Composable
fun SoniqPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val atmosphere = LocalDynamicAtmosphere.current
    val shape = RoundedCornerShape(percent = 50)

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            atmosphere.primaryAccent.copy(alpha = 0.24f)
        } else {
            atmosphere.glassSurface
        },
        animationSpec = tween(220),
        label = "chipBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            atmosphere.primaryAccent.copy(alpha = 0.72f)
        } else {
            atmosphere.glassBorder
        },
        animationSpec = tween(220),
        label = "chipBorder"
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) SoniqColors.TextPrimary else SoniqColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
