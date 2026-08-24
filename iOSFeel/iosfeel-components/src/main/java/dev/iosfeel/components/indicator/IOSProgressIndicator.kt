package dev.iosfeel.components.indicator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.core.tokens.IOSColors

/**
 * Standard iOS Linear Progress Bar.
 */
@Composable
fun IOSLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = IOSColors.SystemBlue,
    trackColor: Color = Color.White.copy(alpha = 0.15f),
    height: Dp = 4.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(activeColor)
        )
    }
}

/**
 * Standard iOS Circular Activity Spinner.
 */
@Composable
fun IOSCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = IOSColors.SystemBlue,
    strokeWidth: Dp = 3.dp
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth
    )
}
