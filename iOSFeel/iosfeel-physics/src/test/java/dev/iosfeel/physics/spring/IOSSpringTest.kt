package dev.iosfeel.physics.spring

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSSpringTest {

    @Test
    fun `spring conversion computes valid stiffness and dampingRatio`() {
        val spec = IOSSpringSpec(response = 0.42f, bounce = 0.10f)

        assertTrue(spec.naturalFrequency > 0f)
        assertTrue(spec.stiffness > 100f)
        assertEquals(0.90f, spec.dampingRatio, 0.001f)
    }

    @Test
    fun `critically damped spring has dampingRatio 1`() {
        val spec = IOSSpringSpec(response = 0.30f, bounce = 0.0f)
        assertEquals(1.0f, spec.dampingRatio, 0.001f)
    }

    @Test
    fun `physical factory constructor converts correctly`() {
        val original = IOSSpringSpec(response = 0.40f, bounce = 0.15f)
        val physical = IOSSpringSpec.physical(
            stiffness = original.stiffness,
            dampingRatio = original.dampingRatio
        )

        assertEquals(original.response, physical.response, 0.01f)
        assertEquals(original.bounce, physical.bounce, 0.01f)
    }

    @Test
    fun `reduced motion disables bounce`() {
        val spec = IOSSpringSpec(response = 0.48f, bounce = 0.20f)
        val reduced = spec.toReducedMotion()

        assertEquals(0f, reduced.bounce, 0.001f)
        assertTrue(reduced.response < spec.response)
    }

    @Test
    fun `all presets have valid parameters`() {
        val presets = listOf(
            IOSSprings.Press,
            IOSSprings.Selection,
            IOSSprings.Navigation,
            IOSSprings.Sheet,
            IOSSprings.PlayerExpansion,
            IOSSprings.Bouncy,
            IOSSprings.Snappy,
            IOSSprings.Gentle
        )

        presets.forEach { preset ->
            assertTrue(preset.response > 0f)
            assertTrue(preset.stiffness > 0f)
            assertTrue(preset.dampingRatio > 0f)
        }
    }
}
