package dev.iosfeel.sheet

import dev.iosfeel.sheet.detent.IOSSheetDetent
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
            detents = resolvedDetents,
            containerHeightPx = 1000f
        )
        assertTrue(nearMedium is IOSSheetTarget.Detent)
        assertEquals(IOSSheetDetent.Medium, (nearMedium as IOSSheetTarget.Detent).value.detent)

        val nearLarge = chooseSheetTarget(
            currentOffset = 100f,
            velocityY = 100f,
            detents = resolvedDetents,
            containerHeightPx = 1000f
        )
        assertTrue(nearLarge is IOSSheetTarget.Detent)
        assertEquals(IOSSheetDetent.Large, (nearLarge as IOSSheetTarget.Detent).value.detent)
    }

    @Test
    fun fastUpwardVelocityChoosesHigherDetent() {
        val target = chooseSheetTarget(
            currentOffset = 450f,
            velocityY = -1500f,
            detents = resolvedDetents,
            containerHeightPx = 1000f,
            velocityThreshold = 900f
        )
        assertTrue(target is IOSSheetTarget.Detent)
        assertEquals(IOSSheetDetent.Large, (target as IOSSheetTarget.Detent).value.detent)
    }

    @Test
    fun fastDownwardVelocityChoosesLowerDetent() {
        val target = chooseSheetTarget(
            currentOffset = 450f,
            velocityY = 1500f,
            detents = resolvedDetents,
            containerHeightPx = 1000f,
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
            containerHeightPx = 1000f,
            velocityThreshold = 900f,
            dismissible = true,
            dismissVelocityThreshold = 1800f
        )
        assertEquals(IOSSheetTarget.Dismiss, target)
    }
}
