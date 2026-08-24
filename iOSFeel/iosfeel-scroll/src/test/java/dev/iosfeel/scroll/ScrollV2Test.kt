package dev.iosfeel.scroll

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSMotionOwner
import dev.iosfeel.physics.resistance.IOSResistanceSpec
import dev.iosfeel.scroll.overscroll.IOSOverscrollState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class ScrollV2Test {

    @Test
    fun `overscroll applies progressive nonlinear resistance`() {
        val state = IOSOverscrollState()

        // Pull 100px
        state.applyPullDelta(100f)
        val resisted100 = state.overscrollOffset
        assertTrue(resisted100 in 1f..99f)

        // Pull another 100px (total 200px)
        state.applyPullDelta(100f)
        val resisted200 = state.overscrollOffset

        // Second 100px displacement must produce smaller marginal increase than first 100px (diminishing return)
        val firstDelta = resisted100
        val secondDelta = resisted200 - resisted100
        assertTrue(secondDelta < firstDelta)
        assertEquals(IOSScrollPhase.Overscrolling, state.phase)
    }

    @Test
    fun `re-grabbing overscroll mid-flight captures displacement with zero jump`() {
        val state = IOSOverscrollState()

        // Simulate returning spring at 45px displacement
        state.motion.state.update(
            value = 45f,
            velocity = -250f,
            target = 0f,
            owner = IOSMotionOwner.Spring
        )

        // User re-touches the list during elastic return
        state.acquire()

        assertEquals(45f, state.overscrollOffset, 0.001f)
        assertEquals(-250f, state.motion.state.velocity, 0.001f)
        assertEquals(IOSMotionOwner.User, state.motion.owner)
        assertEquals(IOSScrollPhase.Dragging, state.phase)
    }
}
