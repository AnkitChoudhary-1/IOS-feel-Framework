package dev.iosfeel.haptics

interface IOSHaptics {

    fun selection()

    fun impact(
        strength: IOSImpact
    )

    fun notification(
        type: IOSNotification
    )

    fun perform(
        event: IOSHapticEvent
    )
}
