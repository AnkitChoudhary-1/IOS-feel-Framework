package dev.iosfeel.sonora.feature.player.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.PlaybackState
import dev.iosfeel.sonora.core.model.RepeatMode

@Composable
fun SecondaryControls(
    state: PlaybackState,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOptionsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val haptics = rememberIOSHaptics()

    val heartScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1.0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.6f),
        label = "heart_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favorite Button
        IconButton(
            onClick = {
                haptics.impact(IOSImpact.Medium)
                onToggleFavorite()
            },
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) SonoraIcons.HeartFilled else SonoraIcons.Heart,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color(0xFFFF2D55) else colors.textTertiary,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = heartScale
                        scaleY = heartScale
                    }
            )
        }

        // Shuffle Button
        IconButton(
            onClick = {
                haptics.selection()
                onToggleShuffle()
            },
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = SonoraIcons.Shuffle,
                contentDescription = "Shuffle",
                tint = if (state.shuffleEnabled) colors.accent else colors.textTertiary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Repeat Button
        IconButton(
            onClick = {
                haptics.selection()
                onCycleRepeat()
            },
            modifier = Modifier.size(44.dp)
        ) {
            val isRepeatActive = state.repeatMode != RepeatMode.Off
            val repeatIcon = if (state.repeatMode == RepeatMode.One) SonoraIcons.RepeatOne else SonoraIcons.Repeat

            Icon(
                imageVector = repeatIcon,
                contentDescription = "Repeat",
                tint = if (isRepeatActive) colors.accent else colors.textTertiary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Options ⋯ Button
        IconButton(
            onClick = {
                haptics.impact(IOSImpact.Light)
                onOptionsClick()
            },
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = SonoraIcons.MoreHorizontal,
                contentDescription = "More Options",
                tint = colors.textTertiary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
