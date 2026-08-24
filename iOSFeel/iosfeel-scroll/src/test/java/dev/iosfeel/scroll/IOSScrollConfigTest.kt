package dev.iosfeel.scroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSScrollConfigTest {

    @Test
    fun defaultValuesAreValid() {
        val config = IOSScrollConfig()
        assertEquals(1.0f, config.flingVelocityMultiplier, 0.001f)
        assertEquals(220f, config.maxOverscrollPx, 0.001f)
        assertTrue(config.springStiffness > 0f)
        assertTrue(config.springDampingRatio >= 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidOverscrollDistanceFails() {
        IOSScrollConfig(maxOverscrollPx = -1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidFlingMultiplierFails() {
        IOSScrollConfig(velocityMultiplier = 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidStiffnessFails() {
        IOSScrollConfig(springStiffness = 0f)
    }
}
