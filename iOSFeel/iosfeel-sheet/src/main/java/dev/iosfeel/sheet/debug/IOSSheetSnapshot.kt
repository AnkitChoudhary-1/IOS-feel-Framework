package dev.iosfeel.sheet.debug

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSMotionOwner
import dev.iosfeel.sheet.IOSSheetPhase
import dev.iosfeel.sheet.detent.IOSSheetDetent

/**
 * Telemetry snapshot of bottom sheet state for Developer Lab and performance monitoring.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSSheetSnapshot(
    val phase: IOSSheetPhase,
    val currentDetent: IOSSheetDetent,
    val targetDetent: IOSSheetDetent,
    val offset: Float,
    val velocity: Float,
    val owner: IOSMotionOwner,
    val timestampNs: Long = System.nanoTime()
)
