package dev.iosfeel.physics.spring

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Semantic spring specification in iOSFeel V2.
 *
 * Configured via two human-intuitive parameters:
 * @property response The duration (in seconds) of one full undamped oscillation cycle. Lower = snappier, higher = lazier.
 * @property bounce The degree of overshoot/elasticity in range [0f, 1f] (0 = critically damped / no overshoot, > 0 = bouncy).
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSSpringSpec(
    val response: Float = 0.42f,
    val bounce: Float = 0.10f
) {
    init {
        require(!response.isNaN() && response > 0f) { "response must be a positive number (was $response)" }
        require(!bounce.isNaN() && bounce in -1f..1f) { "bounce must be in range [-1f, 1f] (was $bounce)" }
    }

    /**
     * Calculates the undamped angular frequency $\omega_0 = 2\pi / \text{response}$.
     */
    val naturalFrequency: Float
        get() = (2f * PI.toFloat()) / response.coerceAtLeast(0.01f)

    /**
     * Converts semantic response and bounce into the raw physical spring stiffness ($k = \omega_0^2$).
     */
    val stiffness: Float
        get() = naturalFrequency.pow(2)

    /**
     * Converts semantic bounce into the dimensionless damping ratio $\zeta$.
     */
    val dampingRatio: Float
        get() {
            return if (bounce >= 0f) {
                (1f - bounce).coerceIn(0.05f, 2.0f)
            } else {
                (1f / (1f + bounce.coerceIn(-0.95f, 0f))).coerceIn(1.0f, 2.5f)
            }
        }

    /**
     * Converts this iOSFeel V2 spring spec into a Compose [SpringSpec].
     */
    fun toComposeSpringSpec(visibilityThreshold: Float = 0.001f): SpringSpec<Float> {
        return SpringSpec(
            dampingRatio = dampingRatio,
            stiffness = stiffness,
            visibilityThreshold = visibilityThreshold
        )
    }

    /**
     * Returns a restrained version suitable for reduced-motion environments.
     */
    fun toReducedMotion(): IOSSpringSpec {
        return IOSSpringSpec(
            response = (response * 0.6f).coerceIn(0.12f, 0.30f),
            bounce = 0f
        )
    }

    companion object {
        /**
         * Advanced factory to create an [IOSSpringSpec] from raw physical parameters (stiffness and dampingRatio).
         */
        fun physical(stiffness: Float, dampingRatio: Float): IOSSpringSpec {
            val safeStiffness = stiffness.coerceAtLeast(1f)
            val safeDampingRatio = dampingRatio.coerceAtLeast(0.05f)
            val omega0 = sqrt(safeStiffness)
            val response = (2f * PI.toFloat()) / omega0
            val bounce = if (safeDampingRatio <= 1f) {
                1f - safeDampingRatio
            } else {
                (1f / safeDampingRatio) - 1f
            }
            return IOSSpringSpec(
                response = response.coerceIn(0.05f, 2.5f),
                bounce = bounce.coerceIn(-1f, 1f)
            )
        }
    }
}
