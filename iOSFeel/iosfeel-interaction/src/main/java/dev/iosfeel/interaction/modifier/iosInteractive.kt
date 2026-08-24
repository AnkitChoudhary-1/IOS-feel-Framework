package dev.iosfeel.interaction.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import dev.iosfeel.interaction.IOSInteractionPhase
import dev.iosfeel.interaction.IOSInteractionState
import dev.iosfeel.interaction.arena.IOSGestureArena
import dev.iosfeel.interaction.gesture.IOSGestureRecognizer
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.CancellationException

/**
 * Attaches an [IOSGestureArena] with a set of [recognizers] to a Compose element.
 */
@ExperimentalIOSFeelV2Api
fun Modifier.iosInteractive(
    arena: IOSGestureArena,
    recognizers: List<IOSGestureRecognizer>,
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this

    return this.pointerInput(arena, recognizers, enabled) {
        recognizers.forEach { arena.register(it) }

        awaitEachGesture {
            try {
                val down = awaitFirstDown(requireUnconsumed = false)
                arena.onPointerDown(down.position, down.uptimeMillis)

                var isPointerDown = true
                while (isPointerDown) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }

                    if (change == null || change.isConsumed) {
                        break
                    }

                    if (change.pressed) {
                        arena.onPointerMove(change.position, change.uptimeMillis)
                    } else {
                        arena.onPointerUp(change.uptimeMillis)
                        isPointerDown = false
                    }
                }
            } catch (e: CancellationException) {
                arena.cancelAll()
                throw e
            } finally {
                arena.cancelAll()
            }
        }
    }
}

/**
 * Attaches a lightweight press & tap interaction handler driving an [IOSInteractionState].
 */
@ExperimentalIOSFeelV2Api
fun Modifier.iosPress(
    state: IOSInteractionState,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier {
    if (!enabled) return this

    return this.pointerInput(state, enabled) {
        awaitEachGesture {
            try {
                val down = awaitFirstDown(requireUnconsumed = false)
                state.update(
                    phase = IOSInteractionPhase.Pressed,
                    pointerPosition = down.position,
                    translation = androidx.compose.ui.geometry.Offset.Zero
                )

                var pointerPressed = true
                var cancelled = false

                while (pointerPressed) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }

                    if (change == null) break

                    if (change.pressed) {
                        val trans = change.position - down.position
                        state.update(
                            pointerPosition = change.position,
                            translation = trans
                        )
                        if (trans.getDistance() > 24f) {
                            cancelled = true
                            state.update(phase = IOSInteractionPhase.Cancelled)
                        }
                    } else {
                        pointerPressed = false
                        if (!cancelled && change.position.x in 0f..size.width.toFloat() && change.position.y in 0f..size.height.toFloat()) {
                            state.update(phase = IOSInteractionPhase.Idle)
                            onClick?.invoke()
                        } else {
                            state.update(phase = IOSInteractionPhase.Idle)
                        }
                    }
                }
            } catch (e: CancellationException) {
                state.update(phase = IOSInteractionPhase.Cancelled)
                throw e
            } finally {
                state.reset()
            }
        }
    }
}
