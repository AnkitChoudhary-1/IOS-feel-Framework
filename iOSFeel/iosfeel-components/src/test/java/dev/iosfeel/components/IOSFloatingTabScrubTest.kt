package dev.iosfeel.components

import dev.iosfeel.components.floatingbar.IOSFloatingTabInteractionPhase
import dev.iosfeel.components.floatingbar.IOSFloatingTabScrubConfig
import dev.iosfeel.components.floatingbar.IOSFloatingTabScrubState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class IOSFloatingTabScrubTest {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    @Test
    fun initialStateIsIdleWithCorrectIndex() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 1,
            config = IOSFloatingTabScrubConfig(),
            scope = testScope
        )

        assertEquals(1, state.selectedIndex)
        assertEquals(1, state.hoveredIndex)
        assertEquals(IOSFloatingTabInteractionPhase.Idle, state.phase)
        assertEquals(0f, state.liftProgress, 0.001f)
        assertEquals(0f, state.barCompression, 0.001f)
    }

    @Test
    fun updatingTabBoundsPositionsDragX() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 2,
            config = IOSFloatingTabScrubConfig(),
            scope = testScope
        )

        state.updateTabBounds(0, 50f, 100f)
        state.updateTabBounds(1, 150f, 100f)
        state.updateTabBounds(2, 250f, 100f)
        state.updateTabBounds(3, 350f, 100f)

        assertEquals(250f, state.dragX, 0.001f)
    }

    @Test
    fun holdTriggerChangesPhaseToHeld() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 0,
            config = IOSFloatingTabScrubConfig(),
            scope = testScope
        )

        state.updateTabBounds(0, 50f, 100f)
        state.updateTabBounds(1, 150f, 100f)

        state.onHoldTriggered(haptics = null)
        assertEquals(IOSFloatingTabInteractionPhase.Held, state.phase)
    }

    @Test
    fun draggingHorizontalCalculatesClosestHoveredIndex() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 0,
            config = IOSFloatingTabScrubConfig(),
            scope = testScope
        )

        state.updateTabBounds(0, 50f, 100f)
        state.updateTabBounds(1, 150f, 100f)
        state.updateTabBounds(2, 250f, 100f)
        state.updateTabBounds(3, 350f, 100f)

        state.onHoldTriggered(haptics = null)

        // Drag to position near tab 2 (250f)
        state.onDrag(
            currentX = 240f,
            verticalDisplacementPx = 5f,
            maxVerticalCancelPx = 100f,
            haptics = null
        )

        assertEquals(IOSFloatingTabInteractionPhase.Scrubbing, state.phase)
        assertEquals(2, state.hoveredIndex)
        assertEquals(240f, state.dragX, 0.001f)
    }

    @Test
    fun draggingTooFarVerticallyCancelsScrub() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 1,
            config = IOSFloatingTabScrubConfig(),
            scope = testScope
        )

        state.updateTabBounds(0, 50f, 100f)
        state.updateTabBounds(1, 150f, 100f)

        state.onHoldTriggered(haptics = null)

        // Drag far vertically (120px with 100px max)
        state.onDrag(
            currentX = 50f,
            verticalDisplacementPx = 120f,
            maxVerticalCancelPx = 100f,
            haptics = null
        )

        // Hovered index reverts to original selected index on cancel
        assertEquals(1, state.hoveredIndex)
    }

    @Test
    fun releasingTriggersSelectionCallback() {
        val state = IOSFloatingTabScrubState(
            initialSelectedIndex = 0,
            config = IOSFloatingTabScrubConfig(),
            scope = testScope
        )

        state.updateTabBounds(0, 50f, 100f)
        state.updateTabBounds(1, 150f, 100f)
        state.updateTabBounds(2, 250f, 100f)

        state.onHoldTriggered(haptics = null)
        state.onDrag(
            currentX = 260f,
            verticalDisplacementPx = 0f,
            maxVerticalCancelPx = 100f,
            haptics = null
        )

        var selectedResult = -1
        state.onRelease { selectedResult = it }

        assertEquals(2, selectedResult)
        assertEquals(2, state.selectedIndex)
    }
}
