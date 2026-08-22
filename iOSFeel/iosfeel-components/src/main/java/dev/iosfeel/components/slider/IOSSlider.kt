package dev.iosfeel.components.slider

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.iosfeel.core.tokens.IOSMotionTokens
import dev.iosfeel.haptics.rememberIOSHaptics
import kotlin.math.roundToInt

@Composable
fun IOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    activeColor: Color = Color(0xFF007AFF),
    inactiveColor: Color = Color(0xFF3A3A3C),
    thumbColor: Color = Color.White,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val haptics = rememberIOSHaptics()
    val density = LocalDensity.current

    val normalized = normalizeSliderValue(value, valueRange)
    val thumbScale = remember { Animatable(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var lastStepIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(isDragging) {
        thumbScale.animateTo(
            targetValue = if (isDragging) 1.25f else 1.0f,
            animationSpec = spring(
                stiffness = IOSMotionTokens.PressStiffness,
                dampingRatio = IOSMotionTokens.PressDampingRatio
            )
        )
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
        val thumbOffsetPx = (totalWidthPx * normalized).roundToInt()

        // Track Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (enabled) inactiveColor else inactiveColor.copy(alpha = 0.38f))
        )

        // Active Track Fill
        val activeWidth = maxWidth * normalized
        Box(
            modifier = Modifier
                .size(width = activeWidth, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (enabled) activeColor else activeColor.copy(alpha = 0.38f))
        )

        // Touch & Drag Handling Overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(enabled, totalWidthPx, steps) {
                    if (!enabled) return@pointerInput

                    detectTapGestures(
                        onPress = { offset ->
                            isDragging = true
                            val rawNorm = ((offset.x - 14.dp.toPx()) / totalWidthPx).coerceIn(0f, 1f)
                            val finalNorm = if (steps > 0) snapToStep(rawNorm, steps) else rawNorm
                            onValueChange(denormalizeSliderValue(finalNorm, valueRange))

                            tryAwaitRelease()
                            isDragging = false
                            onValueChangeFinished?.invoke()
                        }
                    )
                }
                .pointerInput(enabled, totalWidthPx, steps) {
                    if (!enabled) return@pointerInput

                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            onValueChangeFinished?.invoke()
                        },
                        onDragCancel = {
                            isDragging = false
                            onValueChangeFinished?.invoke()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentPx = (totalWidthPx * normalized) + dragAmount.x
                            val rawNorm = (currentPx / totalWidthPx).coerceIn(0f, 1f)
                            val finalNorm = if (steps > 0) {
                                val snapped = snapToStep(rawNorm, steps)
                                val currentStep = (snapped * (steps + 1)).roundToInt()
                                if (currentStep != lastStepIndex) {
                                    lastStepIndex = currentStep
                                    haptics.selection()
                                }
                                snapped
                            } else {
                                rawNorm
                            }
                            onValueChange(denormalizeSliderValue(finalNorm, valueRange))
                        }
                    )
                }
        )

        // Thumb Handle
        Box(
            modifier = Modifier
                .offset { IntOffset(x = thumbOffsetPx, y = 0) }
                .size(28.dp)
                .graphicsLayer {
                    scaleX = thumbScale.value
                    scaleY = thumbScale.value
                }
                .shadow(elevation = 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
