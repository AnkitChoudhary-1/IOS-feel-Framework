package dev.iosfeel.navigation

import dev.iosfeel.navigation.back.IOSBackGestureCoordinator
import dev.iosfeel.navigation.transition.IOSNavigationTransitionSource
import dev.iosfeel.navigation.transition.IOSNavigationTransitionState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSMotionOwner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class NavigationV2Test {

    @Test
    fun `decideBackTarget cancels on low progress and neutral velocity`() {
        val target = IOSBackGestureCoordinator.decideBackTarget(
            progress = 0.25f,
            velocity = 0.2f,
            threshold = 0.45f
        )
        assertEquals(0f, target, 0.001f)
    }

    @Test
    fun `decideBackTarget commits on high progress and neutral velocity`() {
        val target = IOSBackGestureCoordinator.decideBackTarget(
            progress = 0.60f,
            velocity = 0.1f,
            threshold = 0.45f
        )
        assertEquals(1f, target, 0.001f)
    }

    @Test
    fun `decideBackTarget commits on fast short flick even with low progress`() {
        // Fast forward flick (e.g. 18% progress with +2.4/s velocity)
        val target = IOSBackGestureCoordinator.decideBackTarget(
            progress = 0.18f,
            velocity = 2.4f,
            threshold = 0.45f,
            velocityThreshold = 1.1f
        )
        assertEquals(1f, target, 0.001f)
    }

    @Test
    fun `decideBackTarget cancels on strong backward velocity even with high progress`() {
        // Strong backward flick (e.g. 70% progress with -2.0/s velocity)
        val target = IOSBackGestureCoordinator.decideBackTarget(
            progress = 0.70f,
            velocity = -2.0f,
            threshold = 0.45f,
            velocityThreshold = 1.1f
        )
        assertEquals(0f, target, 0.001f)
    }

    @Test
    fun `interactive back gesture acquires user ownership and updates progress`() {
        val transition = IOSNavigationTransitionState(initialProgress = 0f)

        IOSBackGestureCoordinator.startBackGesture(transition, IOSNavigationTransitionSource.EdgeGesture)
        assertEquals(IOSMotionOwner.User, transition.motion.owner)
        assertEquals(IOSNavigationTransitionSource.EdgeGesture, transition.source)

        // Drag 300px on a 1000px container -> 0.3 progress
        IOSBackGestureCoordinator.updateBackGesture(
            transition = transition,
            dragDistancePx = 300f,
            containerWidthPx = 1000f,
            normalizedVelocity = 1.5f
        )

        assertEquals(0.3f, transition.progress, 0.001f)
        assertEquals(1.5f, transition.velocity, 0.001f)
    }

    @Test
    fun `re-grabbing during cancel spring preserves value without jump`() {
        val transition = IOSNavigationTransitionState(initialProgress = 0.40f)

        // Simulate cancel spring in flight at progress 0.28
        transition.motion.state.update(
            value = 0.28f,
            velocity = -1.2f,
            target = 0f,
            owner = IOSMotionOwner.Spring
        )

        // User touches again to re-grab
        transition.motion.acquireByUser()

        assertEquals(0.28f, transition.progress, 0.001f)
        assertEquals(-1.2f, transition.velocity, 0.001f)
        assertEquals(IOSMotionOwner.User, transition.motion.owner)
    }
}
