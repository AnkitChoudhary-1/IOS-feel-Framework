package dev.iosfeel.physics

import dev.iosfeel.physics.bounds.IOSPhysicsBounds
import dev.iosfeel.physics.bounds.normalizeVelocity
import dev.iosfeel.physics.coupled.IOSCoupledMotionSpec
import dev.iosfeel.physics.coupled.IOSCoupledMotionState
import dev.iosfeel.physics.decay.IOSDecayResult
import dev.iosfeel.physics.decay.IOSDecaySpec
import dev.iosfeel.physics.detent.IOSDetent
import dev.iosfeel.physics.detent.IOSDetentDecision
import dev.iosfeel.physics.detent.IOSDetentResolver
import dev.iosfeel.physics.interruption.IOSInterruptibleMotion
import dev.iosfeel.physics.resistance.IOSResistanceSpec
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Top-level entry point and namespace for the iOSFeel V2 Physical Motion Engine.
 */
@ExperimentalIOSFeelV2Api
object IOSPhysics {

    /**
     * Standard spring presets.
     */
    val Springs = IOSSprings

    /**
     * Standard resistance presets.
     */
    val Resistance = IOSResistanceSpec

    /**
     * Normalizes a pixel-based velocity against a given distance.
     */
    fun normalizeVelocity(velocityPxPerSecond: Float, distancePx: Float): Float =
        dev.iosfeel.physics.bounds.normalizeVelocity(velocityPxPerSecond, distancePx)

    /**
     * Resolves detent destination from position and velocity.
     */
    fun <T> resolveDetent(
        position: Float,
        velocity: Float,
        detents: List<IOSDetent<T>>,
        velocityThreshold: Float = 0.5f
    ): IOSDetentDecision<T> = IOSDetentResolver.resolve(position, velocity, detents, velocityThreshold)
}
