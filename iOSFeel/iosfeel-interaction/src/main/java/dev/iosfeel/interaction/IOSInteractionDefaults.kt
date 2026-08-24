package dev.iosfeel.interaction

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.resistance.IOSResistanceSpec

/**
 * Standard calibrated thresholds and defaults for the iOSFeel V2 interaction engine.
 */
@ExperimentalIOSFeelV2Api
object IOSInteractionDefaults {

    /**
     * Standard duration before a touch is promoted to a [IOSInteractionPhase.Held] state.
     */
    const val LongPressDurationMillis: Long = 300L

    /**
     * Fast hold threshold for swift scrubbing navigation (e.g. floating tab bar).
     */
    const val QuickHoldDurationMillis: Long = 200L

    /**
     * Intentional hold threshold for destructive or high-gravity actions (e.g. list reordering).
     */
    const val ReorderHoldDurationMillis: Long = 350L

    /**
     * Touch slop in pixels before directional determination begins.
     */
    const val TouchSlopPx: Float = 16f

    /**
     * Maximum distance the pointer can wander while holding before the hold is aborted.
     */
    val MovementTolerance: Dp = 10.dp

    /**
     * Maximum drag displacement allowed before a pending visual press is cleanly cancelled.
     */
    val PressCancellationDistance: Dp = 12.dp

    /**
     * Ratio threshold to lock axis direction ($|dx| > |dy| \times \text{DirectionBias}$).
     */
    const val DirectionBias: Float = 1.15f

    /**
     * Standard edge width for interactive back gestures.
     */
    val EdgeSwipeWidth: Dp = 24.dp

    /**
     * Standard resistance spec applied when scrubbing past the first or last detent.
     */
    val ScrubEdgeResistance = IOSResistanceSpec.Standard
}
