package dev.iosfeel.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlin.math.abs
import kotlin.math.sqrt

fun Modifier.iosGesture(
    state: IOSGestureState,
    config: IOSGestureConfig = IOSGestureConfig(),
    onStarted: (() -> Unit)? = null,
    onChanged: ((IOSGestureState) -> Unit)? = null,
    onEnded: ((IOSGestureState) -> Unit)? = null,
    onCancelled: (() -> Unit)? = null
): Modifier {

    if (!config.enabled) {
        return this
    }

    return pointerInput(
        state,
        config
    ) {

        awaitEachGesture {

            state.reset()

            val velocityTracker = VelocityTracker()

            val down = awaitFirstDown(
                requireUnconsumed = false
            )

            val maxStartX = config.requiredStartMaxX
            if (maxStartX != null && down.position.x > maxStartX) {
                state.phase = IOSGesturePhase.Cancelled
                return@awaitEachGesture
            }

            state.phase = IOSGesturePhase.Possible

            velocityTracker.addPosition(
                down.uptimeMillis,
                down.position
            )

            val start = down.position
            val pointerId = down.id

            var gestureAccepted = false
            var endedNormally = false

            while (true) {

                val event = awaitPointerEvent()

                val change =
                    event.changes
                        .firstOrNull {
                            it.id == pointerId
                        }
                        ?: break

                velocityTracker.addPosition(
                    change.uptimeMillis,
                    change.position
                )

                val totalX = change.position.x - start.x
                val totalY = change.position.y - start.y

                if (!gestureAccepted) {

                    val movedEnough =
                        abs(totalX) >= config.activationSlopPx ||
                        abs(totalY) >= config.activationSlopPx

                    if (movedEnough) {

                        gestureAccepted =
                            shouldAcceptGesture(
                                dx = totalX,
                                dy = totalY,
                                config = config
                            )

                        if (!gestureAccepted) {
                            state.phase = IOSGesturePhase.Cancelled
                            onCancelled?.invoke()
                            break
                        }

                        state.phase = IOSGesturePhase.Began
                        onStarted?.invoke()
                    }
                }

                if (gestureAccepted) {

                    val velocity = velocityTracker.calculateVelocity()

                    state.translationX = totalX
                    state.translationY = totalY
                    state.velocityX = velocity.x
                    state.velocityY = velocity.y

                    val progressSource = when (config.direction) {
                        IOSGestureDirection.Horizontal -> totalX
                        IOSGestureDirection.Vertical -> totalY
                        IOSGestureDirection.Any -> sqrt(totalX * totalX + totalY * totalY)
                    }

                    state.progress = calculateGestureProgress(
                        translation = progressSource,
                        distance = config.progressDistancePx
                    )

                    state.phase = IOSGesturePhase.Changed
                    change.consume()
                    onChanged?.invoke(state)
                }

                if (change.changedToUp()) {

                    endedNormally = true

                    if (gestureAccepted) {

                        val finalVelocity = velocityTracker.calculateVelocity()

                        state.velocityX = finalVelocity.x
                        state.velocityY = finalVelocity.y
                        state.phase = IOSGesturePhase.Ended

                        onEnded?.invoke(state)
                    }

                    break
                }

                if (!change.pressed) {
                    break
                }
            }

            if (!endedNormally && gestureAccepted) {
                state.phase = IOSGesturePhase.Cancelled
                onCancelled?.invoke()
            }
        }
    }
}
