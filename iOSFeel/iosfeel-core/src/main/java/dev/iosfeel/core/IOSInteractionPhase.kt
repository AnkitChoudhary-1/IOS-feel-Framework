package dev.iosfeel.core

/**
 * Represents the lifecycle phases of an interactive gesture or animation
 * within the iOSFeel framework.
 *
 * This is the shared interaction lifecycle used across all engines
 * (motion, gesture, haptics, navigation) to communicate state transitions.
 *
 * The typical flow is:
 * ```
 * Idle → Began → Changed → Completed
 *                        → Cancelled
 * ```
 */
enum class IOSInteractionPhase {

    /**
     * No interaction is in progress.
     * The element is at rest.
     */
    Idle,

    /**
     * An interaction has just started.
     * For example: the user's finger has touched down on a draggable element.
     */
    Began,

    /**
     * The interaction is actively in progress and state is changing.
     * For example: the user is dragging their finger.
     */
    Changed,

    /**
     * The interaction completed successfully.
     * For example: the user released their finger and the gesture
     * passed the completion threshold.
     */
    Completed,

    /**
     * The interaction was cancelled.
     * For example: the user released without sufficient velocity/progress,
     * or another gesture took priority.
     */
    Cancelled
}
