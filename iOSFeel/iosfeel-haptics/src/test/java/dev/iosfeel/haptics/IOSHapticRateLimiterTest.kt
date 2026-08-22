package dev.iosfeel.haptics

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSHapticRateLimiterTest {

    @Test
    fun repeatedEventIsRateLimited() {
        var time = 1000L

        val limiter = IOSHapticRateLimiter(
            minimumIntervalMs = 40L,
            clock = { time }
        )

        assertTrue(limiter.shouldPerform("selection"))

        time += 10

        assertFalse(limiter.shouldPerform("selection"))

        time += 50

        assertTrue(limiter.shouldPerform("selection"))
    }

    @Test
    fun differentEventsAreNotBlocked() {
        var time = 1000L

        val limiter = IOSHapticRateLimiter(
            minimumIntervalMs = 40L,
            clock = { time }
        )

        assertTrue(limiter.shouldPerform("selection"))
        assertTrue(limiter.shouldPerform("threshold"))
    }

    @Test
    fun resetClearsLastEvent() {
        var time = 1000L

        val limiter = IOSHapticRateLimiter(
            minimumIntervalMs = 40L,
            clock = { time }
        )

        assertTrue(limiter.shouldPerform("selection"))
        time += 5
        assertFalse(limiter.shouldPerform("selection"))

        limiter.reset()
        assertTrue(limiter.shouldPerform("selection"))
    }
}
