package dev.iosfeel.physics.detent

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlin.math.abs

/**
 * Universal position + velocity aware detent resolver.
 *
 * Used by sheets, tabs, segmented sliders, pickers, and scrubbers to determine
 * which discrete detent to snap to when user releases a drag gesture.
 */
@ExperimentalIOSFeelV2Api
object IOSDetentResolver {

    /**
     * Resolves the target detent given [position], [velocity], and the list of available [detents].
     *
     * @param position Current position / progress value.
     * @param velocity Release velocity in units/second.
     * @param detents Available discrete detents (must not be empty).
     * @param velocityThreshold Minimum velocity required to trigger a directional detent jump instead of nearest.
     */
    fun <T> resolve(
        position: Float,
        velocity: Float,
        detents: List<IOSDetent<T>>,
        velocityThreshold: Float = 0.5f
    ): IOSDetentDecision<T> {
        require(detents.isNotEmpty()) { "Cannot resolve detent from empty list" }

        val sorted = detents.sortedBy { it.value }

        // Forward flick
        if (velocity > velocityThreshold) {
            val forwardDetent = sorted.firstOrNull { it.value > position + 0.001f }
            if (forwardDetent != null) {
                return IOSDetentDecision(
                    target = forwardDetent,
                    reason = IOSDetentDecisionReason.VelocityForward
                )
            }
        }

        // Backward flick
        if (velocity < -velocityThreshold) {
            val backwardDetent = sorted.lastOrNull { it.value < position - 0.001f }
            if (backwardDetent != null) {
                return IOSDetentDecision(
                    target = backwardDetent,
                    reason = IOSDetentDecisionReason.VelocityBackward
                )
            }
        }

        // Nearest by distance
        val nearest = sorted.minByOrNull { abs(it.value - position) } ?: sorted.first()
        return IOSDetentDecision(
            target = nearest,
            reason = IOSDetentDecisionReason.Nearest
        )
    }
}
