package dev.iosfeel.physics.spring

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Standard calibrated spring presets for iOSFeel V2.
 *
 * Each preset corresponds to an authentic, tactile physical interaction type.
 */
@ExperimentalIOSFeelV2Api
object IOSSprings {

    /**
     * Fast, energetic spring for button presses and micro-interactions.
     */
    val Press = IOSSpringSpec(
        response = 0.20f,
        bounce = 0.10f
    )

    /**
     * Crisp, responsive spring for tab, segmented control, and item selection.
     */
    val Selection = IOSSpringSpec(
        response = 0.28f,
        bounce = 0.14f
    )

    /**
     * Smooth, fluid spring for screen navigation transitions and page sliding.
     */
    val Navigation = IOSSpringSpec(
        response = 0.42f,
        bounce = 0.05f
    )

    /**
     * Balanced, weighted spring for bottom sheets, modal overlays, and action sheets.
     */
    val Sheet = IOSSpringSpec(
        response = 0.46f,
        bounce = 0.06f
    )

    /**
     * Deep, rich spring for full-screen player expansion and large surface morphs.
     */
    val PlayerExpansion = IOSSpringSpec(
        response = 0.48f,
        bounce = 0.08f
    )

    /**
     * High-bounce spring for playful, dynamic elements.
     */
    val Bouncy = IOSSpringSpec(
        response = 0.35f,
        bounce = 0.25f
    )

    /**
     * Low-latency, critically-damped spring for rapid snaps with zero overshoot.
     */
    val Snappy = IOSSpringSpec(
        response = 0.24f,
        bounce = 0.00f
    )

    /**
     * Soft, luxurious spring for gentle ambient transitions.
     */
    val Gentle = IOSSpringSpec(
        response = 0.52f,
        bounce = 0.00f
    )
}
