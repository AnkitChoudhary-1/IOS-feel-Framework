package dev.iosfeel.motion.expandable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.motion.morph.intervalProgress
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Universal physical expandable surface container transforming smoothly between collapsed mini mode and full expanded view.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSExpandableSurface(
    state: IOSExpandableSurfaceState,
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = 64.dp,
    collapsedCornerRadius: Dp = 16.dp,
    expandedCornerRadius: Dp = 24.dp,
    containerColor: Color = Color(0xFF1C1C1E),
    backdrop: IOSBackdropState? = null,
    material: Boolean = true,
    collapsed: @Composable () -> Unit,
    expanded: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val velocityTracker = VelocityTracker()

    val progress = state.progress
    val cornerRadius = lerp(collapsedCornerRadius, expandedCornerRadius, progress)

    val collapsedAlpha = 1f - intervalProgress(progress, 0f, 0.25f)
    val expandedAlpha = intervalProgress(progress, 0.35f, 0.9f)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }
        val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
        val currentHeightPx = androidx.compose.ui.util.lerp(collapsedHeightPx, totalHeightPx, progress)
        val currentTopOffsetPx = totalHeightPx - currentHeightPx

        val shape = RoundedCornerShape(cornerRadius)

        val surfaceModifier = Modifier
            .offset { IntOffset(x = 0, y = currentTopOffsetPx.roundToInt()) }
            .fillMaxWidth()
            .height(with(density) { currentHeightPx.toDp() })
            .shadow(elevation = 8.dp, shape = shape)
            .clip(shape)
            .pointerInput(Unit) {
                detectTapGestures {
                    if (state.isCollapsed) {
                        scope.launch { state.expand() }
                    }
                }
            }
            .pointerInput(totalHeightPx) {
                detectDragGestures(
                    onDragStart = {
                        velocityTracker.resetTracking()
                    },
                    onDragEnd = {
                        val vY = velocityTracker.calculateVelocity().y
                        val vProgress = -vY / totalHeightPx
                        scope.launch {
                            state.release(vProgress)
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            if (state.progress >= 0.5f) state.expand() else state.collapse()
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        val deltaProgress = -dragAmount.y / (totalHeightPx - collapsedHeightPx).coerceAtLeast(1f)
                        scope.launch {
                            state.dragTo(state.progress + deltaProgress)
                        }
                    }
                )
            }

        if (material) {
            IOSMaterialSurface(
                backdrop = backdrop,
                config = IOSMaterialConfig(
                    style = IOSMaterialStyle.Regular,
                    cornerRadius = cornerRadius
                ),
                modifier = surfaceModifier
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (collapsedAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(collapsedHeight)
                                .graphicsLayer { alpha = collapsedAlpha }
                        ) {
                            collapsed()
                        }
                    }

                    if (expandedAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = expandedAlpha }
                        ) {
                            expanded()
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = surfaceModifier.background(containerColor)
            ) {
                if (collapsedAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(collapsedHeight)
                            .graphicsLayer { alpha = collapsedAlpha }
                    ) {
                        collapsed()
                    }
                }

                if (expandedAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = expandedAlpha }
                    ) {
                        expanded()
                    }
                }
            }
        }
    }
}
