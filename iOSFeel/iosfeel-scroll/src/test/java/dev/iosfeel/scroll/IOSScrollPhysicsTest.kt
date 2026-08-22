package dev.iosfeel.scroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSScrollPhysicsTest {

    @Test
    fun scrollWithinBoundsConsumesDelta() {
        val state = IOSScrollState().apply {
            maxScroll = 1000f
            position = 200f
        }
        val config = IOSScrollConfig()

        val result = consumeIOSScrollDelta(
            state = state,
            delta = 50f,
            config = config
        )

        assertEquals(50f, result.consumed, 0.001f)
        assertEquals(0f, result.unconsumed, 0.001f)
        assertEquals(150f, state.position, 0.001f)
        assertEquals(0f, state.overscroll, 0.001f)
    }

    @Test
    fun scrollPastTopEngagesOverscroll() {
        val state = IOSScrollState().apply {
            maxScroll = 1000f
            position = 10f
        }
        val config = IOSScrollConfig()

        val result = consumeIOSScrollDelta(
            state = state,
            delta = 50f,
            config = config
        )

        assertEquals(50f, result.consumed, 0.001f)
        assertEquals(0f, state.position, 0.001f)
        assertTrue("Expected overscroll > 0 but was ${state.overscroll}", state.overscroll > 0f)
    }

    @Test
    fun velocityFallsOverTime() {
        val result = calculateDeceleratedVelocity(
            velocity = 5000f,
            deltaSeconds = 0.016f,
            decelerationRate = 3f
        )

        assertTrue(result < 5000f)
        assertTrue(result > 0f)
    }

    @Test
    fun fasterInitialVelocityTravelsFarther() {
        val slow = calculateFrameDisplacement(
            velocity = 500f,
            deltaSeconds = 0.016f
        )

        val fast = calculateFrameDisplacement(
            velocity = 5000f,
            deltaSeconds = 0.016f
        )

        assertTrue(fast > slow)
    }
}
