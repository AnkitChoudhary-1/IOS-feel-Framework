package dev.iosfeel.scroll

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.resistance.IOSResistanceSpec
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Configuration options for scroll behavior and overscroll physics in iOSFeel V2.
 */
@Immutable
@OptIn(ExperimentalIOSFeelV2Api::class)
data class IOSScrollConfig(
    val velocityMultiplier: Float = 1f,
    val minimumFlingVelocity: Float = 25f,
    val resistance: IOSResistanceSpec = IOSResistanceSpec.Standard,
    val returnSpring: IOSSpringSpec = IOSSprings.ScrollReturn,
    val flingVelocityMultiplier: Float = velocityMultiplier,
    val resistanceFactor: Float = resistance.factor,
    val resistanceExponent: Float = resistance.exponent,
    val maxOverscrollPx: Float = 220f,
    val springStiffness: Float = returnSpring.stiffness,
    val springDampingRatio: Float = returnSpring.dampingRatio
) {
    init {
        require(flingVelocityMultiplier > 0f) { "flingVelocityMultiplier must be > 0" }
        require(minimumFlingVelocity >= 0f) { "minimumFlingVelocity must be >= 0" }
        require(maxOverscrollPx > 0f) { "maxOverscrollPx must be > 0" }
        require(springStiffness > 0f) { "springStiffness must be > 0" }
        require(springDampingRatio >= 0f) { "springDampingRatio must be >= 0" }
    }
}
