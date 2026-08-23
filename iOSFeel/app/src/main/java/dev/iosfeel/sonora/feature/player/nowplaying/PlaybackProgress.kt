package dev.iosfeel.sonora.feature.player.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.model.PlaybackState
import dev.iosfeel.sonora.core.model.formatDuration

@Composable
fun PlaybackProgress(
    state: PlaybackState,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val actualProgress = if (state.durationMs > 0L) {
        (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }

    val displayProgress = if (isDragging) dragProgress else actualProgress
    val currentPositionMs = (displayProgress * state.durationMs).toLong()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(state.durationMs) {
                    detectTapGestures { offset ->
                        if (state.durationMs > 0L) {
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((newProgress * state.durationMs).toLong())
                        }
                    }
                }
                .pointerInput(state.durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            if (state.durationMs > 0L) {
                                onSeek((dragProgress * state.durationMs).toLong())
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val trackColor = colors.separator.copy(alpha = 0.25f)
            val progressColor = colors.textPrimary
            val thumbColor = colors.textPrimary

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isDragging) 8.dp else 4.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val cornerRadius = CornerRadius(canvasHeight / 2f, canvasHeight / 2f)

                // Background track
                drawRoundRect(
                    color = trackColor,
                    size = Size(canvasWidth, canvasHeight),
                    cornerRadius = cornerRadius
                )

                // Progress active bar
                val progressWidth = canvasWidth * displayProgress
                if (progressWidth > 0f) {
                    drawRoundRect(
                        color = progressColor,
                        size = Size(progressWidth, canvasHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // Thumb knob (enlarges on drag)
                val thumbRadius = if (isDragging) 8.dp.toPx() else 5.dp.toPx()
                drawCircle(
                    color = thumbColor,
                    radius = thumbRadius,
                    center = Offset(progressWidth.coerceIn(thumbRadius, canvasWidth - thumbRadius), canvasHeight / 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentPositionMs.formatDuration(),
                style = typography.caption1,
                color = colors.textTertiary
            )

            Text(
                text = state.durationMs.formatDuration(),
                style = typography.caption1,
                color = colors.textTertiary
            )
        }
    }
}
