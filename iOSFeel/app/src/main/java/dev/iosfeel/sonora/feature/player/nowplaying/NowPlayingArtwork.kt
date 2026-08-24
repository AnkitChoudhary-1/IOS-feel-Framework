package dev.iosfeel.sonora.feature.player.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.sonora.core.design.artwork.MissingArtwork
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.launch

@Composable
fun NowPlayingArtwork(
    song: Song?,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {}
) {
    val haptics = rememberIOSHaptics()
    val scope = rememberCoroutineScope()
    val horizontalOffset = remember { Animatable(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.86f,
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.78f),
        label = "artwork_scale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isPlaying) 24.dp else 8.dp,
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.78f),
        label = "artwork_elevation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    translationX = horizontalOffset.value
                }
                .shadow(
                    elevation = animatedElevation,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = if (isPlaying) 0.38f else 0.20f)
                )
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val currentOffset = horizontalOffset.value
                            scope.launch {
                                if (currentOffset < -65f) {
                                    haptics.impact(IOSImpact.Medium)
                                    onNext()
                                } else if (currentOffset > 65f) {
                                    haptics.impact(IOSImpact.Medium)
                                    onPrevious()
                                }
                                horizontalOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = 450f, dampingRatio = 0.82f)
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                horizontalOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = 450f, dampingRatio = 0.82f)
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val newTarget = horizontalOffset.value + dragAmount * 0.7f
                                horizontalOffset.snapTo(newTarget.coerceIn(-140f, 140f))
                            }
                        }
                    )
                }
        ) {
            if (song != null) {
                SongArtwork(
                    song = song,
                    cornerRadius = 16.dp,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                MissingArtwork(
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 16.dp,
                    iconSize = 64.dp
                )
            }
        }
    }
}
