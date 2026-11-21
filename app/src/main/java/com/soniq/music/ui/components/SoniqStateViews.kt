package com.soniq.music.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.SoniqColors

@Composable
fun SoniqLoadingView(
    label: String = "Tuning frequencies...",
    modifier: Modifier = Modifier
) {
    val atmosphere = LocalDynamicAtmosphere.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = atmosphere.primaryAccent,
            trackColor = SoniqColors.ElevatedNavy,
            strokeWidth = 3.dp,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SoniqColors.TextSecondary
        )
    }
}

@Composable
fun SoniqEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.GraphicEq,
    badgeLabel: String? = null
) {
    val atmosphere = LocalDynamicAtmosphere.current
    SoniqGlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = atmosphere.primaryAccent,
                modifier = Modifier.size(30.dp)
            )
            if (badgeLabel != null) {
                Text(
                    text = badgeLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = atmosphere.secondaryAccent
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SoniqColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SoniqColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SoniqErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val atmosphere = LocalDynamicAtmosphere.current
    SoniqGlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = SoniqColors.Error.copy(alpha = 0.45f),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = SoniqColors.Error,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SoniqColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SoniqColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = atmosphere.primaryAccent,
                        contentColor = SoniqColors.TextPrimary
                    )
                ) {
                    Text(text = "Try Again", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
