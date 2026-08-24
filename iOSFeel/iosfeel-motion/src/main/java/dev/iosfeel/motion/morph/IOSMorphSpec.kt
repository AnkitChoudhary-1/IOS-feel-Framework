package dev.iosfeel.motion.morph

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Geometric motion path between start and end morph coordinates.
 */
enum class IOSMotionPath {
    /** Straight linear spatial interpolation. */
    Linear,

    /** Natural gravitational arc path. */
    Arc
}

/**
 * Specification configuring spring physics, path geometry, and shape transforms for morphing surfaces.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSMorphSpec(
    val spring: IOSSpringSpec = IOSSprings.Selection,
    val path: IOSMotionPath = IOSMotionPath.Linear,
    val startCornerRadius: Dp = 18.dp,
    val endCornerRadius: Dp = 24.dp
)
