package dev.iosfeel.interaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Universal observable interaction state for iOSFeel V2 components.
 *
 * Tracks the high-level phase, raw pointer position, total translation,
 * instantaneous velocity, and progress.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSInteractionState {

    /**
     * Active interaction lifecycle phase.
     */
    var phase: IOSInteractionPhase by mutableStateOf(IOSInteractionPhase.Idle)
        internal set

    /**
     * Current pointer position relative to the component bounds, or [Offset.Unspecified] if idle.
     */
    var pointerPosition: Offset by mutableStateOf(Offset.Unspecified)
        internal set

    /**
     * Cumulative translation offset since touch down.
     */
    var translation: Offset by mutableStateOf(Offset.Zero)
        internal set

    /**
     * Instantaneous velocity in pixels per second.
     */
    var velocity: Velocity by mutableStateOf(Velocity.Zero)
        internal set

    /**
     * Normalized interaction progress (0f..1f or custom domain range).
     */
    var progress: Float by mutableFloatStateOf(0f)
        internal set

    /**
     * Returns true if user is actively touching, holding, dragging, or scrubbing.
     */
    val isInteracting: Boolean
        get() = phase in listOf(
            IOSInteractionPhase.Possible,
            IOSInteractionPhase.Pressed,
            IOSInteractionPhase.Held,
            IOSInteractionPhase.Dragging,
            IOSInteractionPhase.Scrubbing
        )

    /**
     * Resets the state back to Idle.
     */
    fun reset() {
        phase = IOSInteractionPhase.Idle
        pointerPosition = Offset.Unspecified
        translation = Offset.Zero
        velocity = Velocity.Zero
        progress = 0f
    }

    /**
     * Atomically updates interaction values.
     */
    fun update(
        phase: IOSInteractionPhase = this.phase,
        pointerPosition: Offset = this.pointerPosition,
        translation: Offset = this.translation,
        velocity: Velocity = this.velocity,
        progress: Float = this.progress
    ) {
        this.phase = phase
        this.pointerPosition = pointerPosition
        this.translation = translation
        this.velocity = velocity
        this.progress = progress
    }
}

/**
 * Creates and remembers an [IOSInteractionState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSInteractionState(): IOSInteractionState {
    return remember { IOSInteractionState() }
}
