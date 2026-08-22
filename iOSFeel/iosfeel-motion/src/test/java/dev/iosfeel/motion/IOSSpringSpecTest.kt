package dev.iosfeel.motion

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSSpringSpecTest {

    @Test
    fun smoothPresetContainsExpectedValues() {

        assertEquals(
            320f,
            IOSMotionPreset.Smooth.stiffness
        )

        assertEquals(
            0.82f,
            IOSMotionPreset.Smooth.dampingRatio
        )
    }

    @Test(
        expected = IllegalArgumentException::class
    )
    fun negativeStiffnessThrows() {

        IOSSpringSpec(
            stiffness = -1f,
            dampingRatio = 0.8f
        )
    }

    @Test(
        expected = IllegalArgumentException::class
    )
    fun negativeDampingThrows() {

        IOSSpringSpec(
            stiffness = 300f,
            dampingRatio = -0.5f
        )
    }
}
