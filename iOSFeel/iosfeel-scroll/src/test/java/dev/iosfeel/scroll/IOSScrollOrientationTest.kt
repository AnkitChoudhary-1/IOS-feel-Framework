package dev.iosfeel.scroll

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IOSScrollOrientationTest {

    @Test
    fun horizontalNestedConnectionConsumesXAxis() {
        val config = IOSScrollConfig()
        val state = IOSScrollInteractionState(config)
        val horizontalConnection = IOSScrollNestedConnection(
            state = state,
            orientation = IOSScrollOrientation.Horizontal
        )

        val result = horizontalConnection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(x = 50f, y = 0f),
            source = NestedScrollSource.UserInput
        )

        assertNotEquals(0f, result.x)
        assertEquals(0f, result.y, 0.001f)
    }

    @Test
    fun verticalNestedConnectionConsumesYAxis() {
        val config = IOSScrollConfig()
        val state = IOSScrollInteractionState(config)
        val verticalConnection = IOSScrollNestedConnection(
            state = state,
            orientation = IOSScrollOrientation.Vertical
        )

        val result = verticalConnection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(x = 0f, y = 50f),
            source = NestedScrollSource.UserInput
        )

        assertEquals(0f, result.x, 0.001f)
        assertNotEquals(0f, result.y)
    }
}
