package dev.iosfeel.physics.coupled

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSMotionOwner
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSCoupledMotionTest {

    @Test
    fun `drivePrimary calculates expected reaction displacement`() {
        val coupled = IOSCoupledMotionState()

        coupled.drivePrimary(value = 10f, velocity = 5f, strength = -0.20f)

        assertEquals(10f, coupled.primary.value, 0.001f)
        assertEquals(5f, coupled.primary.velocity, 0.001f)
        assertEquals(IOSMotionOwner.User, coupled.primary.owner)

        assertEquals(-2.0f, coupled.reaction.value, 0.001f)
        assertEquals(-1.0f, coupled.reaction.velocity, 0.001f)
    }

    @Test
    fun `snapTo resets both bodies to target values`() {
        val coupled = IOSCoupledMotionState()
        coupled.drivePrimary(value = 10f, velocity = 5f)

        coupled.snapTo(0f, 0f)

        assertEquals(0f, coupled.primary.value, 0.001f)
        assertEquals(0f, coupled.reaction.value, 0.001f)
        assertEquals(IOSMotionOwner.None, coupled.primary.owner)
        assertEquals(IOSMotionOwner.None, coupled.reaction.owner)
    }
}
