package dev.iosfeel.physics.bounds

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSBoundsAndVelocityTest {

    @Test
    fun `normalizeVelocity with valid distance computes progress velocity per second`() {
        val norm = normalizeVelocity(velocityPxPerSecond = 1000f, distancePx = 500f)
        assertEquals(2.0f, norm, 0.0001f)
    }

    @Test
    fun `normalizeVelocity with zero or negative distance returns zero safely`() {
        assertEquals(0f, normalizeVelocity(1000f, 0f), 0.0001f)
        assertEquals(0f, normalizeVelocity(1000f, -100f), 0.0001f)
    }

    @Test
    fun `bounds containment check works`() {
        val bounds = IOSPhysicsBounds(min = 0f, max = 100f)

        assertTrue(bounds.contains(50f))
        assertTrue(bounds.contains(0f))
        assertTrue(bounds.contains(100f))
        assertFalse(bounds.contains(-1f))
        assertFalse(bounds.contains(101f))
    }
}
