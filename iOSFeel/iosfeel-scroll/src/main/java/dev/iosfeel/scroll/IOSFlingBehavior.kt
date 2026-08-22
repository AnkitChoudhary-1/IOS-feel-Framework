package dev.iosfeel.scroll

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import kotlin.math.abs

class IOSFlingBehavior(
    private val decaySpec: DecayAnimationSpec<Float> = exponentialDecay(),
    private val velocityMultiplier: Float = 1f
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {
        var velocity = initialVelocity * velocityMultiplier

        if (abs(velocity) < 1f) {
            return 0f
        }

        /*
         * Basic velocity decay return. Full dynamic decay integration follows in Phase 5B.
         */
        return velocity
    }
}
