package dev.iosfeel.sonora.feature.player.mini

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.PlaybackState
import kotlinx.coroutines.launch

import dev.iosfeel.components.iconbutton.IOSIconButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@Composable
fun MiniPlayer(
    state: PlaybackState,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val song = state.currentSong ?: return
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val haptics = rememberIOSHaptics()
    val scope = rememberCoroutineScope()

    val miniAlpha = (1f - (progress / 0.35f)).coerceIn(0f, 1f)
    if (miniAlpha <= 0.01f) return

    val horizontalOffset = remember { Animatable(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = miniAlpha }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val currentOffset = horizontalOffset.value
                        scope.launch {
                            if (currentOffset < -50f) {
                                haptics.impact(IOSImpact.Light)
                                onNext()
                            } else if (currentOffset > 50f) {
                                haptics.impact(IOSImpact.Light)
                                onPrevious()
                            }
                            horizontalOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(stiffness = 500f, dampingRatio = 0.82f)
                            )
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            horizontalOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(stiffness = 500f, dampingRatio = 0.82f)
                            )
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            // Elastic damping on drag
                            val newTarget = horizontalOffset.value + dragAmount * 0.65f
                            horizontalOffset.snapTo(newTarget.coerceIn(-120f, 120f))
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp)
                .graphicsLayer {
                    translationX = horizontalOffset.value
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongArtwork(
                song = song,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = typography.headline,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = song.artist,
                    style = typography.subheadline,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IOSIconButton(
                onClick = onPlayPause,
                size = 44.dp,
                contentDescription = if (state.isPlaying) "Pause" else "Play"
            ) {
                Icon(
                    imageVector = if (state.isPlaying) SonoraIcons.Pause else SonoraIcons.Play,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            IOSIconButton(
                onClick = onNext,
                size = 44.dp,
                contentDescription = "Next"
            ) {
                Icon(
                    imageVector = SonoraIcons.SkipNext,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = colors.accent,
            trackColor = colors.separator.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}
