package dev.iosfeel.motion.morph

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.util.lerp

/**
 * Immutable rectangular boundary in coordinate space used for continuous surface and element morphs.
 */
@Immutable
data class IOSMorphBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float
        get() = (right - left).coerceAtLeast(0f)

    val height: Float
        get() = (bottom - top).coerceAtLeast(0f)

    val center: Offset
        get() = Offset(left + width / 2f, top + height / 2f)

    val size: Size
        get() = Size(width, height)

    val topLeft: Offset
        get() = Offset(left, top)

    companion object {
        val Zero = IOSMorphBounds(0f, 0f, 0f, 0f)

        /**
         * Linearly interpolates between two bounds according to [progress] (0f to 1f).
         */
        fun lerp(start: IOSMorphBounds, end: IOSMorphBounds, progress: Float): IOSMorphBounds {
            val p = progress.coerceIn(0f, 1f)
            return IOSMorphBounds(
                left = lerp(start.left, end.left, p),
                top = lerp(start.top, end.top, p),
                right = lerp(start.right, end.right, p),
                bottom = lerp(start.bottom, end.bottom, p)
            )
        }
    }
}
