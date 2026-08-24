package dev.iosfeel.physics.debug

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.IOSPhysicsPhase
import dev.iosfeel.physics.interruption.IOSMotionOwner

/**
 * Immutable snapshot of an [dev.iosfeel.physics.IOSPhysicsState] at a single point in time.
 * Ideal for debugging, telemetry, and live Developer Lab inspection.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSPhysicsSnapshot(
    val value: Float,
    val velocity: Float,
    val target: Float,
    val phase: IOSPhysicsPhase,
    val owner: IOSMotionOwner,
    val timestampNs: Long = System.nanoTime()
)

/**
 * Global or localized physics environment configuration.
 *
 * @property animationScale Time multiplier for slow-motion playback (e.g. 0.25x, 0.5x, 1.0x).
 * @property reduceMotion When true, physics primitives adopt restrained, minimum-movement motion specs.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSPhysicsEnvironment(
    val animationScale: Float = 1f,
    val reduceMotion: Boolean = false
) {
    init {
        require(animationScale > 0f) { "animationScale must be strictly positive" }
    }
}

/**
 * CompositionLocal providing the current [IOSPhysicsEnvironment].
 */
@ExperimentalIOSFeelV2Api
val LocalIOSPhysicsEnvironment = compositionLocalOf { IOSPhysicsEnvironment() }
