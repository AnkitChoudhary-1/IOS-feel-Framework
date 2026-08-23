package dev.iosfeel.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSCoupledBounceSpecTest {

    @Test
    fun defaultCoupledBounceSpecHasValidSprings() {
        val spec = IOSCoupledBounceSpec()

        assertTrue(spec.primary.stiffness > 0f)
        assertTrue(spec.primary.dampingRatio > 0f)
        assertTrue(spec.reaction.stiffness > 0f)
        assertTrue(spec.reaction.dampingRatio > 0f)
        assertEquals(0.20f, spec.reactionStrength, 0.001f)
    }

    @Test
    fun customCoupledBounceSpecRetainsValues() {
        val primary = IOSSpringSpec(stiffness = 500f, dampingRatio = 0.70f)
        val reaction = IOSSpringSpec(stiffness = 350f, dampingRatio = 0.90f)
        val spec = IOSCoupledBounceSpec(
            primary = primary,
            reaction = reaction,
            reactionStrength = 0.35f
        )

        assertEquals(500f, spec.primary.stiffness, 0.001f)
        assertEquals(0.70f, spec.primary.dampingRatio, 0.001f)
        assertEquals(350f, spec.reaction.stiffness, 0.001f)
        assertEquals(0.90f, spec.reaction.dampingRatio, 0.001f)
        assertEquals(0.35f, spec.reactionStrength, 0.001f)
    }
}
