package dev.iosfeel.motion

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun Modifier.iosMotionDrag(
    state: IOSMotionState,
    config: IOSMotionDragConfig = IOSMotionDragConfig()
): Modifier {

    if (!config.enabled) {
        return this
    }

    return pointerInput(
        state,
        config
    ) {

        coroutineScope {

            awaitEachGesture {

                val tracker = VelocityTracker()

                val down = awaitFirstDown(
                    requireUnconsumed = false
                )

                tracker.addPosition(
                    down.uptimeMillis,
                    down.position
                )

                /*
                 * Important:
                 * beginDrag cancels any existing spring.
                 */
                config.onDragStarted?.invoke()
                launch {
                    state.beginDrag()
                }

                val pointerId = down.id

                var releasedNormally = false

                while (true) {

                    val event = awaitPointerEvent()

                    val change =
                        event.changes
                            .firstOrNull {
                                it.id == pointerId
                            }
                            ?: break

                    tracker.addPosition(
                        change.uptimeMillis,
                        change.position
                    )

                    val delta =
                        change
                            .positionChange()
                            .x

                    if (delta != 0f) {

                        change.consume()

                        val currentVelocity =
                            tracker
                                .calculateVelocity()
                                .x

                        launch {
                            state.dragBy(
                                delta = delta,
                                gestureVelocity = currentVelocity,
                                bounds = config.bounds
                            )
                        }
                    }

                    if (change.changedToUp()) {

                        releasedNormally = true

                        val releaseVelocity =
                            tracker
                                .calculateVelocity()
                                .x

                        config.onReleased?.invoke(releaseVelocity)

                        launch {

                            state.springTo(
                                targetPosition = config.targetPosition,
                                initialVelocity = releaseVelocity,
                                spec = config.springSpec
                            )
                        }

                        break
                    }

                    if (!change.pressed) {
                        break
                    }
                }

                if (!releasedNormally) {

                    launch {

                        state.springTo(
                            targetPosition = config.targetPosition,
                            initialVelocity = state.velocity,
                            spec = config.springSpec
                        )
                    }
                }
            }
        }
    }
}
