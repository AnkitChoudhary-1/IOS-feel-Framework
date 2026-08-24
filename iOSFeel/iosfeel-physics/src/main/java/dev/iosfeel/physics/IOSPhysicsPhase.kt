package dev.iosfeel.physics

/**
 * Universal physical interaction and animation phase across all iOSFeel V2 components.
 */
@ExperimentalIOSFeelV2Api
enum class IOSPhysicsPhase {
    /**
     * Motion is at rest and no gesture or animation is running.
     */
    Idle,

    /**
     * Motion is currently directly driven by user input (pointer / touch drag).
     */
    UserDriven,

    /**
     * Motion is undergoing inertial deceleration / decay (e.g., scroll fling).
     */
    Decaying,

    /**
     * Motion is actively being restored or driven by a spring simulation.
     */
    Springing,

    /**
     * Motion has reached within the settling threshold ($\epsilon$) and is finalizing.
     */
    Settling,

    /**
     * Motion was cancelled or interrupted before reaching target.
     */
    Cancelled
}
