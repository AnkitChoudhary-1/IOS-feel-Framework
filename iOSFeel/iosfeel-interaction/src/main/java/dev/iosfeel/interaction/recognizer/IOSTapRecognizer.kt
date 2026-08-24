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
 * Recognizes quick, discrete taps within touch slop tolerance.
 */
@ExperimentalIOSFeelV2Api
class IOSTapRecognizer(
    override val id: Any = "TapRecognizer_${UUID.randomUUID()}",
    val slopPx: Float = IOSInteractionDefaults.TouchSlopPx,
    val onTap: ((Offset) -> Unit)? = null
) : IOSGestureRecognizer {

    override var state: IOSGestureState = IOSGestureState.Possible
        private set

    override val priority: IOSGesturePriority = IOSGesturePriority.Normal
    override val compatibility: IOSGestureCompatibility = IOSGestureCompatibility.Exclusive

    private var downPos: Offset = Offset.Unspecified

    override fun onPointerDown(position: Offset, uptimeMillis: Long) {
        state = IOSGestureState.Possible
        downPos = position
    }

    override fun onPointerMove(position: Offset, delta: Offset, uptimeMillis: Long, pointerState: IOSPointerState) {
        if (state == IOSGestureState.Possible) {
            if (pointerState.totalDistance > slopPx) {
                state = IOSGestureState.Rejected
            }
        }
    }

    override fun onPointerUp(uptimeMillis: Long, pointerState: IOSPointerState): IOSGestureRelease {
        val wasPossible = state == IOSGestureState.Possible && pointerState.totalDistance <= slopPx
        if (wasPossible) {
            state = IOSGestureState.Ended
            onTap?.invoke(pointerState.currentPosition)
        } else {
            state = IOSGestureState.Rejected
        }
        return IOSGestureRelease(
            position = pointerState.currentPosition,
            translation = pointerState.translation,
            velocity = pointerState.velocity,
            cancelled = !wasPossible
        )
    }

    override fun onCancel() {
        state = IOSGestureState.Cancelled
        downPos = Offset.Unspecified
    }
}

/**
 * Passive recognizer providing immediate visual press compression feedback.
 * Cancels gracefully if movement exceeds cancellation tolerance (e.g. when scrolling starts).
 */
@ExperimentalIOSFeelV2Api
class IOSPressRecognizer(
    override val id: Any = "PressRecognizer_${UUID.randomUUID()}",
    val maxDistancePx: Float = 24f,
    val onPressChange: ((Boolean) -> Unit)? = null
) : IOSGestureRecognizer {

    override var state: IOSGestureState = IOSGestureState.Possible
        private set

    override val priority: IOSGesturePriority = IOSGesturePriority.Low
    override val compatibility: IOSGestureCompatibility = IOSGestureCompatibility.Passive

    override fun onPointerDown(position: Offset, uptimeMillis: Long) {
        state = IOSGestureState.Accepted
        onPressChange?.invoke(true)
    }

    override fun onPointerMove(position: Offset, delta: Offset, uptimeMillis: Long, pointerState: IOSPointerState) {
        if (state == IOSGestureState.Accepted && pointerState.totalDistance > maxDistancePx) {
            state = IOSGestureState.Cancelled
            onPressChange?.invoke(false)
        }
    }

    override fun onPointerUp(uptimeMillis: Long, pointerState: IOSPointerState): IOSGestureRelease {
        val wasActive = state == IOSGestureState.Accepted
        state = IOSGestureState.Ended
        if (wasActive) {
            onPressChange?.invoke(false)
        }
        return IOSGestureRelease(
            position = pointerState.currentPosition,
            translation = pointerState.translation,
            velocity = pointerState.velocity,
            cancelled = !wasActive
        )
    }

    override fun onCancel() {
        if (state == IOSGestureState.Accepted) {
            onPressChange?.invoke(false)
        }
        state = IOSGestureState.Cancelled
    }
}
