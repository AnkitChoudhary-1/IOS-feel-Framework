package dev.iosfeel.components.slider

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Standard iOS Slider V2 with press expansion, detents, haptic feedback, and deferred commit support.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    detents: List<Float> = emptyList(),
    behavior: IOSSliderBehavior = IOSSliderBehavior.Immediate,
    activeColor: Color = Color(0xFF007AFF),
    inactiveColor: Color = Color(0xFF3A3A3C),
    thumbColor: Color = Color.White,
    onValueChangeFinished: (() -> Unit)? = null,
    onValueCommit: ((Float) -> Unit)? = null
) {
    val haptics = rememberIOSHaptics()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val initialNorm = normalizeSliderValue(value, valueRange)
    val state = rememberIOSSliderState(initialNormalized = initialNorm, detents = detents)

    var previewNormalized by remember { mutableFloatStateOf(initialNorm) }

    LaunchedEffect(value) {
        if (!state.isDragging) {
            val norm = normalizeSliderValue(value, valueRange)
            previewNormalized = norm
            state.snapTo(norm)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(value, valueRange, steps)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = with(density) { (maxWidth - 28.dp).toPx().coerceAtLeast(1f) }
        val displayedNorm = if (state.isDragging) previewNormalized else state.progress
        val thumbOffsetPx = (totalWidthPx * displayedNorm).roundToInt()

        val trackHeight = if (state.isDragging) 6.dp else 4.dp

        // Track Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(if (enabled) inactiveColor else inactiveColor.copy(alpha = 0.38f))
        )

        // Active Track Fill
        val activeWidth = maxWidth * displayedNorm
        Box(
            modifier = Modifier
                .size(width = activeWidth, height = trackHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(if (enabled) activeColor else activeColor.copy(alpha = 0.38f))
        )

        // Touch & Drag Handling Overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(enabled, totalWidthPx, steps, behavior) {
                    if (!enabled) return@pointerInput

                    detectTapGestures(
                        onPress = { offset ->
                            val rawNorm = ((offset.x - 14.dp.toPx()) / totalWidthPx).coerceIn(0f, 1f)
                            val finalNorm = if (steps > 0) snapToStep(rawNorm, steps) else rawNorm
                            previewNormalized = finalNorm
                            scope.launch { state.dragTo(finalNorm) }

                            if (behavior == IOSSliderBehavior.Immediate) {
                                onValueChange(denormalizeSliderValue(finalNorm, valueRange))
                            }

                            tryAwaitRelease()
                            scope.launch {
                                val committedNorm = state.release()
                                val finalVal = denormalizeSliderValue(committedNorm, valueRange)
                                onValueChange(finalVal)
                                onValueCommit?.invoke(finalVal)
                                onValueChangeFinished?.invoke()
                            }
                        }
                    )
                }
                .pointerInput(enabled, totalWidthPx, steps, behavior) {
                    if (!enabled) return@pointerInput

                    detectDragGestures(
                        onDragStart = {
                            scope.launch { state.dragTo(previewNormalized) }
                        },
                        onDragEnd = {
                            scope.launch {
                                val committedNorm = state.release()
                                val finalVal = denormalizeSliderValue(committedNorm, valueRange)
                                onValueChange(finalVal)
                                onValueCommit?.invoke(finalVal)
                                onValueChangeFinished?.invoke()
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                val norm = normalizeSliderValue(value, valueRange)
                                state.release()
                                state.snapTo(norm)
                                previewNormalized = norm
                                onValueChangeFinished?.invoke()
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentPx = (totalWidthPx * previewNormalized) + dragAmount.x
                            val rawNorm = (currentPx / totalWidthPx).coerceIn(0f, 1f)
                            val finalNorm = if (steps > 0) snapToStep(rawNorm, steps) else rawNorm
                            previewNormalized = finalNorm

                            scope.launch {
                                val snapped = state.dragTo(finalNorm)
                                if (detents.isNotEmpty() && snapped != state.lastSnappedDetent) {
                                    state.lastSnappedDetent = snapped
                                    haptics.selection()
                                }
                            }

                            if (behavior == IOSSliderBehavior.Immediate) {
                                onValueChange(denormalizeSliderValue(finalNorm, valueRange))
                            }
                        }
                    )
                }
        )

        // Expanding Thumb Handle
        Box(
            modifier = Modifier
                .offset { IntOffset(x = thumbOffsetPx, y = 0) }
                .size(28.dp)
                .graphicsLayer {
                    scaleX = state.thumbScale
                    scaleY = state.thumbScale
                }
                .shadow(elevation = if (state.isDragging) 6.dp else 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
