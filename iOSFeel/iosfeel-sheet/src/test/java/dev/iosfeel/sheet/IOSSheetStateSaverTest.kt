package dev.iosfeel.sheet

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Test

class IOSSheetStateSaverTest {

    private val saverScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    @Test
    fun mediumDetentCanBeSavedAndRestored() {
        val state = IOSSheetState(initialDetent = IOSSheetDetent.Medium)
        val saved = with(IOSSheetStateSaver) {
            with(saverScope) {
                save(state)
            }
        }

        assertEquals("medium", saved)

        val restoredState = IOSSheetStateSaver.restore(saved ?: "")
        assertEquals(IOSSheetDetent.Medium, restoredState?.currentDetent)
    }

    @Test
    fun largeDetentCanBeSavedAndRestored() {
        val state = IOSSheetState(initialDetent = IOSSheetDetent.Large)
        val saved = with(IOSSheetStateSaver) {
            with(saverScope) {
                save(state)
            }
        }

        assertEquals("large", saved)

        val restoredState = IOSSheetStateSaver.restore(saved ?: "")
        assertEquals(IOSSheetDetent.Large, restoredState?.currentDetent)
    }

    @Test
    fun compactDetentCanBeSavedAndRestored() {
        val state = IOSSheetState(initialDetent = IOSSheetDetent.Compact)
        val saved = with(IOSSheetStateSaver) {
            with(saverScope) {
                save(state)
            }
        }

        assertEquals("compact", saved)

        val restoredState = IOSSheetStateSaver.restore(saved ?: "")
        assertEquals(IOSSheetDetent.Compact, restoredState?.currentDetent)
    }
}
