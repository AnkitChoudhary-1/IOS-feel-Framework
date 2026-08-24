package dev.iosfeel.interaction.arena

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.LayoutDirection
import dev.iosfeel.interaction.gesture.IOSGestureDirection
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.recognizer.IOSDragRecognizer
import dev.iosfeel.interaction.recognizer.IOSEdgeSwipeRecognizer
import dev.iosfeel.interaction.recognizer.IOSReorderRecognizer
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class ArenaIntegrationTest {

    @Test
    fun `edge back vs vertical scroll arbitration`() {
        val arena = IOSGestureArena()
        var backWon = false
        var scrollWon = false

        val edgeBack = IOSEdgeSwipeRecognizer(
            id = "edge_back",
            edgeWidthPx = 48f,
            layoutDirection = LayoutDirection.Ltr,
            containerWidthPx = 1000f,
            priority = IOSGesturePriority.High,
            onSwipeStart = { backWon = true }
        )

        val verticalScroll = IOSDragRecognizer(
            id = "list_scroll",
            direction = IOSGestureDirection.Vertical,
            priority = IOSGesturePriority.Normal,
            slopPx = 16f,
            onDragStart = { scrollWon = true }
        )

        arena.register(edgeBack)
        arena.register(verticalScroll)

        // Scenario 1: Touch inside edge (x=20) and move mostly horizontally (dx=40, dy=5)
        arena.onPointerDown(Offset(20f, 300f), 1000L)
        arena.onPointerMove(Offset(60f, 305f), 1050L)

        assertEquals("edge_back", arena.winnerId)
        assertTrue(backWon)
        assertFalse(scrollWon)
        assertEquals(IOSGestureState.Accepted, edgeBack.state)
        assertEquals(IOSGestureState.Cancelled, verticalScroll.state)
    }

    @Test
    fun `touch inside edge but moving vertically lets scroll win and rejects back`() {
        val arena = IOSGestureArena()
        var backWon = false
        var scrollWon = false

        val edgeBack = IOSEdgeSwipeRecognizer(
            id = "edge_back",
            edgeWidthPx = 48f,
            layoutDirection = LayoutDirection.Ltr,
            containerWidthPx = 1000f,
            priority = IOSGesturePriority.High,
            onSwipeStart = { backWon = true }
        )

        val verticalScroll = IOSDragRecognizer(
            id = "list_scroll",
            direction = IOSGestureDirection.Vertical,
            priority = IOSGesturePriority.Normal,
            slopPx = 16f,
            onDragStart = { scrollWon = true }
        )

        arena.register(edgeBack)
        arena.register(verticalScroll)

        // Touch inside edge (x=20) but move vertically (dx=2, dy=45)
        arena.onPointerDown(Offset(20f, 300f), 1000L)
        arena.onPointerMove(Offset(22f, 345f), 1050L)

        assertEquals("list_scroll", arena.winnerId)
        assertTrue(scrollWon)
        assertFalse(backWon)
        assertEquals(IOSGestureState.Accepted, verticalScroll.state)
        assertEquals(IOSGestureState.Rejected, edgeBack.state)
    }

    @Test
    fun `horizontal seek slider inside sheet wins horizontal drag over sheet vertical drag`() {
        val arena = IOSGestureArena()
        var sliderWon = false
        var sheetWon = false

        val horizontalSlider = IOSDragRecognizer(
            id = "seek_slider",
            direction = IOSGestureDirection.Horizontal,
            priority = IOSGesturePriority.Normal,
            slopPx = 16f,
            onDragStart = { sliderWon = true }
        )

        val sheetDrag = IOSDragRecognizer(
            id = "sheet_drag",
            direction = IOSGestureDirection.Vertical,
            priority = IOSGesturePriority.Normal,
            slopPx = 16f,
            onDragStart = { sheetWon = true }
        )

        arena.register(horizontalSlider)
        arena.register(sheetDrag)

        // Touch inside slider region and drag horizontally (dx=35, dy=4)
        arena.onPointerDown(Offset(200f, 400f), 1000L)
        arena.onPointerMove(Offset(235f, 404f), 1050L)

        assertEquals("seek_slider", arena.winnerId)
        assertTrue(sliderWon)
        assertFalse(sheetWon)
        assertEquals(IOSGestureState.Accepted, horizontalSlider.state)
        assertEquals(IOSGestureState.Cancelled, sheetDrag.state)
    }
}
