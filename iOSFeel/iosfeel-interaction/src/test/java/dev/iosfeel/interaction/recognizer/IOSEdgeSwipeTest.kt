package dev.iosfeel.interaction.recognizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.LayoutDirection
import dev.iosfeel.interaction.arena.IOSGestureArena
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSEdgeSwipeTest {

    @Test
    fun `swipe starting outside edge boundary is immediately rejected`() {
        val edgeSwipe = IOSEdgeSwipeRecognizer(
            edgeWidthPx = 48f,
            layoutDirection = LayoutDirection.Ltr,
            containerWidthPx = 1000f
        )
        val arena = IOSGestureArena()
        arena.register(edgeSwipe)

        // Down at x=100px (outside 48px edge)
        arena.onPointerDown(Offset(100f, 200f), 1000L)

        assertEquals(IOSGestureState.Rejected, edgeSwipe.state)
    }

    @Test
    fun `swipe starting within edge boundary and moving right is accepted`() {
        var swipeStarted = false
        var currentProgress = 0f

        val edgeSwipe = IOSEdgeSwipeRecognizer(
            edgeWidthPx = 48f,
            layoutDirection = LayoutDirection.Ltr,
            containerWidthPx = 1000f,
            slopPx = 16f,
            onSwipeStart = { swipeStarted = true },
            onSwipeProgress = { p, _ -> currentProgress = p }
        )
        val arena = IOSGestureArena()
        arena.register(edgeSwipe)

        // Down at x=20px (inside 48px edge)
        arena.onPointerDown(Offset(20f, 200f), 1000L)
        assertEquals(IOSGestureState.Possible, edgeSwipe.state)

        // Swipe right 200px (dx = 200, dy = 5)
        arena.onPointerMove(Offset(220f, 205f), 1050L)

        assertEquals(IOSGestureState.Accepted, edgeSwipe.state)
        assertTrue(swipeStarted)
        assertEquals(0.20f, currentProgress, 0.01f)
    }
}
