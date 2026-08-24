package dev.iosfeel.sheet

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSMotionOwner
import dev.iosfeel.sheet.detent.IOSSheetDetent
import dev.iosfeel.sheet.detent.IOSSheetDetentResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class SheetV2Test {

    @Test
    fun `detent resolver calculates exact physical coordinates from semantic detents`() {
        val detents = listOf(IOSSheetDetent.Large, IOSSheetDetent.Medium, IOSSheetDetent.Hidden)
        val resolved = IOSSheetDetentResolver.resolveDetents(
            detents = detents,
            containerHeightPx = 1000f
        )

        val largeDetent = resolved.find { it.key == IOSSheetDetent.Large }
        val mediumDetent = resolved.find { it.key == IOSSheetDetent.Medium }
        val hiddenDetent = resolved.find { it.key == IOSSheetDetent.Hidden }

        assertEquals(80f, largeDetent?.value ?: 0f, 0.001f)
        assertEquals(450f, mediumDetent?.value ?: 0f, 0.001f)
        assertEquals(1000f, hiddenDetent?.value ?: 0f, 0.001f)
    }

    @Test
    fun `velocity-driven detent resolver picks next detent on strong upward flick`() {
        val detents = listOf(IOSSheetDetent.Large, IOSSheetDetent.Medium, IOSSheetDetent.Hidden)
        val resolved = IOSSheetDetentResolver.resolveDetents(detents, 1000f)

        // Current offset is 380px (closer to Medium=500 than Large=80)
        // With neutral velocity -> picks Medium
        val targetNeutral = IOSSheetDetentResolver.resolveTarget(
            offsetPx = 380f,
            velocityPxPerSec = 0f,
            resolvedDetents = resolved
        )
        assertEquals(IOSSheetDetent.Medium, targetNeutral.key)

        // With strong upward velocity (-1800 px/s) -> picks Large!
        val targetUpward = IOSSheetDetentResolver.resolveTarget(
            offsetPx = 380f,
            velocityPxPerSec = -1800f,
            resolvedDetents = resolved
        )
        assertEquals(IOSSheetDetent.Large, targetUpward.key)
    }

    @Test
    fun `re-grabbing settling sheet captures offset and changes owner without jump`() {
        val sheetState = IOSSheetState(
            initialDetent = IOSSheetDetent.Medium,
            detents = listOf(IOSSheetDetent.Medium, IOSSheetDetent.Large)
        )
        sheetState.containerHeightPx = 1000f

        // Simulate settling toward Large (80px), currently at 240px with -400px/s velocity
        sheetState.motion.state.update(
            value = 240f,
            velocity = -400f,
            target = 80f,
            owner = IOSMotionOwner.Spring
        )

        // User touches sheet surface mid-settle
        sheetState.acquireByUser()

        assertEquals(240f, sheetState.offset, 0.001f)
        assertEquals(-400f, sheetState.velocity, 0.001f)
        assertEquals(IOSMotionOwner.User, sheetState.motion.owner)
        assertEquals(IOSSheetPhase.Dragging, sheetState.phase)
    }
}
