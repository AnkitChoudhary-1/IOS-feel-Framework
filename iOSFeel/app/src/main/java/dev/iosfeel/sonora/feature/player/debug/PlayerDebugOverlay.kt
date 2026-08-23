package dev.iosfeel.sonora.feature.player.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.expandable.IOSExpandableSurfaceState
import dev.iosfeel.sonora.core.model.PlaybackState

@Composable
fun PlayerDebugOverlay(
    expansionState: IOSExpandableSurfaceState,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.75f))
            .padding(8.dp)
    ) {
        Text(
            text = "PLAYER DIAGNOSTICS",
            color = Color(0xFF30D158),
            fontSize = 10.sp
        )
        Text(
            text = "Progress: ${String.format("%.3f", expansionState.progress)}",
            color = Color.White,
            fontSize = 11.sp
        )
        Text(
            text = "Velocity: ${String.format("%.2f", expansionState.velocity)}/s",
            color = Color.White,
            fontSize = 11.sp
        )
        Text(
            text = "Phase: ${expansionState.phase}",
            color = Color.White,
            fontSize = 11.sp
        )
        Text(
            text = "IsPlaying: ${playbackState.isPlaying}",
            color = Color.White,
            fontSize = 11.sp
        )
        Text(
            text = "Pos: ${playbackState.positionMs}ms / ${playbackState.durationMs}ms",
            color = Color.White,
            fontSize = 11.sp
        )
    }
}
