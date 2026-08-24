package dev.iosfeel.interaction.recognizer

import androidx.compose.ui.geometry.Offset
import dev.iosfeel.interaction.arena.IOSGestureArena
import dev.iosfeel.interaction.gesture.IOSGestureDirection
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSReorderVsScrollTest {

    @Test
    fun `immediate vertical movement lets list scroll win and rejects reorder`() {
        val arena = IOSGestureArena()
        var scrollStarted = false
        var reorderStarted = false

        val scroll = IOSDragRecognizer(
            id = "list_scroll",
            direction = IOSGestureDirection.Vertical,
            priority = IOSGesturePriority.Normal,
            slopPx = 16f,
            onDragStart = { scrollStarted = true }
        )

        val reorder = IOSReorderRecognizer(
            id = "item_reorder",
            holdDurationMillis = 300L,
            priority = IOSGesturePriority.High,
            onReorderStart = { reorderStarted = true }
        )

        arena.register(scroll)
        arena.register(reorder)

        arena.onPointerDown(Offset(100f, 100f), 1000L)

        // Move vertically 30px immediately at 1050L (before 300ms hold threshold)
        arena.onPointerMove(Offset(100f, 130f), 1050L)

        assertEquals("list_scroll", arena.winnerId)
        assertTrue(scrollStarted)
        assertFalse(reorderStarted)
        assertEquals(IOSGestureState.Rejected, reorder.state)
    }

    @Test
    fun `holding past duration activates reorder which claims high priority over scrolling`() {
        val arena = IOSGestureArena()
        var scrollStarted = false
        var reorderStarted = false

        val scroll = IOSDragRecognizer(
            id = "list_scroll",
            direction = IOSGestureDirection.Vertical,
            priority = IOSGesturePriority.Normal,
            slopPx = 16f,
            onDragStart = { scrollStarted = true }
        )

        val reorder = IOSReorderRecognizer(
            id = "item_reorder",
            holdDurationMillis = 300L,
            movementTolerancePx = 16f,
            priority = IOSGesturePriority.High,
            onReorderStart = { reorderStarted = true }
        )

        arena.register(scroll)
        arena.register(reorder)

        arena.onPointerDown(Offset(100f, 100f), 1000L)

        // Slight stationary movement past 300ms (at 1320L)
        arena.onPointerMove(Offset(102f, 102f), 1320L)

        assertTrue(reorder.isHoldActivated)
        assertEquals("item_reorder", arena.winnerId)
        assertTrue(reorderStarted)
        assertFalse(scrollStarted)
        assertEquals(IOSGestureState.Cancelled, scroll.state)
    }
}
