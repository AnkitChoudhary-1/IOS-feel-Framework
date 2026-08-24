package dev.iosfeel.components.list

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.detent.IOSDetent
import dev.iosfeel.physics.detent.IOSDetentResolver
import dev.iosfeel.physics.resistance.IOSResistanceSpec
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Immutable
data class IOSSwipeAction(
    val id: String,
    val label: String,
    val backgroundColor: Color,
    val textColor: Color = Color.White,
    val isDestructive: Boolean = false,
    val width: Dp = 74.dp,
    val onClick: () -> Unit
)

/**
 * High-fidelity iOS Swipe Action container supporting elastic resistance and velocity snap.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSSwipeActions(
    actions: List<IOSSwipeAction>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    springSpec: IOSSpringSpec = IOSSprings.Responsive,
    content: @Composable () -> Unit
) {
    if (actions.isEmpty() || !enabled) {
        content()
        return
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptics = rememberIOSHaptics()

    val totalActionsWidth = actions.fold(0.dp) { acc, a -> acc + a.width }
    val maxOpenOffsetPx = with(density) { totalActionsWidth.toPx() }

    val offsetAnim = remember { Animatable(0f) }
    val velocityTracker = VelocityTracker()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalWidthPx = with(density) { maxWidth.toPx() }

        // Action Buttons Row (Behind Content)
        Row(
            modifier = Modifier
                .matchParentSize(),
            horizontalArrangement = ArrangementEnd
        ) {
            actions.forEach { action ->
                Box(
                    modifier = Modifier
                        .width(action.width)
                        .fillMaxHeight()
                        .background(action.backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (action.isDestructive) {
                                haptics.impact(IOSImpact.Heavy)
                            } else {
                                haptics.impact(IOSImpact.Medium)
                            }
                            action.onClick()
                            scope.launch {
                                offsetAnim.animateTo(
                                    targetValue = 0f,
                                    animationSpec = SpringSpec(
                                        dampingRatio = springSpec.dampingRatio,
                                        stiffness = springSpec.stiffness
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = action.label,
                        color = action.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Swiping Foreground Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = offsetAnim.value.roundToInt(), y = 0) }
                .pointerInput(enabled, maxOpenOffsetPx) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            velocityTracker.resetTracking()
                        },
                        onDragEnd = {
                            val vX = velocityTracker.calculateVelocity().x
                            val detents = listOf(
                                IOSDetent(value = 0f, key = 0f),
                                IOSDetent(value = -maxOpenOffsetPx, key = -maxOpenOffsetPx)
                            )
                            val decision = IOSDetentResolver.resolve(
                                position = offsetAnim.value,
                                velocity = vX,
                                detents = detents,
                                velocityThreshold = 400f
                            )
                            scope.launch {
                                offsetAnim.animateTo(
                                    targetValue = decision.target.value,
                                    initialVelocity = vX,
                                    animationSpec = SpringSpec(
                                        dampingRatio = springSpec.dampingRatio,
                                        stiffness = springSpec.stiffness
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetAnim.animateTo(0f)
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            val current = offsetAnim.value
                            val nextRaw = current + dragAmount
                            val resisted = if (nextRaw < -maxOpenOffsetPx) {
                                val over = abs(nextRaw) - maxOpenOffsetPx
                                -(maxOpenOffsetPx + IOSResistanceSpec.Standard.apply(over))
                            } else if (nextRaw > 0f) {
                                IOSResistanceSpec.Standard.apply(nextRaw)
                            } else {
                                nextRaw
                            }
                            scope.launch {
                                offsetAnim.snapTo(resisted)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

private val ArrangementEnd = androidx.compose.foundation.layout.Arrangement.End
