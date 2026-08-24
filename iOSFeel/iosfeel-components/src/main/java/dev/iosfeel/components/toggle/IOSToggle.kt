package dev.iosfeel.components.toggle

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.interaction.IOSComponentShapes
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Standard iOS Toggle Switch V2 with draggable thumb, coupled track physics, and velocity-aware detent resolution.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = IOSToggleDefaults.ActiveTrackColor,
    inactiveColor: Color = IOSToggleDefaults.DarkInactiveTrackColor,
    hapticsEnabled: Boolean = true,
    state: IOSToggleState = rememberIOSToggleState(checked = checked)
) {
    val haptics = rememberIOSHaptics()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(checked) {
        if (!state.isDragging) {
            state.animateTo(checked)
        }
    }

    val maxTravelPx = with(density) {
        (IOSToggleDefaults.TrackWidth - IOSToggleDefaults.ThumbSize - (IOSToggleDefaults.ThumbPadding * 2)).toPx()
    }

    val trackColor = lerp(inactiveColor, activeColor, state.progress)
    val thumbOffsetPx = (maxTravelPx * state.progress).roundToInt()

    val velocityTracker = VelocityTracker()

    Box(
        modifier = modifier
            .semantics { role = Role.Switch }
            .size(
                width = IOSToggleDefaults.TrackWidth,
                height = IOSToggleDefaults.TrackHeight
            )
            .clip(IOSComponentShapes.Pill)
            .background(if (enabled) trackColor else trackColor.copy(alpha = 0.38f))
            .pointerInput(enabled, checked) {
                if (enabled) {
                    detectTapGestures {
                        val next = !checked
                        if (hapticsEnabled) haptics.selection()
                        scope.launch {
                            state.animateTo(next)
                        }
                        onCheckedChange(next)
                    }
                }
            }
            .pointerInput(enabled, maxTravelPx) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = {
                            velocityTracker.resetTracking()
                        },
                        onDragEnd = {
                            val vX = velocityTracker.calculateVelocity().x
                            val vProgress = vX / maxTravelPx.coerceAtLeast(1f)
                            scope.launch {
                                val result = state.release(vProgress)
                                if (result != checked) {
                                    if (hapticsEnabled) haptics.selection()
                                    onCheckedChange(result)
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                state.animateTo(checked)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            val deltaProgress = dragAmount.x / maxTravelPx.coerceAtLeast(1f)
                            scope.launch {
                                state.dragTo(state.progress + deltaProgress)
                            }
                        }
                    )
                }
            }
            .padding(IOSToggleDefaults.ThumbPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        // Draggable / Springing Thumb with Horizontal Stretch
        Box(
            modifier = Modifier
                .offset { IntOffset(x = thumbOffsetPx, y = 0) }
                .size(IOSToggleDefaults.ThumbSize)
                .graphicsLayer {
                    scaleX = state.thumbScaleX
                    scaleY = if (state.isDragging) 0.95f else 1f
                }
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(IOSToggleDefaults.ThumbColor)
        )
    }
}
