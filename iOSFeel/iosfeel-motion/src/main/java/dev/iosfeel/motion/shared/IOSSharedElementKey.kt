package dev.iosfeel.motion.shared

/**
 * Type-safe key uniquely identifying a continuous shared element across screens.
 */
@JvmInline
value class IOSSharedElementKey(
    val value: String
)

/**
 * Lifecycle phase of a shared element transition.
 */
enum class IOSSharedElementPhase {
    Idle,
    WaitingForTarget,
    Transitioning,
    Settling
}
