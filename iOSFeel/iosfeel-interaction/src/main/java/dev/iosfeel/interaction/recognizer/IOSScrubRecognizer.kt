package dev.iosfeel.interaction.recognizer

import androidx.compose.ui.geometry.Offset
import dev.iosfeel.interaction.gesture.IOSGestureCompatibility
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureRecognizer
import dev.iosfeel.interaction.gesture.IOSGestureRelease
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.pointer.IOSPointerState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.detent.IOSDetent
import dev.iosfeel.physics.detent.IOSDetentResolver
import java.util.UUID

/**
 * Recognizer mapping continuous pointer translation across a series of discrete detents.
 * Emits live candidate updates and detent-change events.
 */
@ExperimentalIOSFeelV2Api
class IOSScrubRecognizer<T>(
    override val id: Any = "ScrubRecognizer_${UUID.randomUUID()}",
    val detents: List<IOSDetent<T>>,
    val normalizePosition: (Offset) -> Float,
    override val priority: IOSGesturePriority = IOSGesturePriority.High,
    val onCandidateChanged: ((IOSDetent<T>) -> Unit)? = null,
    val onScrubEnd: ((IOSDetent<T>, IOSGestureRelease) -> Unit)? = null
) : IOSGestureRecognizer {

    override var state: IOSGestureState = IOSGestureState.Possible
        private set

    override val compatibility: IOSGestureCompatibility = IOSGestureCompatibility.Exclusive

    /**
     * Currently highlighted candidate detent.
     */
    var currentCandidate: IOSDetent<T>? = null
        private set

    override fun onPointerDown(position: Offset, uptimeMillis: Long) {
        state = IOSGestureState.Accepted
        val normalized = normalizePosition(position)
        val decision = IOSDetentResolver.resolve(normalized, 0f, detents)
        currentCandidate = decision.target
        onCandidateChanged?.invoke(decision.target)
    }

    override fun onPointerMove(position: Offset, delta: Offset, uptimeMillis: Long, pointerState: IOSPointerState) {
        if (state == IOSGestureState.Accepted) {
            val normalized = normalizePosition(position)
            val decision = IOSDetentResolver.resolve(normalized, 0f, detents)
            if (decision.target != currentCandidate) {
                currentCandidate = decision.target
                onCandidateChanged?.invoke(decision.target)
            }
        }
    }

    override fun onPointerUp(uptimeMillis: Long, pointerState: IOSPointerState): IOSGestureRelease {
        val wasAccepted = state == IOSGestureState.Accepted
        state = if (wasAccepted) IOSGestureState.Ended else IOSGestureState.Rejected

        val normalized = normalizePosition(pointerState.currentPosition)
        val decision = IOSDetentResolver.resolve(normalized, pointerState.velocity.x, detents)
        val finalCandidate = decision.target

        val release = IOSGestureRelease(
            position = pointerState.currentPosition,
            translation = pointerState.translation,
            velocity = pointerState.velocity,
            cancelled = !wasAccepted
        )

        if (wasAccepted) {
            onScrubEnd?.invoke(finalCandidate, release)
        }
        return release
    }

    override fun onCancel() {
        state = IOSGestureState.Cancelled
        currentCandidate = null
    }
}
