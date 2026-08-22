package dev.iosfeel.haptics

class IOSHapticThreshold(
    private val threshold: Float
) {

    private var active = false

    fun update(
        value: Float,
        haptics: IOSHaptics
    ) {

        val newActive = value >= threshold

        if (newActive && !active) {
            haptics.perform(
                IOSHapticEvent.ThresholdActivated
            )
        }

        if (!newActive && active) {
            haptics.perform(
                IOSHapticEvent.ThresholdDeactivated
            )
        }

        active = newActive
    }

    fun reset() {
        active = false
    }
}
