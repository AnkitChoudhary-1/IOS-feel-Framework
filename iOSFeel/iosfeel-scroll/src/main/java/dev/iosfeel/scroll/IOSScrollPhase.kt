package dev.iosfeel.scroll

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Universal scroll interaction lifecycle phases in iOSFeel V2.
 */
@ExperimentalIOSFeelV2Api
enum class IOSScrollPhase {
    /**
     * Scroll surface is stationary and at rest.
     */
    Idle,

    /**
     * User's finger is directly dragging and scrolling content.
     */
    Dragging,

    /**
     * Inertial decay is active following user release fling.
     */
    Decaying,

    /**
     * Content has reached boundary and is stretching into elastic overscroll.
     */
    Overscrolling,

    /**
     * Elastic overscroll is returning to zero boundary via spring.
     */
    Returning,

    /**
     * Deprecated alias for [Returning].
     */
    @Deprecated("Use Returning instead", ReplaceWith("Returning"))
    SpringingBack
}

/**
 * Result returned upon fling completion or boundary interception.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSFlingResult(
    val consumedDistance: Float,
    val remainingVelocity: Float
)
