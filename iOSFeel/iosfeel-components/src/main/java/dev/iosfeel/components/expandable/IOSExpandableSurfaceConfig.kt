package dev.iosfeel.components.expandable

import androidx.compose.runtime.Immutable
import dev.iosfeel.motion.IOSMotionPreset
import dev.iosfeel.motion.IOSSpringSpec

@Immutable
data class IOSExpandableSurfaceConfig(
    val expansionThreshold: Float = 0.38f,
    val velocityThreshold: Float = 0.40f,
    val springSpec: IOSSpringSpec = IOSMotionPreset.PlayerExpansion
)
