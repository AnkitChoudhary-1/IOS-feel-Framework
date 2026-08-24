package dev.iosfeel.interaction.recognizer

import androidx.compose.ui.geometry.Offset
import dev.iosfeel.interaction.gesture.IOSDirectionConfidence
import dev.iosfeel.interaction.gesture.IOSGestureDirection
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.pointer.IOSPointerState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSDragAndDirectionTest {

    @Test
    fun `direction confidence calculates properly`() {
        val hConf = IOSDirectionConfidence.horizontalConfidence(dx = 80f, dy = 20f)
        assertEquals(0.80f, hConf, 0.001f)

        val vConf = IOSDirectionConfidence.verticalConfidence(dx = 10f, dy = 90f)
        assertEquals(0.90f, vConf, 0.001f)

        val equalConf = IOSDirectionConfidence.horizontalConfidence(dx = 50f, dy = 50f)
        assertEquals(0.50f, equalConf, 0.001f)
    }

    @Test
    fun `horizontal drag recognizer accepts on horizontal pull and rejects on vertical pull`() {
        var dragStarted = false
        val hDrag = IOSDragRecognizer(
            direction = IOSGestureDirection.Horizontal,
            slopPx = 16f,
            onDragStart = { dragStarted = true }
        )
        val pointerState = IOSPointerState()

        pointerState.onDown(Offset(100f, 100f), 1000L)
        hDrag.onPointerDown(Offset(100f, 100f), 1000L)

        // Move horizontally (dx = 30, dy = 5)
        pointerState.onMove(Offset(130f, 105f), 1050L)
        hDrag.onPointerMove(Offset(130f, 105f), Offset(30f, 5f), 1050L, pointerState)

        assertEquals(IOSGestureState.Accepted, hDrag.state)
        assertTrue(dragStarted)
    }

    @Test
    fun `horizontal drag recognizer rejects on mostly vertical motion`() {
        var dragStarted = false
        val hDrag = IOSDragRecognizer(
            direction = IOSGestureDirection.Horizontal,
            slopPx = 16f,
            onDragStart = { dragStarted = true }
        )
        val pointerState = IOSPointerState()

        pointerState.onDown(Offset(100f, 100f), 1000L)
        hDrag.onPointerDown(Offset(100f, 100f), 1000L)

        // Move vertically (dx = 4, dy = 40)
        pointerState.onMove(Offset(104f, 140f), 1050L)
        hDrag.onPointerMove(Offset(104f, 140f), Offset(4f, 40f), 1050L, pointerState)

        assertEquals(IOSGestureState.Rejected, hDrag.state)
        assertTrue(!dragStarted)
    }
}
