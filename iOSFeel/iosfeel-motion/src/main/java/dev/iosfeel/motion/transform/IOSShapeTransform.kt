package dev.iosfeel.motion.transform

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import dev.iosfeel.motion.morph.IOSMorphBounds
import dev.iosfeel.motion.morph.IOSMotionPath
import dev.iosfeel.motion.morph.intervalProgress

/**
 * Shape transform specification for interpolating corner radii.
 */
@Immutable
data class IOSShapeTransform(
    val startRadius: Dp,
    val endRadius: Dp
) {
    fun transform(progress: Float): Dp = lerp(startRadius, endRadius, progress.coerceIn(0f, 1f))
}

/**
 * Bounds transform with geometric motion path support (Linear or Arc).
 */
@Immutable
data class IOSBoundsTransform(
    val start: IOSMorphBounds,
    val end: IOSMorphBounds,
    val path: IOSMotionPath = IOSMotionPath.Linear
) {
    fun transform(progress: Float): IOSMorphBounds {
        val p = progress.coerceIn(0f, 1f)
        return when (path) {
            IOSMotionPath.Linear -> IOSMorphBounds.lerp(start, end, p)
            IOSMotionPath.Arc -> {
                val startCenter = start.center
                val endCenter = end.center
                // Arc displacement calculation: adds slight parabolic lift in perpendicular direction
                val arcOffset = Offset(
                    x = androidx.compose.ui.util.lerp(startCenter.x, endCenter.x, p),
                    y = androidx.compose.ui.util.lerp(startCenter.y, endCenter.y, p) - (kotlin.math.sin(p * Math.PI).toFloat() * 16f)
                )
                val width = androidx.compose.ui.util.lerp(start.width, end.width, p)
                val height = androidx.compose.ui.util.lerp(start.height, end.height, p)
                IOSMorphBounds(
                    left = arcOffset.x - width / 2f,
                    top = arcOffset.y - height / 2f,
                    right = arcOffset.x + width / 2f,
                    bottom = arcOffset.y + height / 2f
                )
            }
        }
    }
}

/**
 * Staggered alpha transform over sub-intervals of morph progress.
 */
@Immutable
data class IOSAlphaTransform(
    val startProgress: Float,
    val endProgress: Float,
    val invert: Boolean = false
) {
    fun transform(progress: Float): Float {
        val raw = intervalProgress(progress, startProgress, endProgress)
        return if (invert) 1f - raw else raw
    }
}
