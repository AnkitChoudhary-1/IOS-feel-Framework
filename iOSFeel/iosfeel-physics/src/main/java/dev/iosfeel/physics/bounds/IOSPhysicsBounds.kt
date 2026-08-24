package dev.iosfeel.physics.bounds

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Behavior when motion encounters a defined physical boundary.
 */
@ExperimentalIOSFeelV2Api
enum class IOSBoundaryBehavior {
    /**
     * Clamps position strictly at boundary and zeroes remaining velocity.
     */
    Clamp,

    /**
     * Applies progressive nonlinear resistance beyond boundary.
     */
    Resist,

    /**
     * Bounces elastically off boundary reversing velocity with damping.
     */
    Bounce
}

/**
 * Defines numeric bounds for physical motion.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSPhysicsBounds(
    val min: Float = Float.NEGATIVE_INFINITY,
    val max: Float = Float.POSITIVE_INFINITY,
    val behavior: IOSBoundaryBehavior = IOSBoundaryBehavior.Clamp
) {
    init {
        require(min <= max) { "min ($min) must be <= max ($max)" }
    }

    /**
     * Checks if [value] is within [min] and [max].
     */
    fun contains(value: Float): Boolean = value in min..max
}

/**
 * Normalizes a pixel-based velocity to a progress unit per second based on total distance.
 */
@ExperimentalIOSFeelV2Api
fun normalizeVelocity(velocityPxPerSecond: Float, distancePx: Float): Float {
    if (distancePx <= 0f || distancePx.isNaN()) {
        return 0f
    }
    return velocityPxPerSecond / distancePx
}
