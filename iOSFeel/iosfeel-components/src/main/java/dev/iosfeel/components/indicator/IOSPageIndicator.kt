package dev.iosfeel.components.indicator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.core.tokens.IOSColors
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Standard iOS Page Indicator with stretching active pill indicator.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = IOSColors.SystemBlue,
    inactiveColor: Color = Color.White.copy(alpha = 0.25f),
    dotSize: Dp = 8.dp,
    spacing: Dp = 6.dp
) {
    if (pageCount <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val isSelected = i == currentPage
            val widthAnim by animateFloatAsState(
                targetValue = if (isSelected) 20f else 8f,
                animationSpec = spring(
                    dampingRatio = IOSSprings.Selection.dampingRatio,
                    stiffness = IOSSprings.Selection.stiffness
                ),
                label = "dotWidth"
            )

            Box(
                modifier = Modifier
                    .height(dotSize)
                    .width(widthAnim.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor)
            )
        }
    }
}
