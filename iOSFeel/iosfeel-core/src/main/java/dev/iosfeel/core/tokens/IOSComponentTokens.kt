package dev.iosfeel.core.tokens

enum class IOSComponentState {
    Normal,
    Pressed,
    Disabled,
    Selected,
    Destructive
}

enum class IOSActionRole {
    Normal,
    Destructive
}

object IOSComponentAlpha {
    const val Enabled: Float = 1.0f
    const val Disabled: Float = 0.42f
    const val Secondary: Float = 0.62f
    const val Tertiary: Float = 0.38f
}
