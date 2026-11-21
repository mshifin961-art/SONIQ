package com.soniq.music.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.SoniqColors

/**
 * Original SONIQ frequency-bar emblem paired with crisp wordmark.
 */
@Composable
fun SoniqBrandMark(
    modifier: Modifier = Modifier,
    showTagline: Boolean = false
) {
    val atmosphere = LocalDynamicAtmosphere.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val barWidth = size.width * 0.16f
            val gap = size.width * 0.10f
            val heights = listOf(0.42f, 0.78f, 1.0f, 0.62f)
            val colors = listOf(
                atmosphere.secondaryAccent,
                atmosphere.primaryAccent,
                SoniqColors.NeonPink,
                atmosphere.primaryAccent
            )
            heights.forEachIndexed { index, fraction ->
                val barHeight = size.height * fraction
                val x = index * (barWidth + gap)
                val y = (size.height - barHeight) / 2f
                drawRoundRect(
                    color = colors[index],
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }

        Column {
            Text(
                text = "SONIQ",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.4.sp
                ),
                color = SoniqColors.TextPrimary
            )
            if (showTagline) {
                Text(
                    text = "Feel Every Frequency.",
                    style = MaterialTheme.typography.labelMedium,
                    color = SoniqColors.TextSecondary
                )
            }
        }
    }
}
