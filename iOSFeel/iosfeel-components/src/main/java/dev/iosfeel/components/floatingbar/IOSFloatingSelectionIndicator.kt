package dev.iosfeel.components.floatingbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A floating, physical selector indicator that lifts above the tab bar
 * during a scrub interaction, following the user's finger with dynamic elevation.
 */
@Composable
fun IOSFloatingSelectionIndicator(
    state: IOSFloatingTabScrubState,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val lift = state.liftProgress
    if (lift <= 0.01f && state.phase == IOSFloatingTabInteractionPhase.Idle) {
        return
    }

    val config = state.config
    val currentHovered = state.hoveredIndex
    val tabWidth = if (currentHovered in state.tabWidths.indices && state.tabWidths[currentHovered] > 0f) {
        state.tabWidths[currentHovered]
    } else {
        120f
    }
    val indicatorWidthDp = (tabWidth * 0.85f).coerceAtLeast(48f).dp

    Box(
        modifier = modifier
            .graphicsLayer {
                val liftDistancePx = config.selectorLift.toPx()
                translationX = state.dragX - (size.width / 2f)
                translationY = -lift * liftDistancePx
                scaleX = state.selectorScale
                scaleY = state.selectorScale
            }
            .width(indicatorWidthDp)
            .height(44.dp)
            .shadow(
                elevation = (10.dp * lift).coerceAtLeast(0.dp),
                shape = RoundedCornerShape(22.dp),
                spotColor = activeColor.copy(alpha = 0.40f * lift),
                ambientColor = Color.Black.copy(alpha = 0.20f * lift)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(activeColor.copy(alpha = 0.16f + 0.16f * lift))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.20f + 0.30f * lift),
                shape = RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {}
}
