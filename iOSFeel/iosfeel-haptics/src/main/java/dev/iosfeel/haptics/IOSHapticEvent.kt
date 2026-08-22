package dev.iosfeel.haptics

sealed interface IOSHapticEvent {

    data object Selection : IOSHapticEvent

    data object GestureStart : IOSHapticEvent

    data object GestureEnd : IOSHapticEvent

    data object ThresholdActivated : IOSHapticEvent

    data object ThresholdDeactivated : IOSHapticEvent

    data class Impact(
        val strength: IOSImpact
    ) : IOSHapticEvent

    data class Notification(
        val type: IOSNotification
    ) : IOSHapticEvent
}
