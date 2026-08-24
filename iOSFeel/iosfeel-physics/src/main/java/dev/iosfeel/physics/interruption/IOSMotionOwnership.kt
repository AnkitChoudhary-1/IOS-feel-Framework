package dev.iosfeel.physics.interruption

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Declares who currently owns an interactive component's physical motion.
 *
 * This ownership model enables instant velocity preservation and re-grabbing:
 * when the user touches an object in flight, ownership transfers immediately
 * from [Spring] or [Decay] to [User] with zero positional jump.
 */
@ExperimentalIOSFeelV2Api
enum class IOSMotionOwner {
    /**
     * No active driver owns the motion (at rest).
     */
    None,

    /**
     * Touch / pointer gesture currently owns and drives position directly.
     */
    User,

    /**
     * Active spring simulation owns the motion.
     */
    Spring,

    /**
     * Inertial friction decay owns the motion.
     */
    Decay,

    /**
     * Programmatic or system driver (e.g. Predictive Back, accessibility action) owns the motion.
     */
    System
}
