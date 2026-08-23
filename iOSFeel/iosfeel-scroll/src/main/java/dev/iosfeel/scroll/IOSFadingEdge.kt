package dev.iosfeel.scroll

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a smooth alpha gradient fade mask to the edges of a composable.
 * Essential for modern iOS lists so content smoothly dissolves before floating bars.
 */
fun Modifier.iosFadingEdge(
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
    start: Dp = 0.dp,
    end: Dp = 0.dp
): Modifier {
    if (top <= 0.dp && bottom <= 0.dp && start <= 0.dp && end <= 0.dp) {
        return this
    }

    return this
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithContent {
            drawContent()
            if (size.width <= 0f || size.height <= 0f) return@drawWithContent

            val topPx = top.toPx().coerceIn(0f, size.height / 2f)
            val bottomPx = bottom.toPx().coerceIn(0f, size.height / 2f)
            val startPx = start.toPx().coerceIn(0f, size.width / 2f)
            val endPx = end.toPx().coerceIn(0f, size.width / 2f)

            // Top Fade
            if (topPx > 0.5f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 0f,
                        endY = topPx
                    ),
                    topLeft = Offset.Zero,
                    size = Size(width = size.width, height = topPx),
                    blendMode = BlendMode.DstIn
                )
            }

            // Bottom Fade
            if (bottomPx > 0.5f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startY = size.height - bottomPx,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, size.height - bottomPx),
                    size = Size(width = size.width, height = bottomPx),
                    blendMode = BlendMode.DstIn
                )
            }

            // Start Fade
            if (startPx > 0.5f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startX = 0f,
                        endX = startPx
                    ),
                    topLeft = Offset.Zero,
                    size = Size(width = startPx, height = size.height),
                    blendMode = BlendMode.DstIn
                )
            }

            // End Fade
            if (endPx > 0.5f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startX = size.width - endPx,
                        endX = size.width
                    ),
                    topLeft = Offset(size.width - endPx, 0f),
                    size = Size(width = endPx, height = size.height),
                    blendMode = BlendMode.DstIn
                )
            }
        }
}

fun Modifier.iosVerticalFade(
    top: Dp = 24.dp,
    bottom: Dp = 36.dp
): Modifier = iosFadingEdge(top = top, bottom = bottom)

fun Modifier.iosHorizontalFade(
    start: Dp = 20.dp,
    end: Dp = 20.dp
): Modifier = iosFadingEdge(start = start, end = end)
