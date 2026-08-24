package dev.iosfeel.motion.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import dev.iosfeel.motion.morph.IOSMorphBounds
import kotlin.math.roundToInt

/**
 * Renders an animated floating overlay interpolating between registered source and target shared element bounds.
 */
@Composable
fun IOSSharedElementTransition(
    key: IOSSharedElementKey,
    progress: Float,
    registry: IOSSharedElementRegistry = LocalIOSSharedElementRegistry.current,
    fallbackContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val src = registry.getSourceBounds(key)
    val tgt = registry.getTargetBounds(key)

    if (src == null || tgt == null) {
        fallbackContent()
        return
    }

    val interpolated = IOSMorphBounds.lerp(src, tgt, progress)
    val density = LocalDensity.current
    val widthDp = with(density) { interpolated.width.toDp() }
    val heightDp = with(density) { interpolated.height.toDp() }

    Box(
        modifier = Modifier
            .offset { IntOffset(x = interpolated.left.roundToInt(), y = interpolated.top.roundToInt()) }
            .size(width = widthDp, height = heightDp)
    ) {
        content()
    }
}
