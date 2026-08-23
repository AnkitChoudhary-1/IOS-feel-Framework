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
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.PlaybackState

@Composable
fun PlaybackControls(
    state: PlaybackState,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val haptics = rememberIOSHaptics()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                haptics.selection()
                onPrevious()
            },
            modifier = Modifier.size(54.dp)
        ) {
            Icon(
                imageVector = SonoraIcons.SkipPrevious,
                contentDescription = "Previous",
                tint = colors.textPrimary,
                modifier = Modifier.size(34.dp)
            )
        }

        IconButton(
            onClick = {
                haptics.impact(IOSImpact.Light)
                onPlayPause()
            },
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = if (state.isPlaying) SonoraIcons.Pause else SonoraIcons.Play,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                tint = colors.textPrimary,
                modifier = Modifier.size(46.dp)
            )
        }

        IconButton(
            onClick = {
                haptics.selection()
                onNext()
            },
            modifier = Modifier.size(54.dp)
        ) {
            Icon(
                imageVector = SonoraIcons.SkipNext,
                contentDescription = "Next",
                tint = colors.textPrimary,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
