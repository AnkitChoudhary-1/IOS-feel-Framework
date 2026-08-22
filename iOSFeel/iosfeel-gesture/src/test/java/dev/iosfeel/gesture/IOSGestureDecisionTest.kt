package dev.iosfeel.gesture

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSGestureDecisionTest {

    @Test
    fun halfDistanceProducesHalfProgress() {
        assertEquals(
            0.5f,
            calculateGestureProgress(
                translation = 150f,
                distance = 300f
            ),
            0.001f
        )
    }

    @Test
    fun zeroDistanceProducesZeroProgress() {
        assertEquals(
            0f,
            calculateGestureProgress(
                translation = 150f,
                distance = 0f
            ),
            0.001f
        )
    }

    @Test
    fun overDistanceIsClampedToOne() {
        assertEquals(
            1f,
            calculateGestureProgress(
                translation = 500f,
                distance = 300f
            ),
            0.001f
        )
    }

    @Test
    fun largeProgressCompletesGesture() {
        val result = decideGestureCompletion(
            progress = 0.7f,
            velocity = 100f
        )

        assertEquals(
            IOSGestureDecision.Complete,
            result
        )
    }

    @Test
    fun smallProgressCancelsGesture() {
        val result = decideGestureCompletion(
            progress = 0.2f,
            velocity = 100f
        )

        assertEquals(
            IOSGestureDecision.Cancel,
            result
        )
    }

    @Test
    fun highVelocityCompletesShortGesture() {
        val result = decideGestureCompletion(
            progress = 0.15f,
            velocity = 1800f
        )

        assertEquals(
            IOSGestureDecision.Complete,
            result
        )
    }

    @Test
    fun oppositeVelocityDoesNotComplete() {
        val result = decideDirectionalGestureCompletion(
            progress = 0.1f,
            velocity = -2000f,
            direction = IOSGestureAxisDirection.Positive
        )

        assertEquals(
            IOSGestureDecision.Cancel,
            result
        )
    }

    @Test
    fun positiveDirectionalVelocityCompletes() {
        val result = decideDirectionalGestureCompletion(
            progress = 0.15f,
            velocity = 1500f,
            direction = IOSGestureAxisDirection.Positive
        )

        assertEquals(
            IOSGestureDecision.Complete,
            result
        )
    }
}
