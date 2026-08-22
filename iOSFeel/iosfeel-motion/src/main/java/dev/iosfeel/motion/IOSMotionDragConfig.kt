package dev.iosfeel.motion

data class IOSMotionDragConfig(
    val targetPosition: Float = 0f,
    val springSpec: IOSSpringSpec = IOSMotionPreset.Smooth,
    val bounds: IOSMotionBounds? = null,
    val enabled: Boolean = true,
    val onDragStarted: (() -> Unit)? = null,
    val onReleased: ((Float) -> Unit)? = null
)
