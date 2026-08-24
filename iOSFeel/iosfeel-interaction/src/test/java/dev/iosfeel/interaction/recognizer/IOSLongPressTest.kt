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
class IOSLongPressTest {

    @Test
    fun `long press triggers hold after duration is exceeded`() {
        var holdFired = false
        val longPressRecognizer = IOSLongPressRecognizer(durationMillis = 300L, movementTolerancePx = 16f) {
            holdFired = true
        }
        val pointerState = IOSPointerState()

        pointerState.onDown(Offset(100f, 100f), 1000L)
        longPressRecognizer.onPointerDown(Offset(100f, 100f), 1000L)
        assertEquals(IOSGestureState.Possible, longPressRecognizer.state)

        // At 200ms -> not fired
        pointerState.onMove(Offset(102f, 102f), 1200L)
        longPressRecognizer.onPointerMove(Offset(102f, 102f), Offset(2f, 2f), 1200L, pointerState)
        assertFalse(holdFired)
        assertEquals(IOSGestureState.Possible, longPressRecognizer.state)

        // At 305ms -> fired
        pointerState.onMove(Offset(102f, 102f), 1305L)
        longPressRecognizer.onPointerMove(Offset(102f, 102f), Offset(0f, 0f), 1305L, pointerState)
        assertTrue(holdFired)
        assertEquals(IOSGestureState.Accepted, longPressRecognizer.state)
    }

    @Test
    fun `long press is rejected if movement exceeds tolerance before duration`() {
        var holdFired = false
        val longPressRecognizer = IOSLongPressRecognizer(durationMillis = 300L, movementTolerancePx = 16f) {
            holdFired = true
        }
        val pointerState = IOSPointerState()

        pointerState.onDown(Offset(100f, 100f), 1000L)
        longPressRecognizer.onPointerDown(Offset(100f, 100f), 1000L)

        // Moved 25px at 150ms
        pointerState.onMove(Offset(125f, 100f), 1150L)
        longPressRecognizer.onPointerMove(Offset(125f, 100f), Offset(25f, 0f), 1150L, pointerState)
        assertEquals(IOSGestureState.Rejected, longPressRecognizer.state)

        // At 400ms -> should still be rejected and never fire
        pointerState.onMove(Offset(125f, 100f), 1400L)
        longPressRecognizer.onPointerMove(Offset(125f, 100f), Offset(0f, 0f), 1400L, pointerState)
        assertFalse(holdFired)
    }
}
