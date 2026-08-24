package dev.iosfeel.physics

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.iosfeel.physics.debug.IOSPhysicsSnapshot
import dev.iosfeel.physics.interruption.IOSMotionOwner

/**
 * Universal physical state representation in iOSFeel V2.
 *
 * Tracks value, instantaneous velocity, target destination, active phase,
 * and current ownership.
 *
 * All state mutations are protected against `NaN` and `Infinity`.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSPhysicsState(
    initialValue: Float = 0f
) {
    /**
     * Current position or normalized progress.
     */
    var value: Float by mutableFloatStateOf(sanitizeFloat(initialValue, 0f))
        internal set

    /**
     * Instantaneous velocity in units/second (or px/second).
     */
    var velocity: Float by mutableFloatStateOf(0f)
        internal set

    /**
     * The intended target destination value.
     */
    var target: Float by mutableFloatStateOf(sanitizeFloat(initialValue, 0f))
        internal set

    /**
     * Active interaction or simulation phase.
     */
    var phase: IOSPhysicsPhase by mutableStateOf(IOSPhysicsPhase.Idle)
        internal set

    /**
     * Current owner of the motion.
     */
    var owner: IOSMotionOwner by mutableStateOf(IOSMotionOwner.None)
        internal set

    /**
     * Checks if the physical state is currently at rest.
     */
    val isIdle: Boolean
        get() = phase == IOSPhysicsPhase.Idle

    /**
     * Updates all state fields atomically with numerical safety.
     */
    fun update(
        value: Float,
        velocity: Float,
        target: Float = this.target,
        phase: IOSPhysicsPhase = this.phase,
        owner: IOSMotionOwner = this.owner
    ) {
        this.value = sanitizeFloat(value, this.value)
        this.velocity = sanitizeFloat(velocity, 0f)
        this.target = sanitizeFloat(target, this.target)
        this.phase = phase
        this.owner = owner
    }

    /**
     * Immediately snaps the state to a new value, zeroing velocity and returning to Idle.
     */
    fun snapTo(value: Float) {
        val clean = sanitizeFloat(value, 0f)
        this.value = clean
        this.target = clean
        this.velocity = 0f
        this.phase = IOSPhysicsPhase.Idle
        this.owner = IOSMotionOwner.None
    }

    /**
     * Resets the physical state to Idle while preserving current value.
     */
    fun reset() {
        this.target = this.value
        this.velocity = 0f
        this.phase = IOSPhysicsPhase.Idle
        this.owner = IOSMotionOwner.None
    }

    /**
     * Captures an immutable snapshot of this state.
     */
    fun snapshot(): IOSPhysicsSnapshot = IOSPhysicsSnapshot(
        value = value,
        velocity = velocity,
        target = target,
        phase = phase,
        owner = owner
    )

    companion object {
        internal fun sanitizeFloat(input: Float, fallback: Float): Float {
            return if (input.isNaN() || input.isInfinite()) fallback else input
        }
    }
}
