package dev.iosfeel.sonora.feature.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import dev.iosfeel.components.expandable.IOSExpandableSurfaceState
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.model.PlaybackState
import dev.iosfeel.sonora.feature.player.mini.MiniPlayer
import dev.iosfeel.sonora.feature.player.nowplaying.NowPlayingContent
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PlayerSurface(
    playbackState: PlaybackState,
    expansionState: IOSExpandableSurfaceState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    backdrop: IOSBackdropState? = null,
    modifier: Modifier = Modifier
) {
    if (!playbackState.hasActiveMedia) return

    val colors = LocalSonoraColors.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptics = rememberIOSHaptics()

    val progress = expansionState.progress

    // Back button handling: collapse player if expanded
    BackHandler(enabled = progress > 0.05f) {
        scope.launch {
            expansionState.collapse()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxHeightPx = constraints.maxHeight.toFloat()
        val miniHeightPx = with(density) { 62.dp.toPx() }
        val expandableDistancePx = (maxHeightPx - miniHeightPx).coerceAtLeast(1f)

        val cornerRadius = lerp(31.dp, 0.dp, progress)
        val horizontalPadding = lerp(16.dp, 0.dp, progress)
        val bottomPadding = lerp(84.dp, 0.dp, progress)
        val currentHeight = lerp(62.dp, maxHeight, progress)
        val pillShape = RoundedCornerShape(cornerRadius)

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding)
                .padding(bottom = bottomPadding)
                .fillMaxWidth()
                .height(currentHeight)
                .shadow(
                    elevation = if (progress < 0.95f) lerp(12.dp, 0.dp, progress) else 0.dp,
                    shape = pillShape,
                    spotColor = Color.Black.copy(alpha = 0.28f),
                    ambientColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(pillShape)
                .border(
                    width = 0.5.dp,
                    color = if (progress < 0.2f) Color.White.copy(alpha = 0.18f) else Color.Transparent,
                    shape = pillShape
                )
                .background(if (progress > 0.6f) colors.background else Color.Transparent)
                .clickable(enabled = progress < 0.08f) {
                    scope.launch {
                        expansionState.expand()
                    }
                }
                .pointerInput(expandableDistancePx) {
                    val velocityTracker = VelocityTracker()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        velocityTracker.resetTracking()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)

                        scope.launch {
                            expansionState.beginDrag()
                        }

                        val pointerId = down.id
                        var lastY = down.position.y

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                            if (!change.pressed) {
                                val velocityY = velocityTracker.calculateVelocity().y
                                val normalizedVelocity = -velocityY / expandableDistancePx
                                scope.launch {
                                    expansionState.settle(velocity = normalizedVelocity)
                                }
                                break
                            }

                            val currentY = change.position.y
                            val dragDeltaY = currentY - lastY

                            if (abs(dragDeltaY) > 0.3f) {
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                val deltaProgress = -dragDeltaY / expandableDistancePx
                                scope.launch {
                                    expansionState.dragBy(
                                        deltaProgress = deltaProgress,
                                        velocity = -dragDeltaY
                                    )
                                }
                                lastY = currentY
                            }
                        }
                    }
                }
        ) {
            // Frosted backdrop blur layer for mini player
            if (progress < 0.85f) {
                IOSMaterialSurface(
                    backdrop = backdrop,
                    config = IOSMaterialConfig(
                        style = IOSMaterialStyle.Regular,
                        cornerRadius = cornerRadius
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {}
            }

            // Mini Player view
            if (progress < 0.45f) {
                MiniPlayer(
                    state = playbackState,
                    progress = progress,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // Expanded Now Playing view
            if (progress > 0.15f) {
                NowPlayingContent(
                    state = playbackState,
                    progress = progress,
                    onCollapse = {
                        scope.launch {
                            expansionState.collapse()
                        }
                    },
                    onPlayPause = onPlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onSeek = onSeek,
                    onToggleShuffle = onToggleShuffle,
                    onCycleRepeat = onCycleRepeat,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
