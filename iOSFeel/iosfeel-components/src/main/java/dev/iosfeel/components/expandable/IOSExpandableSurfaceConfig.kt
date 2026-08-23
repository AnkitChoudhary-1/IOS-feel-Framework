package dev.iosfeel.components.expandable

import androidx.compose.runtime.Immutable
import dev.iosfeel.motion.IOSMotionPreset
import dev.iosfeel.motion.IOSSpringSpec

@Immutable
data class IOSExpandableSurfaceConfig(
    val expansionThreshold: Float = 0.5f,
    val velocityThreshold: Float = 1.15f,
    val springSpec: IOSSpringSpec = IOSMotionPreset.PlayerExpansion
)
