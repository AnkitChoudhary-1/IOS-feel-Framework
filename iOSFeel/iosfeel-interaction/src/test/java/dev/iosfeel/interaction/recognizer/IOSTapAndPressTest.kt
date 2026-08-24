package dev.iosfeel.interaction.recognizer

import androidx.compose.ui.geometry.Offset
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.interaction.pointer.IOSPointerState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSTapAndPressTest {

    @Test
    fun `quick tap within slop succeeds and triggers callback`() {
        var tapClicked = false
        val tapRecognizer = IOSTapRecognizer(slopPx = 16f) {
            tapClicked = true
        }
        val pointerState = IOSPointerState()

        // Pointer Down
        pointerState.onDown(Offset(100f, 100f), 1000L)
        tapRecognizer.onPointerDown(Offset(100f, 100f), 1000L)
        assertEquals(IOSGestureState.Possible, tapRecognizer.state)

        // Slight movement within slop (5px)
        pointerState.onMove(Offset(103f, 104f), 1050L)
        tapRecognizer.onPointerMove(Offset(103f, 104f), Offset(3f, 4f), 1050L, pointerState)
        assertEquals(IOSGestureState.Possible, tapRecognizer.state)

        // Pointer Up
        pointerState.onUp(1100L)
        val release = tapRecognizer.onPointerUp(1100L, pointerState)

        assertEquals(IOSGestureState.Ended, tapRecognizer.state)
        assertTrue(tapClicked)
        assertFalse(release.cancelled)
    }

    @Test
    fun `tap is rejected if movement exceeds slop tolerance`() {
        var tapClicked = false
        val tapRecognizer = IOSTapRecognizer(slopPx = 16f) {
            tapClicked = true
        }
        val pointerState = IOSPointerState()

        pointerState.onDown(Offset(100f, 100f), 1000L)
        tapRecognizer.onPointerDown(Offset(100f, 100f), 1000L)

        // Drag 30px
        pointerState.onMove(Offset(130f, 100f), 1050L)
        tapRecognizer.onPointerMove(Offset(130f, 100f), Offset(30f, 0f), 1050L, pointerState)
        assertEquals(IOSGestureState.Rejected, tapRecognizer.state)

        pointerState.onUp(1100L)
        val release = tapRecognizer.onPointerUp(1100L, pointerState)

        assertFalse(tapClicked)
        assertTrue(release.cancelled)
    }

    @Test
    fun `press recognizer activates immediately and cancels when movement exceeds tolerance`() {
        var isPressed = false
        val pressRecognizer = IOSPressRecognizer(maxDistancePx = 24f) { pressed ->
            isPressed = pressed
        }
        val pointerState = IOSPointerState()

        pointerState.onDown(Offset(50f, 50f), 1000L)
        pressRecognizer.onPointerDown(Offset(50f, 50f), 1000L)
        assertTrue(isPressed)
        assertEquals(IOSGestureState.Accepted, pressRecognizer.state)

        // Move beyond 24px (35px)
        pointerState.onMove(Offset(50f, 85f), 1060L)
        pressRecognizer.onPointerMove(Offset(50f, 85f), Offset(0f, 35f), 1060L, pointerState)

        assertFalse(isPressed)
        assertEquals(IOSGestureState.Cancelled, pressRecognizer.state)
    }
}
