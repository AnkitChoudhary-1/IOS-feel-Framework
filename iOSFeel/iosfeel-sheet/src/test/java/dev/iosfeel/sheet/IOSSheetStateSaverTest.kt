package dev.iosfeel.sheet

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSSheetStateSaverTest {

    private val saverScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    @Test
    fun mediumDetentCanBeSavedAndRestored() {
        val state = IOSSheetState(initialDetent = IOSSheetDetent.Medium, initialVisible = true)
        val saved = with(IOSSheetStateSaver) {
            with(saverScope) {
                save(state)
            }
        }

        assertEquals(listOf("medium", true), saved)

        val restoredState = IOSSheetStateSaver.restore(saved ?: emptyList<Any>())
        assertEquals(IOSSheetDetent.Medium, restoredState?.currentDetent)
        assertTrue(restoredState?.visible == true)
    }

    @Test
    fun largeDetentCanBeSavedAndRestored() {
        val state = IOSSheetState(initialDetent = IOSSheetDetent.Large, initialVisible = false)
        val saved = with(IOSSheetStateSaver) {
            with(saverScope) {
                save(state)
            }
        }

        assertEquals(listOf("large", false), saved)

        val restoredState = IOSSheetStateSaver.restore(saved ?: emptyList<Any>())
        assertEquals(IOSSheetDetent.Large, restoredState?.currentDetent)
        assertFalse(restoredState?.visible == true)
    }

    @Test
    fun compactDetentCanBeSavedAndRestored() {
        val state = IOSSheetState(initialDetent = IOSSheetDetent.Compact, initialVisible = false)
        val saved = with(IOSSheetStateSaver) {
            with(saverScope) {
                save(state)
            }
        }

        assertEquals(listOf("compact", false), saved)

        val restoredState = IOSSheetStateSaver.restore(saved ?: emptyList<Any>())
        assertEquals(IOSSheetDetent.Compact, restoredState?.currentDetent)
        assertFalse(restoredState?.visible == true)
    }
}
