package dev.iosfeel.sonora.feature.player.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.PlaybackState
import dev.iosfeel.sonora.core.model.RepeatMode

@Composable
fun SecondaryControls(
    state: PlaybackState,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val haptics = rememberIOSHaptics()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
    }
}
