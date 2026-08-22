package dev.iosfeel.navigation

enum class IOSNavigationTransitionType {
    None,
    Push,
    InteractivePop
}

sealed interface IOSNavigationEvent {

    data class Pushed(
        val entry: IOSNavigationEntry
    ) : IOSNavigationEvent

    data class Popped(
        val entry: IOSNavigationEntry
    ) : IOSNavigationEvent

    data object BackCancelled : IOSNavigationEvent
}
