package dev.iosfeel.motion

object IOSMotionPreset {

    val Snappy = IOSSpringSpec(
        stiffness = 520f,
        dampingRatio = 0.78f
    )

    val Smooth = IOSSpringSpec(
        stiffness = 320f,
        dampingRatio = 0.82f
    )

    val Gentle = IOSSpringSpec(
        stiffness = 180f,
        dampingRatio = 0.88f
    )

    val PlayerExpansion = IOSSpringSpec(
        stiffness = 330f,
        dampingRatio = 0.86f
    )
}
