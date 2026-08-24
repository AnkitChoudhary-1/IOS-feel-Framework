package dev.iosfeel.sheet

import androidx.compose.runtime.saveable.SaverScope
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sheet.detent.IOSSheetDetent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
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

        assertEquals(listOf("medium", true), saved)

        val restoredState = IOSSheetStateSaver.restore(saved ?: emptyList<Any>())
        assertEquals(IOSSheetDetent.Medium, restoredState?.currentDetent)
        assertTrue(restoredState?.isVisible == true)
    }

    @Test
    fun hiddenDetentCanBeSavedAndRestored() {
        val state = IOSSheetState(initialDetent = IOSSheetDetent.Hidden)
        val saved = with(IOSSheetStateSaver) {
            with(saverScope) {
                save(state)
            }
        }

        assertEquals(listOf("hidden", false), saved)

        val restoredState = IOSSheetStateSaver.restore(saved ?: emptyList<Any>())
        assertEquals(IOSSheetDetent.Hidden, restoredState?.currentDetent)
        assertFalse(restoredState?.isVisible == true)
    }
}
