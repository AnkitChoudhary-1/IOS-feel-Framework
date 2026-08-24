package dev.iosfeel.navigation.debug

import androidx.compose.runtime.Immutable
import dev.iosfeel.navigation.transition.IOSNavigationDirection
import dev.iosfeel.navigation.transition.IOSNavigationTransitionSource
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSMotionOwner

/**
 * Debug telemetry snapshot of navigation state for Developer Lab and diagnostics.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSNavigationSnapshot(
    val progress: Float,
    val velocity: Float,
    val source: IOSNavigationTransitionSource,
    val direction: IOSNavigationDirection,
    val owner: IOSMotionOwner,
    val currentRoute: String,
    val backStackDepth: Int,
    val timestampNs: Long = System.nanoTime()
)
