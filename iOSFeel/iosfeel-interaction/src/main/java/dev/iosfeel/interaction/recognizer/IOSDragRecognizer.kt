package dev.iosfeel.interaction.recognizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.interaction.IOSInteractionDefaults
import dev.iosfeel.interaction.gesture.IOSDirectionConfidence
import dev.iosfeel.interaction.gesture.IOSGestureCompatibility
import dev.iosfeel.interaction.gesture.IOSGestureContext
import dev.iosfeel.interaction.gesture.IOSGestureDirection
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureRecognizer
import dev.iosfeel.interaction.gesture.IOSGestureRelease
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.pointer.IOSPointerState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import java.util.UUID
import kotlin.math.abs

/**
 * Universal drag recognizer supporting directional axis locking (Horizontal / Vertical / Any).
 */
@ExperimentalIOSFeelV2Api
class IOSDragRecognizer(
    override val id: Any = "DragRecognizer_${UUID.randomUUID()}",
    val direction: IOSGestureDirection = IOSGestureDirection.Any,
    val lockDirection: Boolean = true,
    val directionBias: Float = IOSInteractionDefaults.DirectionBias,
    val slopPx: Float = IOSInteractionDefaults.TouchSlopPx,
    override val priority: IOSGesturePriority = IOSGesturePriority.Normal,
    override val compatibility: IOSGestureCompatibility = IOSGestureCompatibility.Exclusive,
    val onDragStart: ((Offset) -> Unit)? = null,
    val onDrag: ((Offset, Offset) -> Unit)? = null,
    val onDragEnd: ((IOSGestureRelease) -> Unit)? = null
) : IOSGestureRecognizer {

    override var state: IOSGestureState = IOSGestureState.Possible
        private set

    override fun canAccept(context: IOSGestureContext): Boolean {
        if (state == IOSGestureState.Rejected || state == IOSGestureState.Cancelled) return false
        if (direction == IOSGestureDirection.Any) return true

        val translation = context.pointer.translation
        val absX = abs(translation.x)
        val absY = abs(translation.y)

        return when (direction) {
            IOSGestureDirection.Horizontal -> {
                if (absX > slopPx) absX >= absY * directionBias else true
            }
            IOSGestureDirection.Vertical -> {
                if (absY > slopPx) absY >= absX * directionBias else true
            }
            IOSGestureDirection.Any -> true
        }
    }

    override fun onPointerDown(position: Offset, uptimeMillis: Long) {
        state = IOSGestureState.Possible
    }

    override fun onPointerMove(position: Offset, delta: Offset, uptimeMillis: Long, pointerState: IOSPointerState) {
        val translation = pointerState.translation
        val absX = abs(translation.x)
        val absY = abs(translation.y)

        if (state == IOSGestureState.Possible) {
            when (direction) {
                IOSGestureDirection.Horizontal -> {
                    if (absX > slopPx && absX >= absY * directionBias) {
                        state = IOSGestureState.Accepted
                        onDragStart?.invoke(position)
                        onDrag?.invoke(translation, delta)
                    } else if (absY > slopPx && absY >= absX * directionBias) {
                        state = IOSGestureState.Rejected
                    }
                }
                IOSGestureDirection.Vertical -> {
                    if (absY > slopPx && absY >= absX * directionBias) {
                        state = IOSGestureState.Accepted
                        onDragStart?.invoke(position)
                        onDrag?.invoke(translation, delta)
                    } else if (absX > slopPx && absX >= absY * directionBias) {
                        state = IOSGestureState.Rejected
                    }
                }
                IOSGestureDirection.Any -> {
                    if (pointerState.totalDistance > slopPx) {
                        state = IOSGestureState.Accepted
                        onDragStart?.invoke(position)
                        onDrag?.invoke(translation, delta)
                    }
                }
            }
        } else if (state == IOSGestureState.Accepted) {
            onDrag?.invoke(translation, delta)
        }
    }

    override fun onPointerUp(uptimeMillis: Long, pointerState: IOSPointerState): IOSGestureRelease {
        val wasAccepted = state == IOSGestureState.Accepted
        state = if (wasAccepted) IOSGestureState.Ended else IOSGestureState.Rejected
        val release = IOSGestureRelease(
            position = pointerState.currentPosition,
            translation = pointerState.translation,
            velocity = pointerState.velocity,
            cancelled = !wasAccepted
        )
        if (wasAccepted) {
            onDragEnd?.invoke(release)
        }
        return release
    }

    override fun onCancel() {
        state = IOSGestureState.Cancelled
    }
}
