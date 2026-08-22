package dev.iosfeel.components.segmented

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import dev.iosfeel.core.tokens.IOSMotionTokens
import dev.iosfeel.core.tokens.IOSShapes
import dev.iosfeel.core.tokens.IOSSpacing
import dev.iosfeel.haptics.rememberIOSHaptics
import kotlin.math.roundToInt

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
    hapticsEnabled: Boolean = true
) {
    if (items.isEmpty()) return

    val haptics = rememberIOSHaptics()
    val density = LocalDensity.current

    val selectedIndex = items.indexOfFirst { it.value == selectedValue }.coerceAtLeast(0)
    val animatedIndex = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        animatedIndex.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(
                stiffness = IOSMotionTokens.SegmentedStiffness,
                dampingRatio = IOSMotionTokens.SegmentedDampingRatio
            )
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(IOSShapes.Control)
            .background(containerColor)
            .padding(IOSSpacing.XXSmall)
            .selectableGroup()
    ) {
        val totalWidth = maxWidth
        val segmentWidth = totalWidth / items.size
        val segmentWidthPx = with(density) { segmentWidth.toPx() }
        val pillOffsetPx = (segmentWidthPx * animatedIndex.value).roundToInt()

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

        // Segment Options Row
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics {
                            role = Role.Tab
                            selected = isSelected
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (item.value != selectedValue) {
                                if (hapticsEnabled) {
                                    haptics.selection()
                                }
                                onSelected(item.value)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) selectedTextColor else unselectedTextColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
