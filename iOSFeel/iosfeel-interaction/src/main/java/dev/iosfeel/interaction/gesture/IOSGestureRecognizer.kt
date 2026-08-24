package dev.iosfeel.interaction.gesture

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.interaction.pointer.IOSPointerState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Context passed to [IOSGestureRecognizer] to evaluate dynamic eligibility.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSGestureContext(
    val pointer: IOSPointerState,
    val direction: IOSGestureDirection? = null,
    val elapsedMillis: Long = 0L,
    val directionConfidence: Float = 0.5f
)

/**
 * Complete release event data returned when user finishes a gesture.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSGestureRelease(
    val position: Offset,
    val translation: Offset,
    val velocity: Velocity,
    val cancelled: Boolean = false
)

/**
 * Base contract for all iOSFeel V2 gesture recognizers.
 */
@ExperimentalIOSFeelV2Api
interface IOSGestureRecognizer {

    /**
     * Unique identifier for this recognizer instance.
     */
    val id: Any

    /**
     * Current lifecycle state of the recognizer.
     */
    val state: IOSGestureState

    /**
     * Priority level of this recognizer in the arena.
     */
    val priority: IOSGesturePriority
        get() = IOSGesturePriority.Normal

    /**
     * Compatibility mode in the arena.
     */
    val compatibility: IOSGestureCompatibility
        get() = IOSGestureCompatibility.Exclusive

    /**
     * Returns true if this recognizer is currently eligible to accept the gesture given [context].
     */
    fun canAccept(context: IOSGestureContext): Boolean = true

    /**
     * Called when a pointer down event occurs.
     */
    fun onPointerDown(position: Offset, uptimeMillis: Long)

    /**
     * Called when pointer moves.
     */
    fun onPointerMove(position: Offset, delta: Offset, uptimeMillis: Long, pointerState: IOSPointerState)

    /**
     * Called when pointer is lifted.
     */
    fun onPointerUp(uptimeMillis: Long, pointerState: IOSPointerState): IOSGestureRelease

    /**
     * Called when this recognizer is rejected or cancelled.
     */
    fun onCancel()
}
