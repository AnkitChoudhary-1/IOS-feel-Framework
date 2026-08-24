package dev.iosfeel.interaction.arena

import androidx.compose.runtime.Immutable
import dev.iosfeel.interaction.gesture.IOSGestureCompatibility
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureRecognizer
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Registration entry representing a recognizer participating in the arena.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSGestureArenaEntry(
    val id: Any,
    val recognizer: IOSGestureRecognizer,
    val priority: IOSGesturePriority = recognizer.priority,
    val compatibility: IOSGestureCompatibility = recognizer.compatibility
)

/**
 * Result metadata describing the current state of arena arbitration.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSGestureArenaDecision(
    val winner: Any? = null,
    val activeRecognizers: Set<Any> = emptySet(),
    val isDecided: Boolean = winner != null
)
