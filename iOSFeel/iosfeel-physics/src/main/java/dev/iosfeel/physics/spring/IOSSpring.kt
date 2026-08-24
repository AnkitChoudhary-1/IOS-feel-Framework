package dev.iosfeel.physics.spring

import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.withFrameNanos
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.IOSPhysicsPhase
import dev.iosfeel.physics.IOSPhysicsState
import dev.iosfeel.physics.debug.IOSPhysicsEnvironment
import dev.iosfeel.physics.interruption.IOSMotionOwner
import kotlin.math.abs

/**
 * Runs a spring simulation to drive [state] from its current value to [target].
 *
 * Supports velocity continuation, interruption cancellation, time scaling,
 * and automatic settling detection.
 */
@ExperimentalIOSFeelV2Api
suspend fun animateSpring(
    state: IOSPhysicsState,
    target: Float,
    initialVelocity: Float = state.velocity,
    spec: IOSSpringSpec = IOSSprings.Navigation,
    environment: IOSPhysicsEnvironment = IOSPhysicsEnvironment()
) {
    val activeSpec = if (environment.reduceMotion) spec.toReducedMotion() else spec
    val composeSpec = activeSpec.toComposeSpringSpec(visibilityThreshold = 0.0005f)

    val startValue = state.value
    val startVelocity = initialVelocity

    state.update(
        value = startValue,
        velocity = startVelocity,
        target = target,
        phase = IOSPhysicsPhase.Springing,
        owner = IOSMotionOwner.Spring
    )

    // If already at target with virtually zero velocity, settle immediately
    if (abs(startValue - target) < 0.0001f && abs(startVelocity) < 0.001f) {
        state.update(
            value = target,
            velocity = 0f,
            target = target,
            phase = IOSPhysicsPhase.Idle,
            owner = IOSMotionOwner.None
        )
        return
    }

    val animation = TargetBasedAnimation(
        animationSpec = composeSpec,
        typeConverter = Float.VectorConverter,
        initialValue = startValue,
        targetValue = target,
        initialVelocity = startVelocity
    )

    val startTime = withFrameNanos { it }
    var isFinished = false

    while (!isFinished) {
        withFrameNanos { frameTimeNanos ->
            val elapsedNanos = frameTimeNanos - startTime
            // Apply environment time scaling
            val scaledPlayTimeNanos = (elapsedNanos / environment.animationScale).toLong()

            val currentValue = animation.getValueFromNanos(scaledPlayTimeNanos)
            val currentVelocity = animation.getVelocityVectorFromNanos(scaledPlayTimeNanos).value

            val isSettled = animation.isFinishedFromNanos(scaledPlayTimeNanos) || 
                (abs(currentValue - target) <= 0.0005f && abs(currentVelocity) <= 0.01f)

            if (isSettled) {
                state.update(
                    value = target,
                    velocity = 0f,
                    target = target,
                    phase = IOSPhysicsPhase.Idle,
                    owner = IOSMotionOwner.None
                )
                isFinished = true
            } else {
                val phase = if (abs(currentValue - target) <= 0.02f) {
                    IOSPhysicsPhase.Settling
                } else {
                    IOSPhysicsPhase.Springing
                }
                state.update(
                    value = currentValue,
                    velocity = currentVelocity,
                    target = target,
                    phase = phase,
                    owner = IOSMotionOwner.Spring
                )
            }
        }
    }
}
