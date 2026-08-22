package dev.iosfeel.haptics

data class IOSHapticPolicy(
    val enabled: Boolean = true,

    /**
     * Minimum gap between repeated identical
     * high-frequency events.
     */
    val minimumIntervalMs: Long = 35L,

    /**
     * Prefer Android semantic UI feedback
     * whenever an appropriate event exists.
     */
    val preferSystemFeedback: Boolean = true,

    /**
     * Allow richer vibrator compositions when
     * the hardware supports them.
     */
    val allowRichEffects: Boolean = true
)
