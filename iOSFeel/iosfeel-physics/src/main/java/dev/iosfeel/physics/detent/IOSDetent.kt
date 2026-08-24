package dev.iosfeel.physics.detent

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * A discrete stopping target or step in a multi-position control or sheet.
 *
 * @param T Key or identifier associated with this detent (e.g., Tab, Step, SheetHeight).
 * @property value Numeric position of the detent.
 * @property key Domain identifier or semantic payload.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSDetent<T>(
    val value: Float,
    val key: T
) {
    init {
        require(!value.isNaN() && !value.isInfinite()) { "Detent value cannot be NaN or Infinite" }
    }
}

/**
 * The physical rationale behind selecting a target detent.
 */
@ExperimentalIOSFeelV2Api
enum class IOSDetentDecisionReason {
    /**
     * Selected because it is the closest detent by position with near-zero flick velocity.
     */
    Nearest,

    /**
     * Selected because a positive forward flick velocity surpassed the threshold.
     */
    VelocityForward,

    /**
     * Selected because a negative backward flick velocity surpassed the threshold.
     */
    VelocityBackward
}

/**
 * Complete decision metadata returned by [IOSDetentResolver].
 *
 * @property target The chosen [IOSDetent].
 * @property reason Why this detent was chosen.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSDetentDecision<T>(
    val target: IOSDetent<T>,
    val reason: IOSDetentDecisionReason
)
