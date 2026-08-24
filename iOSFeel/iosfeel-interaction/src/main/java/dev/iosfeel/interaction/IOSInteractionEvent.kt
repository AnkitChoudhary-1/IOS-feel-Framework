package dev.iosfeel.interaction

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * High-level semantic interaction events emitted by the iOSFeel V2 gesture engine.
 */
@ExperimentalIOSFeelV2Api
sealed interface IOSInteractionEvent {

    /**
     * Emitted when user initiates touch contact.
     */
    data class PressStarted(val position: Offset) : IOSInteractionEvent

    /**
     * Emitted when user lifts finger after a valid press/tap.
     */
    data object PressEnded : IOSInteractionEvent

    /**
     * Emitted when a long-press hold threshold is reached and accepted.
     */
    data class HoldActivated(val position: Offset) : IOSInteractionEvent

    /**
     * Emitted when scrubbing moves across a discrete detent boundary.
     */
    data class DetentChanged<T>(val detent: T) : IOSInteractionEvent

    /**
     * Emitted when drag motion starts and takes ownership.
     */
    data class DragStarted(val startPosition: Offset) : IOSInteractionEvent

    /**
     * Emitted when user releases an active drag gesture.
     */
    data class DragEnded(
        val finalPosition: Offset,
        val translation: Offset,
        val velocity: Velocity
    ) : IOSInteractionEvent

    /**
     * Emitted when an interaction is cancelled or stolen by a competing recognizer.
     */
    data object Cancelled : IOSInteractionEvent
}
