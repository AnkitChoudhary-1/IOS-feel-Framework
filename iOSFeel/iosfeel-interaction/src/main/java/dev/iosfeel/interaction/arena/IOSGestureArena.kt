package dev.iosfeel.interaction.arena

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import dev.iosfeel.interaction.gesture.IOSDirectionConfidence
import dev.iosfeel.interaction.gesture.IOSGestureCompatibility
import dev.iosfeel.interaction.gesture.IOSGestureContext
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureRecognizer
import dev.iosfeel.interaction.gesture.IOSGestureRelease
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.pointer.IOSPointerState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Centralized gesture arbitration arena in iOSFeel V2.
 *
 * Coordinates concurrent gesture recognizers, managing eligibility, direction locking,
 * priority resolution, and exclusive ownership transfer.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSGestureArena {

    private val entries = mutableListOf<IOSGestureArenaEntry>()
    val pointerState = IOSPointerState()

    /**
     * Current winning recognizer ID, or null if undecided or idle.
     */
    var winnerId: Any? by mutableStateOf(null)
        private set

    /**
     * Active candidate recognizers still participating in arbitration.
     */
    val candidateIds: Set<Any>
        get() = entries.filter { it.recognizer.state == IOSGestureState.Possible || it.recognizer.state == IOSGestureState.Accepted }.map { it.id }.toSet()

    /**
     * Registers a candidate recognizer into this arena.
     */
    fun register(recognizer: IOSGestureRecognizer) {
        if (entries.none { it.id == recognizer.id }) {
            entries.add(IOSGestureArenaEntry(id = recognizer.id, recognizer = recognizer))
        }
    }

    /**
     * Unregisters a recognizer from this arena.
     */
    fun unregister(recognizerId: Any) {
        entries.removeAll { it.id == recognizerId }
    }

    /**
     * Clears all registered entries.
     */
    fun clear() {
        entries.clear()
        winnerId = null
        pointerState.reset()
    }

    /**
     * Called when a pointer down event occurs.
     */
    fun onPointerDown(position: Offset, uptimeMillis: Long) {
        winnerId = null
        pointerState.onDown(position, uptimeMillis)

        entries.forEach { entry ->
            entry.recognizer.onPointerDown(position, uptimeMillis)
        }
    }

    /**
     * Called when pointer moves.
     */
    fun onPointerMove(position: Offset, uptimeMillis: Long) {
        pointerState.onMove(position, uptimeMillis)

        val translation = pointerState.translation
        val hConf = IOSDirectionConfidence.horizontalConfidence(translation.x, translation.y)
        val vConf = IOSDirectionConfidence.verticalConfidence(translation.x, translation.y)
        val maxConf = maxOf(hConf, vConf)

        val context = IOSGestureContext(
            pointer = pointerState,
            elapsedMillis = pointerState.elapsedMillis,
            directionConfidence = maxConf
        )

        // Evaluate recognizers
        for (entry in entries.toList()) {
            if (entry.recognizer.state == IOSGestureState.Possible || entry.recognizer.state == IOSGestureState.Accepted) {
                if (!entry.recognizer.canAccept(context) && entry.recognizer.state == IOSGestureState.Possible) {
                    entry.recognizer.onCancel()
                    continue
                }
                entry.recognizer.onPointerMove(position, pointerState.delta, uptimeMillis, pointerState)

                // Check if this recognizer just accepted or has higher priority than current winner
                if (entry.recognizer.state == IOSGestureState.Accepted) {
                    if (entry.compatibility == IOSGestureCompatibility.Exclusive) {
                        val currentWinnerEntry = entries.find { it.id == winnerId }
                        val currentWinnerPriority = currentWinnerEntry?.priority ?: IOSGesturePriority.Low
                        if (winnerId == null || entry.priority.ordinal > currentWinnerPriority.ordinal) {
                            claimWinner(entry.id, entry.priority)
                        }
                    }
                }
            }
        }
    }

    /**
     * Called when pointer is lifted.
     */
    fun onPointerUp(uptimeMillis: Long): Map<Any, IOSGestureRelease> {
        pointerState.onUp(uptimeMillis)
        val results = mutableMapOf<Any, IOSGestureRelease>()

        entries.forEach { entry ->
            if (entry.recognizer.state == IOSGestureState.Accepted || entry.recognizer.state == IOSGestureState.Possible) {
                val release = entry.recognizer.onPointerUp(uptimeMillis, pointerState)
                results[entry.id] = release
            }
        }

        winnerId = null
        pointerState.reset()
        return results
    }

    /**
     * Explicitly claims ownership for a recognizer and rejects competing candidates.
     */
    fun claimWinner(recognizerId: Any, priority: IOSGesturePriority = IOSGesturePriority.Normal) {
        winnerId = recognizerId

        entries.forEach { entry ->
            if (entry.id != recognizerId) {
                if (entry.compatibility == IOSGestureCompatibility.Exclusive) {
                    // Reject if priority is lower or equal
                    if (entry.priority.ordinal <= priority.ordinal) {
                        entry.recognizer.onCancel()
                    }
                } else if (entry.compatibility == IOSGestureCompatibility.Passive) {
                    // Passive recognizers observe but don't block
                }
            }
        }
    }

    /**
     * Cancels all active recognizers and resets arena.
     */
    fun cancelAll() {
        entries.forEach { it.recognizer.onCancel() }
        winnerId = null
        pointerState.reset()
    }
}

/**
 * Creates and remembers an [IOSGestureArena].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSGestureArena(): IOSGestureArena {
    return remember { IOSGestureArena() }
}
