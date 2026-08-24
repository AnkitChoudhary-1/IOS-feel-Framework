package dev.iosfeel.physics.decay

import androidx.compose.runtime.withFrameNanos
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.IOSPhysicsPhase
import dev.iosfeel.physics.IOSPhysicsState
import dev.iosfeel.physics.bounds.IOSBoundaryBehavior
import dev.iosfeel.physics.bounds.IOSPhysicsBounds
import dev.iosfeel.physics.debug.IOSPhysicsEnvironment
import dev.iosfeel.physics.interruption.IOSMotionOwner
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

/**
 * Runs an inertial decay simulation driving [state] until it stops or hits [bounds].
 *
 * Returns [IOSDecayResult] containing consumed distance and leftover velocity.
 */
@ExperimentalIOSFeelV2Api
suspend fun animateDecay(
    state: IOSPhysicsState,
    initialVelocity: Float = state.velocity,
    spec: IOSDecaySpec = IOSDecaySpec(),
    bounds: IOSPhysicsBounds? = null,
    environment: IOSPhysicsEnvironment = IOSPhysicsEnvironment()
): IOSDecayResult {
    var currentVelocity = initialVelocity * spec.velocityMultiplier
    val startValue = state.value
    var currentValue = startValue

    if (abs(currentVelocity) < spec.minimumVelocity) {
        state.update(
            value = currentValue,
            velocity = 0f,
            target = currentValue,
            phase = IOSPhysicsPhase.Idle,
            owner = IOSMotionOwner.None
        )
        return IOSDecayResult(
            consumedDistance = 0f,
            remainingVelocity = 0f,
            hitBoundary = false
        )
    }

    state.update(
        value = currentValue,
        velocity = currentVelocity,
        phase = IOSPhysicsPhase.Decaying,
        owner = IOSMotionOwner.Decay
    )

    var lastFrameTimeNanos = withFrameNanos { it }
    var hitBoundary = false
    var remainingVelocity = 0f

    // Exponential friction coefficient $\gamma = -\ln(\text{friction}) \times 60$
    val frictionPerSec = -ln(spec.friction.toDouble()).toFloat() * 60f

    while (abs(currentVelocity) >= spec.minimumVelocity && !hitBoundary) {
        withFrameNanos { frameTimeNanos ->
            val dtNanos = frameTimeNanos - lastFrameTimeNanos
            lastFrameTimeNanos = frameTimeNanos

            val dtSeconds = (dtNanos / 1_000_000_000.0f) * environment.animationScale
            if (dtSeconds <= 0f) return@withFrameNanos

            // Kinematic decay step: $v(t + dt) = v(t) \cdot e^{-\gamma \cdot dt}$
            val decayFactor = kotlin.math.exp(-frictionPerSec * dtSeconds)
            val newVelocity = currentVelocity * decayFactor
            val deltaDistance = (currentVelocity + newVelocity) * 0.5f * dtSeconds
            var nextValue = currentValue + deltaDistance

            // Boundary check
            if (bounds != null) {
                if (nextValue < bounds.min) {
                    when (bounds.behavior) {
                        IOSBoundaryBehavior.Clamp -> {
                            nextValue = bounds.min
                            remainingVelocity = currentVelocity
                            hitBoundary = true
                        }
                        IOSBoundaryBehavior.Bounce -> {
                            nextValue = bounds.min
                            currentVelocity = -currentVelocity * 0.4f
                        }
                        IOSBoundaryBehavior.Resist -> {
                            // Let caller handle overscroll resistance
                            remainingVelocity = currentVelocity
                            hitBoundary = true
                        }
                    }
                } else if (nextValue > bounds.max) {
                    when (bounds.behavior) {
                        IOSBoundaryBehavior.Clamp -> {
                            nextValue = bounds.max
                            remainingVelocity = currentVelocity
                            hitBoundary = true
                        }
                        IOSBoundaryBehavior.Bounce -> {
                            nextValue = bounds.max
                            currentVelocity = -currentVelocity * 0.4f
                        }
                        IOSBoundaryBehavior.Resist -> {
                            remainingVelocity = currentVelocity
                            hitBoundary = true
                        }
                    }
                }
            }

            currentValue = nextValue
            if (!hitBoundary) {
                currentVelocity = newVelocity
            }

            state.update(
                value = currentValue,
                velocity = if (hitBoundary) remainingVelocity else currentVelocity,
                target = currentValue,
                phase = if (hitBoundary || abs(currentVelocity) < spec.minimumVelocity) IOSPhysicsPhase.Idle else IOSPhysicsPhase.Decaying,
                owner = if (hitBoundary || abs(currentVelocity) < spec.minimumVelocity) IOSMotionOwner.None else IOSMotionOwner.Decay
            )
        }
    }

    state.update(
        value = currentValue,
        velocity = if (hitBoundary) remainingVelocity else 0f,
        target = currentValue,
        phase = IOSPhysicsPhase.Idle,
        owner = IOSMotionOwner.None
    )

    return IOSDecayResult(
        consumedDistance = currentValue - startValue,
        remainingVelocity = if (hitBoundary) remainingVelocity else 0f,
        hitBoundary = hitBoundary
    )
}
