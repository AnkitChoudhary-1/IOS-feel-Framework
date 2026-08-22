package dev.iosfeel.scroll

import androidx.compose.runtime.Immutable

@Immutable
data class IOSScrollConfig(
    // Fling
    val flingVelocityMultiplier: Float = 1.0f,
    val minimumFlingVelocity: Float = 25f,

    // Elasticity
    val resistanceFactor: Float = 0.55f,
    val resistanceExponent: Float = 0.85f,
    val maxOverscrollPx: Float = 220f,

    // Spring return
    val springStiffness: Float = 300f,
    val springDampingRatio: Float = 0.78f
) {
    init {
        require(flingVelocityMultiplier > 0f) { "flingVelocityMultiplier must be > 0" }
        require(minimumFlingVelocity >= 0f) { "minimumFlingVelocity must be >= 0" }

        require(resistanceFactor > 0f) { "resistanceFactor must be > 0" }
        require(resistanceExponent > 0f) { "resistanceExponent must be > 0" }
        require(maxOverscrollPx > 0f) { "maxOverscrollPx must be > 0" }

        require(springStiffness > 0f) { "springStiffness must be > 0" }
        require(springDampingRatio > 0f) { "springDampingRatio must be > 0" }
    }
}
