package dev.iosfeel.components.floatingbar

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class FloatingTabBarV2Test {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @Test
    fun `tab scrub state updates bounds and syncs selected index`() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 0,
            scope = scope
        )

        state.updateTabBounds(0, centerPx = 50f, widthPx = 100f)
        state.updateTabBounds(1, centerPx = 150f, widthPx = 100f)
        state.updateTabBounds(2, centerPx = 250f, widthPx = 100f)

        assertEquals(0, state.selectedIndex)
        assertEquals(50f, state.dragX, 0.001f)

        state.syncSelectedIndex(1)
        assertEquals(1, state.selectedIndex)
        assertEquals(150f, state.dragX, 0.001f)
    }

    @Test
    fun `press down initiates pressing phase and selector scale compression`() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 0,
            scope = scope
        )
        state.updateTabBounds(0, centerPx = 50f, widthPx = 100f)
        state.updateTabBounds(1, centerPx = 150f, widthPx = 100f)

        state.onPressDown(0)
        assertEquals(IOSFloatingTabInteractionPhase.Pressing, state.phase)
    }

    @Test
    fun `hold triggered activates held phase and lift animation`() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 0,
            scope = scope
        )
        state.updateTabBounds(0, centerPx = 50f, widthPx = 100f)
        state.updateTabBounds(1, centerPx = 150f, widthPx = 100f)

        state.onHoldTriggered(haptics = null)
        assertEquals(IOSFloatingTabInteractionPhase.Held, state.phase)
    }

    @Test
    fun `horizontal drag scrubs selector across tabs`() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 0,
            scope = scope
        )
        state.updateTabBounds(0, centerPx = 50f, widthPx = 100f)
        state.updateTabBounds(1, centerPx = 150f, widthPx = 100f)
        state.updateTabBounds(2, centerPx = 250f, widthPx = 100f)

        state.onHoldTriggered(haptics = null)

        // Drag to x=160 (near tab 1)
        state.onDrag(
            currentX = 160f,
            verticalDisplacementPx = 0f,
            maxVerticalCancelPx = 100f,
            haptics = null
        )

        assertEquals(160f, state.dragX, 0.001f)
        assertEquals(1, state.hoveredIndex)
        assertEquals(IOSFloatingTabInteractionPhase.Scrubbing, state.phase)
    }

    @Test
    fun `release commits destination tab`() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 0,
            scope = scope
        )
        state.updateTabBounds(0, centerPx = 50f, widthPx = 100f)
        state.updateTabBounds(1, centerPx = 150f, widthPx = 100f)

        state.onHoldTriggered(haptics = null)
        state.onDrag(
            currentX = 145f,
            verticalDisplacementPx = 0f,
            maxVerticalCancelPx = 100f,
            haptics = null
        )

        var committed = -1
        state.onRelease { targetIndex ->
            committed = targetIndex
        }

        assertEquals(1, committed)
        assertEquals(1, state.selectedIndex)
    }
}
