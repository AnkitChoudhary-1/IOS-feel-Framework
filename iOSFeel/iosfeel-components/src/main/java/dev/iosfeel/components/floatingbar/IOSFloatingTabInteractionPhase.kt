package dev.iosfeel.components.floatingbar

/**
 * Represents the distinct physical states of a floating tab bar interaction.
 */
enum class IOSFloatingTabInteractionPhase {
    /** Normal resting state. */
    Idle,

    /** Initial finger touch-down on active tab before long-press threshold. */
    Pressing,

    /** Selected tab has been grabbed and lifted above the navigation bar. */
    Held,

    /** User is actively scrubbing the lifted selector across destination tabs. */
    Scrubbing,

    /** Selector is springing down and settling into the target or original tab. */
    Settling
}
