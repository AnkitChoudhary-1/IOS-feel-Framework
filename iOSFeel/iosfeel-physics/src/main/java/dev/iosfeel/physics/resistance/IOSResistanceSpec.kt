package dev.iosfeel.physics.resistance

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign

/**
 * Nonlinear resistance curve specification for overscroll, elastic boundaries, and tab scrub drag limits.
 *
 * Rather than scaling motion linearly, progressive nonlinear resistance becomes
 * increasingly stiff the farther the user pulls past the physical boundary.
 *
 * @property factor Scale coefficient on the raw pull distance.
 * @property exponent Power curve parameter (0 < exponent <= 1.0). Lower = more aggressive resistance.
 * @property maximumDistance Hard upper bound on the displacement.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSResistanceSpec(
    val factor: Float = 0.55f,
    val exponent: Float = 0.82f,
    val maximumDistance: Float = Float.POSITIVE_INFINITY
) {
    init {
        require(!factor.isNaN() && factor > 0f) { "factor must be positive (was $factor)" }
        require(!exponent.isNaN() && exponent in 0.01f..1.0f) { "exponent must be in range (0f, 1.0f] (was $exponent)" }
        require(!maximumDistance.isNaN() && maximumDistance > 0f) { "maximumDistance must be positive" }
    }

    /**
     * Calculates the resisted displacement corresponding to [rawDistance].
     */
    fun apply(rawDistance: Float): Float {
        if (rawDistance == 0f || rawDistance.isNaN()) return 0f
        val sign = sign(rawDistance)
        val absDistance = abs(rawDistance)
        val resisted = factor * absDistance.pow(exponent)
        return sign * min(resisted, maximumDistance)
    }

    companion object {
        /**
         * Standard balanced resistance for iOS scroll views and floating bars.
         */
        val Standard = IOSResistanceSpec(factor = 0.55f, exponent = 0.82f)

        /**
         * Soft resistance with higher displacement freedom before firm resistance kicks in.
         */
        val Soft = IOSResistanceSpec(factor = 0.70f, exponent = 0.88f)

        /**
         * Firm resistance with immediate elastic stiffness (e.g. sliders, switches).
         */
        val Firm = IOSResistanceSpec(factor = 0.35f, exponent = 0.75f)
    }
}
