package dev.iosfeel.interaction

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Universal interaction lifecycle phases across all iOSFeel V2 components.
 */
@ExperimentalIOSFeelV2Api
enum class IOSInteractionPhase {
    /**
     * Interaction is at rest; no gesture or pointer is active.
     */
    Idle,

    /**
     * Pointer is down, but gesture intention has not yet been resolved by the Arena.
     */
    Possible,

    /**
     * Component is actively compressed/pressed by user touch.
     */
    Pressed,

    /**
     * Long-press hold threshold has been met and accepted.
     */
    Held,

    /**
     * User is dragging the component (1D or 2D).
     */
    Dragging,

    /**
     * User is continuously scrubbing across a set of discrete detents.
     */
    Scrubbing,

    /**
     * User has released with high velocity; motion is decaying.
     */
    Flinging,

    /**
     * Motion is settling into a final physical target via spring simulation.
     */
    Settling,

    /**
     * Interaction was interrupted, cancelled, or stolen by a parent recognizer.
     */
    Cancelled
}
