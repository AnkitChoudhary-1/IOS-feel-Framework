package dev.iosfeel.motion

import androidx.compose.runtime.Immutable

/**
 * Defines physical coupled spring parameters for Newtonian action-reaction
 * interaction pairs (e.g. lifted selector capsule and reacting navbar base).
 *
 * @param primary Spring spec governing the active/grabbed body (e.g. lifted selector).
 * @param reaction Spring spec governing the reactive body (e.g. compressed navbar).
 * @param reactionStrength Multiplier for the reactive displacement/scale.
 */
@Immutable
data class IOSCoupledBounceSpec(
    val primary: IOSSpringSpec = IOSSpringSpec(stiffness = 380f, dampingRatio = 0.72f),
    val reaction: IOSSpringSpec = IOSSpringSpec(stiffness = 420f, dampingRatio = 0.85f),
    val reactionStrength: Float = 0.20f
)
