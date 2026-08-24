package dev.iosfeel.physics.decay

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Frictional inertial decay specification for scrolling, flings, and momentum transfer.
 *
 * @property friction Deceleration rate per second. Typical values in range [0.85f, 0.99f].
 * @property velocityMultiplier Multiplier applied to incoming velocity before decay starts.
 * @property minimumVelocity Absolute velocity (in px/s or units/s) below which motion halts.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSDecaySpec(
    val friction: Float = 0.985f,
    val velocityMultiplier: Float = 1f,
    val minimumVelocity: Float = 15f
) {
    init {
        require(!friction.isNaN() && friction in 0.1f..0.999f) { "friction must be in range [0.1f, 0.999f] (was $friction)" }
        require(!velocityMultiplier.isNaN() && velocityMultiplier > 0f) { "velocityMultiplier must be positive" }
        require(!minimumVelocity.isNaN() && minimumVelocity >= 0f) { "minimumVelocity must be non-negative" }
    }
}

/**
 * Result of an inertial decay motion or boundary momentum transfer.
 *
 * @property consumedDistance Distance traversed during decay before stopping or hitting boundary.
 * @property remainingVelocity Residual velocity when motion halted or encountered a boundary.
 * @property hitBoundary True if decay stopped due to encountering a defined physical boundary.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSDecayResult(
    val consumedDistance: Float,
    val remainingVelocity: Float,
    val hitBoundary: Boolean
)
