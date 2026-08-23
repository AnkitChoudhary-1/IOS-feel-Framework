package dev.iosfeel.components.floatingbar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.motion.IOSCoupledBounceSpec

/**
 * Configuration options for the physical iOS floating tab scrub interaction.
 *
 * @param enabled Whether scrubbing and hold-to-lift gesture is active.
 * @param longPressDurationMillis Milliseconds of holding required to lift the selector.
 * @param selectorLift Upward translation distance of the lifted selector capsule.
 * @param heldScale Scale factor of the lifted selector capsule when held.
 * @param pressedScale Initial micro-scale compression of selector on touch down.
 * @param barPressedScale Micro-scale compression of the main navbar pill when selector is lifted.
 * @param barReactionDistance Downward translation reaction of the navbar pill.
 * @param hapticDetents Whether to fire selection haptics when crossing tab boundaries.
 * @param verticalCancelDistance Drag distance away vertically that smoothly cancels the scrub.
 * @param coupledSpringSpec Coupled spring specs governing selector and navbar rebound physics.
 */
@Immutable
data class IOSFloatingTabScrubConfig(
    val enabled: Boolean = true,
    val longPressDurationMillis: Long = 250L,
    val selectorLift: Dp = 0.dp,
    val heldScale: Float = 1.08f,
    val pressedScale: Float = 0.94f,
    val barPressedScale: Float = 0.985f,
    val barReactionDistance: Dp = 2.dp,
    val hapticDetents: Boolean = true,
    val verticalCancelDistance: Dp = 64.dp,
    val coupledSpringSpec: IOSCoupledBounceSpec = IOSCoupledBounceSpec()
)
