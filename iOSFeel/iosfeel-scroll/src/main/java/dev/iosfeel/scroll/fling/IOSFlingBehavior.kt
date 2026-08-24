package dev.iosfeel.scroll.fling

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.decay.IOSDecaySpec
import dev.iosfeel.scroll.IOSFlingResult
import dev.iosfeel.scroll.IOSScrollConfig
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

/**
 * High-fidelity iOS decay fling behavior for Compose Lazy lists and Scrollables.
 *
 * Simulates exponential friction decay, detecting edge boundaries and computing
 * exact remaining velocity for parent / sheet handoff.
 */
@ExperimentalIOSFeelV2Api
class IOSFlingBehavior(
    val config: IOSScrollConfig = IOSScrollConfig(),
    val decaySpec: IOSDecaySpec = IOSDecaySpec(),
    val onFlingFinished: ((IOSFlingResult) -> Unit)? = null
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        if (abs(initialVelocity) < config.minimumFlingVelocity) {
            val result = IOSFlingResult(consumedDistance = 0f, remainingVelocity = 0f)
            onFlingFinished?.invoke(result)
            return 0f
        }

        var currentVelocity = initialVelocity * config.velocityMultiplier
        var lastFrameTimeNanos = withFrameNanos { it }
        val frictionPerSec = -ln(decaySpec.friction.toDouble()).toFloat() * 60f
        var totalConsumed = 0f
        var remainingVelocity = 0f

        while (abs(currentVelocity) >= decaySpec.minimumVelocity) {
            val nowNanos = withFrameNanos { it }
            val dt = (nowNanos - lastFrameTimeNanos) / 1_000_000_000f
            lastFrameTimeNanos = nowNanos

            val decayFactor = decaySpec.friction.pow(dt * 60f)
            val vNext = currentVelocity * decayFactor
            val deltaDistance = (currentVelocity - vNext) / frictionPerSec
            currentVelocity = vNext

            val consumed = scrollBy(deltaDistance)
            totalConsumed += consumed

            // If consumed less than requested, boundary hit
            if (abs(consumed - deltaDistance) > 0.5f && abs(deltaDistance) > 0.5f) {
                remainingVelocity = currentVelocity
                break
            }
        }

        val finalResult = IOSFlingResult(
            consumedDistance = totalConsumed,
            remainingVelocity = remainingVelocity
        )
        onFlingFinished?.invoke(finalResult)
        return finalResult.remainingVelocity
    }
}

/**
 * Creates and remembers an [IOSFlingBehavior].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSFlingBehavior(
    config: IOSScrollConfig = IOSScrollConfig(),
    onFlingFinished: ((IOSFlingResult) -> Unit)? = null
): IOSFlingBehavior {
    return remember(config) {
        IOSFlingBehavior(
            config = config,
            onFlingFinished = onFlingFinished
        )
    }
}
