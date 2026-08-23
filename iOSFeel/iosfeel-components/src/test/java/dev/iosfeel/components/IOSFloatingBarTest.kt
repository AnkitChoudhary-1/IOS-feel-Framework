package dev.iosfeel.components

import dev.iosfeel.components.floatingbar.IOSFloatingBarDefaults
import dev.iosfeel.components.floatingbar.IOSFloatingBarState
import dev.iosfeel.components.floatingbar.IOSFloatingMaterialStyle
import dev.iosfeel.components.floatingbar.IOSFloatingShapes
import dev.iosfeel.material.IOSMaterialStyle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSFloatingBarTest {

    @Test
    fun floatingBarStateProgressAndFlags() = runBlocking {
        val state = IOSFloatingBarState(initialProgress = 0f)

        assertEquals(0f, state.progress, 0.001f)
        assertTrue(state.isExpanded)
        assertFalse(state.isCompact)

        state.snapTo(1f)
        assertEquals(1f, state.progress, 0.001f)
        assertFalse(state.isExpanded)
        assertTrue(state.isCompact)

        state.snapTo(0.5f)
        assertEquals(0.5f, state.progress, 0.001f)
        assertFalse(state.isExpanded)
        assertFalse(state.isCompact)
    }

    @Test
    fun materialStyleMapping() {
        assertEquals(IOSMaterialStyle.UltraThin, IOSFloatingMaterialStyle.Thin.toMaterialStyle())
        assertEquals(IOSMaterialStyle.Regular, IOSFloatingMaterialStyle.Regular.toMaterialStyle())
        assertEquals(IOSMaterialStyle.Thick, IOSFloatingMaterialStyle.Thick.toMaterialStyle())
    }

    @Test
    fun floatingShapesDefaultsNotNull() {
        org.junit.Assert.assertNotNull(IOSFloatingShapes.Bar)
        org.junit.Assert.assertNotNull(IOSFloatingShapes.Control)
        org.junit.Assert.assertNotNull(IOSFloatingShapes.Group)
        org.junit.Assert.assertNotNull(IOSFloatingBarDefaults.Shape)
    }
}
