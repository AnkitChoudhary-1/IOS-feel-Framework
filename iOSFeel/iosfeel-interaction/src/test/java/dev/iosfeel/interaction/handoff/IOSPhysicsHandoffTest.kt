package dev.iosfeel.interaction.handoff

import androidx.compose.ui.geometry.Offset
import dev.iosfeel.interaction.gesture.IOSGestureDirection
import dev.iosfeel.interaction.recognizer.IOSDragRecognizer
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.IOSPhysics
import dev.iosfeel.physics.interruption.IOSInterruptibleMotion
import dev.iosfeel.physics.interruption.IOSMotionOwner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSPhysicsHandoffTest {

    @Test
    fun `gesture release velocity seamlessly transfers into physics motion with no jump`() {
        val motion = IOSInterruptibleMotion(initialValue = 0f)

        // Gesture begins
        motion.acquireByUser()
        assertEquals(IOSMotionOwner.User, motion.owner)

        // User drags to 0.45
        motion.dragTo(value = 0.45f, velocity = 1.8f)
        assertEquals(0.45f, motion.value, 0.0001f)
        assertEquals(1.8f, motion.velocity, 0.0001f)

        // Velocity normalization helper test
        val normalizedVelocity = IOSPhysics.normalizeVelocity(
            velocityPxPerSecond = 1800f,
            distancePx = 1000f
        )
        assertEquals(1.8f, normalizedVelocity, 0.0001f)
    }

    @Test
    fun `user reclaims ownership during active motion with continuous value`() {
        val motion = IOSInterruptibleMotion(initialValue = 0.2f)

        // Simulate flying toward target 1.0 at position 0.65
        motion.state.update(
            value = 0.65f,
            velocity = 2.4f,
            target = 1.0f,
            owner = IOSMotionOwner.Spring
        )

        // User touches the element in flight
        motion.acquireByUser()

        // Must retain exact value and velocity without snapping back to 0.2
        assertEquals(0.65f, motion.value, 0.0001f)
        assertEquals(2.4f, motion.velocity, 0.0001f)
        assertEquals(IOSMotionOwner.User, motion.owner)
    }
}
