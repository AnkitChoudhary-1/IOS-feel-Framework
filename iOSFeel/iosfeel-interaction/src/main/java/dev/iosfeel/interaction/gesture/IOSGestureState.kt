package dev.iosfeel.interaction.gesture

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlin.math.abs

/**
 * Lifecycle state of an individual [IOSGestureRecognizer].
 */
@ExperimentalIOSFeelV2Api
enum class IOSGestureState {
    /**
     * Recognizer is evaluating whether incoming input matches its gesture criteria.
     */
    Possible,

    /**
     * Recognizer has claimed and won the gesture arena.
     */
    Accepted,

    /**
     * Recognizer lost arbitration or violated its gesture criteria.
     */
    Rejected,

    /**
     * Gesture completed successfully on pointer release.
     */
    Ended,

    /**
     * Gesture was aborted or cancelled by system/parent event.
     */
    Cancelled
}

/**
 * Priority assigned to a gesture recognizer in the [dev.iosfeel.interaction.arena.IOSGestureArena].
 */
@ExperimentalIOSFeelV2Api
enum class IOSGesturePriority {
    /**
     * Low priority (e.g. ambient background tap).
     */
    Low,

    /**
     * Normal priority for standard taps, rows, and scroll views.
     */
    Normal,

    /**
     * High priority for intentional controls (seek slider, reorder handle, edge-swipe back).
     */
    High,

    /**
     * Exclusive priority that immediately supersedes any other active candidates.
     */
    Exclusive
}

/**
 * Compatibility mode of a gesture recognizer in the arena.
 */
@ExperimentalIOSFeelV2Api
enum class IOSGestureCompatibility {
    /**
     * Exclusive ownership. Once accepted, all competing incompatible recognizers are rejected.
     */
    Exclusive,

    /**
     * Cooperative mode. Can run concurrently with other cooperative recognizers.
     */
    Cooperative,

    /**
     * Passive mode. Observes pointer input without claiming exclusive ownership (e.g. press compression).
     */
    Passive
}

/**
 * Axis constraint for directional gestures.
 */
@ExperimentalIOSFeelV2Api
enum class IOSGestureDirection {
    Horizontal,
    Vertical,
    Any
}

/**
 * Helper to compute directional confidences for gesture arbitration.
 */
@ExperimentalIOSFeelV2Api
object IOSDirectionConfidence {

    /**
     * Calculates the horizontal confidence in range [0f, 1f].
     * Returns 0.5f if movement is zero.
     */
    fun horizontalConfidence(dx: Float, dy: Float): Float {
        val absX = abs(dx)
        val absY = abs(dy)
        val sum = absX + absY
        return if (sum <= 0.001f) 0.5f else (absX / sum)
    }

    /**
     * Calculates the vertical confidence in range [0f, 1f].
     * Returns 0.5f if movement is zero.
     */
    fun verticalConfidence(dx: Float, dy: Float): Float {
        val absX = abs(dx)
        val absY = abs(dy)
        val sum = absX + absY
        return if (sum <= 0.001f) 0.5f else (absY / sum)
    }
}
