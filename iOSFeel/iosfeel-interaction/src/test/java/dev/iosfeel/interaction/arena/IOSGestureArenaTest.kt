package dev.iosfeel.interaction.arena

import androidx.compose.ui.geometry.Offset
import dev.iosfeel.interaction.gesture.IOSGestureDirection
import dev.iosfeel.interaction.gesture.IOSGesturePriority
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.recognizer.IOSDragRecognizer
import dev.iosfeel.interaction.recognizer.IOSPressRecognizer
import dev.iosfeel.interaction.recognizer.IOSTapRecognizer
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSGestureArenaTest {

    @Test
    fun `quick tap wins arena over normal drag`() {
        val arena = IOSGestureArena()
        var tapFired = false
        var dragFired = false

        val tap = IOSTapRecognizer(id = "tap") { tapFired = true }
        val drag = IOSDragRecognizer(
            id = "drag",
            direction = IOSGestureDirection.Any,
            slopPx = 16f,
            onDragStart = { dragFired = true }
        )

        arena.register(tap)
        arena.register(drag)

        // Down
        arena.onPointerDown(Offset(50f, 50f), 1000L)
        // Up immediately within slop
        val results = arena.onPointerUp(1050L)

        assertTrue(tapFired)
        assertTrue(!dragFired)
        assertEquals(IOSGestureState.Ended, tap.state)
    }

    @Test
    fun `drag wins arena when movement exceeds slop, cancelling tap`() {
        val arena = IOSGestureArena()
        var tapFired = false
        var dragFired = false

        val tap = IOSTapRecognizer(id = "tap") { tapFired = true }
        val drag = IOSDragRecognizer(
            id = "drag",
            direction = IOSGestureDirection.Horizontal,
            slopPx = 16f,
            onDragStart = { dragFired = true }
        )

        arena.register(tap)
        arena.register(drag)

        arena.onPointerDown(Offset(50f, 50f), 1000L)

        // Drag 30px horizontally
        arena.onPointerMove(Offset(80f, 50f), 1050L)

        assertEquals("drag", arena.winnerId)
        assertEquals(IOSGestureState.Accepted, drag.state)
        assertEquals(IOSGestureState.Cancelled, tap.state)
        assertTrue(dragFired)
        assertTrue(!tapFired)
    }

    @Test
    fun `passive press recognizer stays active alongside exclusive winner`() {
        val arena = IOSGestureArena()
        var isPressed = false
        val press = IOSPressRecognizer(id = "press") { isPressed = it }
        val drag = IOSDragRecognizer(id = "drag", direction = IOSGestureDirection.Any, slopPx = 16f)

        arena.register(press)
        arena.register(drag)

        arena.onPointerDown(Offset(50f, 50f), 1000L)
        assertTrue(isPressed)

        // Move 20px
        arena.onPointerMove(Offset(70f, 50f), 1050L)
        assertEquals("drag", arena.winnerId)
        // Passive recognizer was not cancelled by exclusive winner
        assertTrue(isPressed)
    }
}
