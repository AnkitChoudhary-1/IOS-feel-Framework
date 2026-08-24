package dev.iosfeel.interaction.recognizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.LayoutDirection
import dev.iosfeel.interaction.IOSInteractionDefaults
import dev.iosfeel.interaction.gesture.IOSGestureCompatibility
import dev.iosfeel.interaction.gesture.IOSGestureContext
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureRecognizer
import dev.iosfeel.interaction.gesture.IOSGestureRelease
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.pointer.IOSPointerState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import java.util.UUID

/**
 * Composite recognizer for list / queue reordering:
 * Requires user to hold on item before vertical movement engages reordering.
 * Once hold activates, reorder steals ownership with [IOSGesturePriority.High] from list scrolling.
 */
@ExperimentalIOSFeelV2Api
class IOSReorderRecognizer(
    override val id: Any = "ReorderRecognizer_${UUID.randomUUID()}",
    val holdDurationMillis: Long = IOSInteractionDefaults.ReorderHoldDurationMillis,
    val movementTolerancePx: Float = 16f,
    override val priority: IOSGesturePriority = IOSGesturePriority.High,
    val onReorderStart: ((Offset) -> Unit)? = null,
    val onReorderDrag: ((Offset, Offset) -> Unit)? = null,
    val onReorderEnd: ((IOSGestureRelease) -> Unit)? = null
) : IOSGestureRecognizer {

    override var state: IOSGestureState = IOSGestureState.Possible
        private set

    override val compatibility: IOSGestureCompatibility = IOSGestureCompatibility.Exclusive

    private var downTimeMillis: Long = 0L
    var isHoldActivated: Boolean = false
        private set

    override fun onPointerDown(position: Offset, uptimeMillis: Long) {
        state = IOSGestureState.Possible
        downTimeMillis = uptimeMillis
        isHoldActivated = false
    }

    override fun onPointerMove(position: Offset, delta: Offset, uptimeMillis: Long, pointerState: IOSPointerState) {
        if (!isHoldActivated) {
            if (pointerState.totalDistance > movementTolerancePx) {
                state = IOSGestureState.Rejected
                return
            }
            val elapsed = uptimeMillis - downTimeMillis
            if (elapsed >= holdDurationMillis) {
                isHoldActivated = true
                state = IOSGestureState.Accepted
                onReorderStart?.invoke(position)
            }
        } else if (state == IOSGestureState.Accepted) {
            onReorderDrag?.invoke(pointerState.translation, delta)
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
            onReorderEnd?.invoke(release)
        }
        isHoldActivated = false
        return release
    }

    override fun onCancel() {
        state = IOSGestureState.Cancelled
        isHoldActivated = false
    }
}

/**
 * Recognizer for interactive back edge-swipe navigation transitions.
 * Constrained to leading edge initiation with LTR and RTL support.
 */
@ExperimentalIOSFeelV2Api
class IOSEdgeSwipeRecognizer(
    override val id: Any = "EdgeSwipeRecognizer_${UUID.randomUUID()}",
    val edgeWidthPx: Float = 48f,
    val layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    val containerWidthPx: Float = Float.POSITIVE_INFINITY,
    val slopPx: Float = IOSInteractionDefaults.TouchSlopPx,
    val directionBias: Float = IOSInteractionDefaults.DirectionBias,
    override val priority: IOSGesturePriority = IOSGesturePriority.High,
    val onSwipeStart: ((Offset) -> Unit)? = null,
    val onSwipeProgress: ((Float, Offset) -> Unit)? = null,
    val onSwipeEnd: ((IOSGestureRelease) -> Unit)? = null
) : IOSGestureRecognizer {

    override var state: IOSGestureState = IOSGestureState.Possible
        private set

    override val compatibility: IOSGestureCompatibility = IOSGestureCompatibility.Exclusive

    override fun canAccept(context: IOSGestureContext): Boolean {
        val downX = context.pointer.downPosition.x
        val isWithinEdge = if (layoutDirection == LayoutDirection.Ltr) {
            downX <= edgeWidthPx
        } else {
            downX >= (containerWidthPx - edgeWidthPx)
        }
        return isWithinEdge
    }

    override fun onPointerDown(position: Offset, uptimeMillis: Long) {
        val isWithinEdge = if (layoutDirection == LayoutDirection.Ltr) {
            position.x <= edgeWidthPx
        } else {
            position.x >= (containerWidthPx - edgeWidthPx)
        }
        state = if (isWithinEdge) IOSGestureState.Possible else IOSGestureState.Rejected
    }

    override fun onPointerMove(position: Offset, delta: Offset, uptimeMillis: Long, pointerState: IOSPointerState) {
        if (state == IOSGestureState.Possible) {
            val tx = pointerState.translation.x
            val ty = pointerState.translation.y
            val absX = kotlin.math.abs(tx)
            val absY = kotlin.math.abs(ty)

            if (absX > slopPx) {
                val isForward = if (layoutDirection == LayoutDirection.Ltr) tx > 0 else tx < 0
                if (isForward && absX >= absY * directionBias) {
                    state = IOSGestureState.Accepted
                    onSwipeStart?.invoke(position)
                    val progress = (absX / containerWidthPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
                    onSwipeProgress?.invoke(progress, pointerState.translation)
                } else {
                    state = IOSGestureState.Rejected
                }
            }
        } else if (state == IOSGestureState.Accepted) {
            val absX = kotlin.math.abs(pointerState.translation.x)
            val progress = (absX / containerWidthPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
            onSwipeProgress?.invoke(progress, pointerState.translation)
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
            onSwipeEnd?.invoke(release)
        }
        return release
    }

    override fun onCancel() {
        state = IOSGestureState.Cancelled
    }
}
