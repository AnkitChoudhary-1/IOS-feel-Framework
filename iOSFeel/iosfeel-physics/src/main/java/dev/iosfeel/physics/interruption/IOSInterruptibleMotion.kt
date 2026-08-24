package dev.iosfeel.physics.interruption

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.IOSPhysicsPhase
import dev.iosfeel.physics.IOSPhysicsState
import dev.iosfeel.physics.bounds.IOSPhysicsBounds
import dev.iosfeel.physics.debug.IOSPhysicsEnvironment
import dev.iosfeel.physics.decay.IOSDecayResult
import dev.iosfeel.physics.decay.IOSDecaySpec
import dev.iosfeel.physics.decay.animateDecay
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings
import dev.iosfeel.physics.spring.animateSpring
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Universal physical controller enabling seamless gesture interruption and re-grabbing.
 *
 * Implements the core physical rule of iOSFeel V2:
 * When a user touches an object in flight, ownership is instantly reclaimed by the User
 * without snapping back or resetting velocity, and the active spring/decay halts immediately.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSInterruptibleMotion(
    initialValue: Float = 0f
) {
    /**
     * Observable physical state of this motion.
     */
    val state: IOSPhysicsState = IOSPhysicsState(initialValue)

    /**
     * Active animation job if a spring or decay is running.
     */
    private var activeJob: Job? = null

    /**
     * Current position value.
     */
    val value: Float
        get() = state.value

    /**
     * Current instantaneous velocity.
     */
    val velocity: Float
        get() = state.velocity

    /**
     * Target destination value.
     */
    val target: Float
        get() = state.target

    /**
     * Current phase of motion.
     */
    val phase: IOSPhysicsPhase
        get() = state.phase

    /**
     * Current owner of motion.
     */
    val owner: IOSMotionOwner
        get() = state.owner

    /**
     * Immediately reclaims motion ownership for the user.
     *
     * Cancels any active spring or decay animation, captures the instantaneous value and velocity,
     * and guarantees **zero positional jump**.
     */
    fun acquireByUser() {
        activeJob?.cancel(CancellationException("Interrupted by user touch"))
        activeJob = null

        state.update(
            value = state.value,
            velocity = state.velocity,
            target = state.value,
            phase = IOSPhysicsPhase.UserDriven,
            owner = IOSMotionOwner.User
        )
    }

    /**
     * Direct user gesture update.
     */
    fun dragTo(value: Float, velocity: Float) {
        state.update(
            value = value,
            velocity = velocity,
            target = value,
            phase = IOSPhysicsPhase.UserDriven,
            owner = IOSMotionOwner.User
        )
    }

    /**
     * Releases ownership to an active spring simulation toward [target].
     */
    suspend fun releaseToSpring(
        target: Float,
        initialVelocity: Float = state.velocity,
        spec: IOSSpringSpec = IOSSprings.Navigation,
        environment: IOSPhysicsEnvironment = IOSPhysicsEnvironment()
    ) {
        activeJob?.cancel()
        coroutineScope {
            val job = launch {
                animateSpring(
                    state = state,
                    target = target,
                    initialVelocity = initialVelocity,
                    spec = spec,
                    environment = environment
                )
            }
            activeJob = job
            job.join()
        }
    }

    /**
     * Releases ownership to an inertial decay simulation.
     */
    suspend fun releaseToDecay(
        velocity: Float = state.velocity,
        spec: IOSDecaySpec = IOSDecaySpec(),
        bounds: IOSPhysicsBounds? = null,
        environment: IOSPhysicsEnvironment = IOSPhysicsEnvironment()
    ): IOSDecayResult {
        activeJob?.cancel()
        var result = IOSDecayResult(0f, 0f, false)
        coroutineScope {
            val job = launch {
                result = animateDecay(
                    state = state,
                    initialVelocity = velocity,
                    spec = spec,
                    bounds = bounds,
                    environment = environment
                )
            }
            activeJob = job
            job.join()
        }
        return result
    }

    /**
     * Cancels any running motion and returns to Idle.
     */
    fun cancel() {
        activeJob?.cancel()
        activeJob = null
        state.reset()
    }

    /**
     * Immediately snaps the state to a specific value.
     */
    fun snapTo(value: Float) {
        activeJob?.cancel()
        activeJob = null
        state.snapTo(value)
    }
}

/**
 * Creates and remembers an [IOSInterruptibleMotion] instance.
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSInterruptibleMotion(
    initialValue: Float = 0f
): IOSInterruptibleMotion {
    return remember { IOSInterruptibleMotion(initialValue) }
}
