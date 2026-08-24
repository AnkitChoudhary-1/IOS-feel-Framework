package dev.iosfeel.scroll.debug

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.scroll.IOSScrollPhase

/**
 * Telemetry snapshot of scroll engine metrics for Developer Lab and live diagnostics.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSScrollSnapshot(
    val phase: IOSScrollPhase,
    val velocity: Float,
    val overscrollOffset: Float,
    val consumedDelta: Float,
    val remainingDelta: Float,
    val remainingVelocity: Float,
    val timestampNs: Long = System.nanoTime()
)
