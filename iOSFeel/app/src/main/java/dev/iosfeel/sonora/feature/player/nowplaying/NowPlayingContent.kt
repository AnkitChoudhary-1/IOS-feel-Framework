package dev.iosfeel.sonora.feature.player.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.PlaybackState
import dev.iosfeel.sonora.core.model.RepeatMode

@Composable
fun NowPlayingContent(
    state: PlaybackState,
    progress: Float,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOptionsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val song = state.currentSong ?: return
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    val expandedAlpha = ((progress - 0.35f) / 0.65f).coerceIn(0f, 1f)
    if (expandedAlpha <= 0.01f) return

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = expandedAlpha },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top drag handle / collapse button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.textTertiary.copy(alpha = 0.4f))
            )

            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = SonoraIcons.ChevronDown,
                    contentDescription = "Collapse",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Big artwork with dynamic spring scaling and swipe-to-skip
        NowPlayingArtwork(
            song = song,
            isPlaying = state.isPlaying,
            onNext = onNext,
            onPrevious = onPrevious,
            modifier = Modifier.weight(1f, fill = false)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title and artist
        NowPlayingMetadata(
            song = song
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        PlaybackProgress(
            state = state,
            onSeek = onSeek
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        PlaybackControls(
            state = state,
            onPlayPause = onPlayPause,
            onPrevious = onPrevious,
            onNext = onNext
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary controls with Favorite and Options buttons
        SecondaryControls(
            state = state,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat,
            onOptionsClick = onOptionsClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Up Next preview footer
        val nextSong = state.queue.getOrNull(state.currentQueueIndex + 1)
        if (nextSong != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceElevated.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = SonoraIcons.MusicNote,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Up Next: ${nextSong.title}",
                        style = typography.subheadline,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                }

                Text(
                    text = nextSong.artist,
                    style = typography.caption1,
                    color = colors.textSecondary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}
