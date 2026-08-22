package dev.iosfeel.scroll

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import kotlin.math.abs

class IOSDecayFlingBehavior(
    private val decaySpec: DecayAnimationSpec<Float>,
    private val config: IOSScrollConfig = IOSScrollConfig(),
    private val observer: IOSFlingObserver? = null
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {
        val startingVelocity = initialVelocity * config.flingVelocityMultiplier

        if (abs(startingVelocity) < config.minimumFlingVelocity) {
            return startingVelocity
        }

        observer?.onFlingStarted(startingVelocity)

        var previousValue = 0f
        val animation = AnimationState(
            initialValue = 0f,
            initialVelocity = startingVelocity
        )

        animation.animateDecay(
            animationSpec = decaySpec
        ) {
            val delta = value - previousValue
            val consumed = scrollBy(delta)
            previousValue += consumed

            observer?.onFlingVelocityChanged(this.velocity)

            /*
             * If child could not consume the entire frame displacement,
             * we've hit a boundary or another nested participant has taken over.
             */
            if (abs(consumed - delta) > 0.5f) {
                cancelAnimation()
            }
        }

        observer?.onFlingEnded()

        /*
         * Whatever velocity remains is returned and made available to nested scroll.
         */
        return animation.velocity
    }
}
