package com.soniq.music.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soniq.music.ui.navigation.PrimaryDestinationItem
import com.soniq.music.ui.navigation.SoniqDestination
import com.soniq.music.ui.theme.LocalDynamicAtmosphere
import com.soniq.music.ui.theme.SoniqColors

/**
 * Floating translucent dark glass navigation dock for SONIQ's 5 primary destinations.
 */
@Composable
fun SoniqBottomBar(
    destinations: List<PrimaryDestinationItem>,
    currentDestination: SoniqDestination,
    onNavigate: (SoniqDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val atmosphere = LocalDynamicAtmosphere.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = SoniqColors.DeepNavy.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, atmosphere.glassBorder),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEach { item ->
                    val selected = currentDestination == item.route
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) {
                            SoniqColors.TextPrimary
                        } else {
                            SoniqColors.TextMuted
                        },
                        animationSpec = tween(200),
                        label = "navContentColor"
                    )
                    val pillColor by animateColorAsState(
                        targetValue = if (selected) {
                            atmosphere.primaryAccent.copy(alpha = 0.22f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = tween(200),
                        label = "navPillColor"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(pillColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onNavigate(item.route) }
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = if (selected) atmosphere.primaryAccent else contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
