package dev.iosfeel.components.segmented

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.interaction.IOSComponentShapes
import dev.iosfeel.core.tokens.IOSSpacing
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Standard iOS Segmented Control V2 with single moving selection pill, tap transitions, and continuous scrub physics.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun <T> IOSSegmentedControl(
    items: List<IOSSegmentedItem<T>>,
    selectedValue: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF2C2C2E).copy(alpha = 0.6f),
    selectedPillColor: Color = Color(0xFF636366),
    selectedTextColor: Color = Color.White,
    unselectedTextColor: Color = Color.White.copy(alpha = 0.7f),
    hapticsEnabled: Boolean = true,
    state: IOSSegmentedState<T> = rememberIOSSegmentedState(
        items = items.map { it.value },
        selected = selectedValue
    )
) {
    if (items.isEmpty()) return

    val haptics = rememberIOSHaptics()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedValue) {
        if (!state.isScrubbing) {
            state.select(selectedValue)
        }
    }

    val velocityTracker = VelocityTracker()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(IOSComponentShapes.Control)
            .background(containerColor)
            .padding(IOSSpacing.XXSmall)
            .selectableGroup()
    ) {
        val totalWidth = maxWidth
        val segmentWidth = totalWidth / items.size
        val segmentWidthPx = with(density) { segmentWidth.toPx() }
        val pillOffsetPx = (segmentWidthPx * state.indexProgress).roundToInt()

        // Single Moving Selection Pill
        Box(
            modifier = Modifier
                .offset { IntOffset(x = pillOffsetPx, y = 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(selectedPillColor)
        )

        // Interactive Layer & Segment Options Row
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(items, segmentWidthPx) {
                    detectTapGestures { offset ->
                        val clickedIndex = (offset.x / segmentWidthPx).toInt().coerceIn(0, items.size - 1)
                        val item = items[clickedIndex]
                        if (item.value != selectedValue) {
                            if (hapticsEnabled) haptics.selection()
                            scope.launch { state.select(item.value) }
                            onSelected(item.value)
                        }
                    }
                }
                .pointerInput(items, segmentWidthPx) {
                    detectDragGestures(
                        onDragStart = {
                            velocityTracker.resetTracking()
                        },
                        onDragEnd = {
                            val vX = velocityTracker.calculateVelocity().x
                            val vProgress = vX / segmentWidthPx.coerceAtLeast(1f)
                            scope.launch {
                                val committedItem = state.release(vProgress)
                                if (committedItem != selectedValue) {
                                    if (hapticsEnabled) haptics.selection()
                                    onSelected(committedItem)
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { state.select(selectedValue) }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            val deltaProgress = dragAmount.x / segmentWidthPx.coerceAtLeast(1f)
                            val prevCandidate = state.candidateIndex
                            scope.launch {
                                val newCandidate = state.scrubTo(state.indexProgress + deltaProgress)
                                if (newCandidate != prevCandidate && hapticsEnabled) {
                                    haptics.selection()
                                }
                            }
                        }
                    )
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == state.selectedIndex
                    val distance = abs(index - state.indexProgress)
                    val labelAlpha = (1f - distance.coerceIn(0f, 1f) * 0.4f)
                    val textColor = lerp(unselectedTextColor, selectedTextColor, (1f - distance).coerceIn(0f, 1f))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics {
                                role = Role.Tab
                                selected = isSelected
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = textColor.copy(alpha = labelAlpha),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
