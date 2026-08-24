package dev.iosfeel.physics.resistance

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSResistanceTest {

    @Test
    fun `zero raw distance returns zero displacement`() {
        val spec = IOSResistanceSpec.Standard
        assertEquals(0f, spec.apply(0f), 0.0001f)
    }

    @Test
    fun `positive and negative resistance is symmetric`() {
        val spec = IOSResistanceSpec.Standard
        val pos = spec.apply(150f)
        val neg = spec.apply(-150f)

        assertEquals(pos, -neg, 0.0001f)
    }

    @Test
    fun `resistance is progressively nonlinear`() {
        val spec = IOSResistanceSpec.Standard

        val r100 = spec.apply(100f)
        val r200 = spec.apply(200f)
        val r300 = spec.apply(300f)

        // Monotonically increasing
        assertTrue(r200 > r100)
        assertTrue(r300 > r200)

        // Sublinear ratio: (r200 / 200) < (r100 / 100)
        val ratio100 = r100 / 100f
        val ratio200 = r200 / 200f
        val ratio300 = r300 / 300f

        assertTrue("Expected ratio200 ($ratio200) < ratio100 ($ratio100)", ratio200 < ratio100)
        assertTrue("Expected ratio300 ($ratio300) < ratio200 ($ratio200)", ratio300 < ratio200)
    }

    @Test
    fun `maximumDistance clamps output`() {
        val spec = IOSResistanceSpec(factor = 1.0f, exponent = 1.0f, maximumDistance = 50f)
        assertEquals(50f, spec.apply(200f), 0.0001f)
        assertEquals(-50f, spec.apply(-200f), 0.0001f)
    }
}
