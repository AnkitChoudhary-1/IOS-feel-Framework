package dev.iosfeel.physics.interruption

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.IOSPhysicsPhase
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSInterruptibleMotionTest {

    @Test
    fun `initial motion state is Idle and owned by None`() {
        val motion = IOSInterruptibleMotion(initialValue = 0.5f)

        assertEquals(0.5f, motion.value, 0.0001f)
        assertEquals(0f, motion.velocity, 0.0001f)
        assertEquals(IOSPhysicsPhase.Idle, motion.phase)
        assertEquals(IOSMotionOwner.None, motion.owner)
    }

    @Test
    fun `acquireByUser claims ownership with zero positional jump`() {
        val motion = IOSInterruptibleMotion(initialValue = 0.3f)

        // Simulate motion mid-flight
        motion.state.update(
            value = 0.65f,
            velocity = 1.42f,
            target = 1.0f,
            phase = IOSPhysicsPhase.Springing,
            owner = IOSMotionOwner.Spring
        )

        // User touches object mid-flight
        motion.acquireByUser()

        assertEquals(0.65f, motion.value, 0.0001f)
        assertEquals(1.42f, motion.velocity, 0.0001f)
        assertEquals(0.65f, motion.target, 0.0001f)
        assertEquals(IOSPhysicsPhase.UserDriven, motion.phase)
        assertEquals(IOSMotionOwner.User, motion.owner)
    }

    @Test
    fun `dragTo updates value and velocity during gesture`() {
        val motion = IOSInterruptibleMotion(initialValue = 0f)
        motion.acquireByUser()

        motion.dragTo(value = 0.75f, velocity = 2.5f)

        assertEquals(0.75f, motion.value, 0.0001f)
        assertEquals(2.5f, motion.velocity, 0.0001f)
        assertEquals(IOSPhysicsPhase.UserDriven, motion.phase)
        assertEquals(IOSMotionOwner.User, motion.owner)
    }

    @Test
    fun `cancel resets phase and ownership to Idle and None`() {
        val motion = IOSInterruptibleMotion(initialValue = 0.4f)
        motion.dragTo(value = 0.8f, velocity = 1.0f)

        motion.cancel()

        assertEquals(0.8f, motion.value, 0.0001f)
        assertEquals(0f, motion.velocity, 0.0001f)
        assertEquals(IOSPhysicsPhase.Idle, motion.phase)
        assertEquals(IOSMotionOwner.None, motion.owner)
    }
}
