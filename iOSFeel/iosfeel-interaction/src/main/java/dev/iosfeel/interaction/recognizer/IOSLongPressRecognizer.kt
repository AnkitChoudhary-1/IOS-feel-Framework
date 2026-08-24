package dev.iosfeel.interaction.recognizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.interaction.IOSInteractionDefaults
import dev.iosfeel.interaction.gesture.IOSGestureCompatibility
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureRecognizer
import dev.iosfeel.interaction.gesture.IOSGestureRelease
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.pointer.IOSPointerState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import java.util.UUID

/**
 * Recognizer detecting deterministic long-press hold interactions.
 * Supports time source injection for reliable, zero-sleep unit testing.
 */
@ExperimentalIOSFeelV2Api
class IOSLongPressRecognizer(
    override val id: Any = "LongPressRecognizer_${UUID.randomUUID()}",
    val durationMillis: Long = IOSInteractionDefaults.LongPressDurationMillis,
    val movementTolerancePx: Float = 16f,
    val onHoldActivated: ((Offset) -> Unit)? = null
) : IOSGestureRecognizer {

    override var state: IOSGestureState = IOSGestureState.Possible
        private set

    override val priority: IOSGesturePriority = IOSGesturePriority.High
    override val compatibility: IOSGestureCompatibility = IOSGestureCompatibility.Exclusive

    private var downTimeMillis: Long = 0L
    private var holdTriggered: Boolean = false

    override fun onPointerDown(position: Offset, uptimeMillis: Long) {
        state = IOSGestureState.Possible
        downTimeMillis = uptimeMillis
        holdTriggered = false
    }

    override fun onPointerMove(position: Offset, delta: Offset, uptimeMillis: Long, pointerState: IOSPointerState) {
        if (state == IOSGestureState.Possible) {
            if (pointerState.totalDistance > movementTolerancePx) {
                state = IOSGestureState.Rejected
                return
            }
            val elapsed = uptimeMillis - downTimeMillis
            if (elapsed >= durationMillis && !holdTriggered) {
                state = IOSGestureState.Accepted
                holdTriggered = true
                onHoldActivated?.invoke(position)
            }
        }
    }

    /**
     * Can be called by a timer or clock check to trigger hold without explicit pointer movement.
     */
    fun checkHold(currentTimeMillis: Long, currentPosition: Offset) {
        if (state == IOSGestureState.Possible && !holdTriggered) {
            val elapsed = currentTimeMillis - downTimeMillis
            if (elapsed >= durationMillis) {
                state = IOSGestureState.Accepted
                holdTriggered = true
                onHoldActivated?.invoke(currentPosition)
            }
        }
    }

    override fun onPointerUp(uptimeMillis: Long, pointerState: IOSPointerState): IOSGestureRelease {
        val wasAccepted = state == IOSGestureState.Accepted
        state = if (wasAccepted) IOSGestureState.Ended else IOSGestureState.Rejected
        return IOSGestureRelease(
            position = pointerState.currentPosition,
            translation = pointerState.translation,
            velocity = pointerState.velocity,
            cancelled = !wasAccepted
        )
    }

    override fun onCancel() {
        state = IOSGestureState.Cancelled
        holdTriggered = false
    }
}
