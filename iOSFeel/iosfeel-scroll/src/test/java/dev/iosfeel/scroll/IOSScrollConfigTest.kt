package dev.iosfeel.scroll

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSScrollConfigTest {

    @Test
    fun defaultValuesAreValid() {
        val config = IOSScrollConfig()
        assertEquals(1.0f, config.flingVelocityMultiplier, 0.001f)
        assertEquals(220f, config.maxOverscrollPx, 0.001f)
        assertEquals(300f, config.springStiffness, 0.001f)
        assertEquals(0.78f, config.springDampingRatio, 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidOverscrollDistanceFails() {
        IOSScrollConfig(maxOverscrollPx = -1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidFlingMultiplierFails() {
        IOSScrollConfig(flingVelocityMultiplier = 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidStiffnessFails() {
        IOSScrollConfig(springStiffness = 0f)
    }
}
