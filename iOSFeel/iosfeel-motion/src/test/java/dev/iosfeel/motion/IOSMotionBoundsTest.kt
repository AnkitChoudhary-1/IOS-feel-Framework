package dev.iosfeel.motion

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSMotionBoundsTest {

    @Test
    fun valueInsideBoundsIsUnchanged() {
        val bounds = IOSMotionBounds(
            min = -100f,
            max = 100f
        )

        assertEquals(
            50f,
            bounds.constrain(50f)
        )
    }

    @Test
    fun valueAboveMaximumIsClamped() {
        val bounds = IOSMotionBounds(
            min = -100f,
            max = 100f
        )

        assertEquals(
            100f,
            bounds.constrain(500f)
        )
    }

    @Test
    fun valueBelowMinimumIsClamped() {
        val bounds = IOSMotionBounds(
            min = -100f,
            max = 100f
        )

        assertEquals(
            -100f,
            bounds.constrain(-500f)
        )
    }
}
