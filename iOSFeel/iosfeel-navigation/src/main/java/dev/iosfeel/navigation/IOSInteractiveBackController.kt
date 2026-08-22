package dev.iosfeel.navigation

import dev.iosfeel.gesture.IOSGestureAxisDirection
import dev.iosfeel.gesture.IOSGestureDecision
import dev.iosfeel.gesture.IOSGestureThresholds
import dev.iosfeel.gesture.decideDirectionalGestureCompletion
import kotlin.math.max

class IOSInteractiveBackController(
    private val transition: IOSBackTransitionState
) {

    private var startProgress = 0f

    suspend fun begin() {
        transition.beginInteractive()
        startProgress = transition.progress.value
    }

    suspend fun update(
        gestureProgress: Float,
        velocityPxPerSecond: Float,
        distancePx: Float
    ) {
        val mappedProgress = mapRegrabProgress(
            startProgress = startProgress,
            gestureProgress = gestureProgress
        )

        val normalizedVelocity = normalizeGestureVelocity(
            velocityPxPerSecond = velocityPxPerSecond,
            distancePx = max(distancePx, 1f)
        )

        transition.updateInteractive(
            progressValue = mappedProgress,
            progressVelocity = normalizedVelocity
        )
    }

    fun decide(
        velocityPxPerSecond: Float,
        distancePx: Float,
        thresholds: IOSGestureThresholds
    ): IOSGestureDecision {
        return decideDirectionalGestureCompletion(
            progress = transition.progress.value,
            velocity = velocityPxPerSecond,
            direction = IOSGestureAxisDirection.Positive,
            thresholds = thresholds
        )
    }
}
