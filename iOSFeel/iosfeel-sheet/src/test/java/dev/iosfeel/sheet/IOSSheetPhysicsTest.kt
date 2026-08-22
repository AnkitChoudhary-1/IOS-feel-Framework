package dev.iosfeel.sheet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSSheetPhysicsTest {

    private val resolvedDetents = listOf(
        IOSResolvedDetent(IOSSheetDetent.Large, 80f),
        IOSResolvedDetent(IOSSheetDetent.Medium, 450f),
        IOSResolvedDetent(IOSSheetDetent.Compact, 780f)
    )

    @Test
    fun slowReleaseChoosesNearestDetent() {
        val nearMedium = chooseSheetTarget(
            currentOffset = 440f,
            velocityY = 0f,
            detents = resolvedDetents
        )
        assertTrue(nearMedium is IOSSheetTarget.Detent)
        assertEquals(IOSSheetDetent.Medium, (nearMedium as IOSSheetTarget.Detent).value.detent)

        val nearLarge = chooseSheetTarget(
            currentOffset = 100f,
            velocityY = 100f,
            detents = resolvedDetents
        )
        assertTrue(nearLarge is IOSSheetTarget.Detent)
        assertEquals(IOSSheetDetent.Large, (nearLarge as IOSSheetTarget.Detent).value.detent)
    }

    @Test
    fun fastUpwardVelocityChoosesHigherDetent() {
        // At Medium offset, fast upward throw (-1500 px/s) should target Large
        val target = chooseSheetTarget(
            currentOffset = 450f,
            velocityY = -1500f,
            detents = resolvedDetents,
            velocityThreshold = 900f
        )
        assertTrue(target is IOSSheetTarget.Detent)
        assertEquals(IOSSheetDetent.Large, (target as IOSSheetTarget.Detent).value.detent)
    }

    @Test
    fun fastDownwardVelocityChoosesLowerDetent() {
        // At Medium offset, fast downward throw (+1500 px/s) should target Compact
        val target = chooseSheetTarget(
            currentOffset = 450f,
            velocityY = 1500f,
            detents = resolvedDetents,
            velocityThreshold = 900f
        )
        assertTrue(target is IOSSheetTarget.Detent)
        assertEquals(IOSSheetDetent.Compact, (target as IOSSheetTarget.Detent).value.detent)
    }

    @Test
    fun fastDownwardFlingAtLowestDetentDismisses() {
        val target = chooseSheetTarget(
            currentOffset = 780f,
            velocityY = 2500f,
            detents = resolvedDetents,
            velocityThreshold = 900f,
            dismissible = true,
            dismissVelocityThreshold = 1800f
        )
        assertEquals(IOSSheetTarget.Dismiss, target)
    }

    @Test
    fun downwardFlingFromMediumTargetsCompactFirst() {
        val target = chooseSheetTarget(
            currentOffset = 450f,
            velocityY = 2000f,
            detents = resolvedDetents,
            velocityThreshold = 900f,
            dismissible = true,
            dismissVelocityThreshold = 1800f
        )
        assertTrue(target is IOSSheetTarget.Detent)
        assertEquals(IOSSheetDetent.Compact, (target as IOSSheetTarget.Detent).value.detent)
    }

    @Test
    fun largeDetentProducesFullExpansion() {
        val progress = calculateSheetExpansionProgress(
            offset = 80f,
            minOffset = 80f,
            maxOffset = 780f
        )
        assertEquals(1f, progress, 0.001f)
    }

    @Test
    fun compactDetentProducesZeroExpansion() {
        val progress = calculateSheetExpansionProgress(
            offset = 780f,
            minOffset = 80f,
            maxOffset = 780f
        )
        assertEquals(0f, progress, 0.001f)
    }
}
