package dev.iosfeel.sheet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSSheetDetentTest {

    @Test
    fun resolveSheetDetentsSortsByOffset() {
        val containerHeight = 1000f
        val detents = listOf(
            IOSSheetDetent.Compact,
            IOSSheetDetent.Medium,
            IOSSheetDetent.Large
        )

        val resolved = resolveSheetDetents(containerHeight, detents)

        assertEquals(3, resolved.size)
        // Large is highest (lowest offset = 80px)
        assertEquals(IOSSheetDetent.Large, resolved[0].detent)
        assertEquals(80f, resolved[0].offsetPx, 0.001f)

        // Medium is middle (offset = 450px)
        assertEquals(IOSSheetDetent.Medium, resolved[1].detent)
        assertEquals(450f, resolved[1].offsetPx, 0.001f)

        // Compact is lowest (highest offset = 780px)
        assertEquals(IOSSheetDetent.Compact, resolved[2].detent)
        assertEquals(780f, resolved[2].offsetPx, 0.001f)
    }

    @Test
    fun customFractionDetentResolvesCorrectly() {
        val containerHeight = 1000f
        val detents = listOf(
            IOSSheetDetent.Fraction("custom_quarter", 0.25f)
        )

        val resolved = resolveSheetDetents(containerHeight, detents)
        assertEquals(1, resolved.size)
        assertEquals(750f, resolved[0].offsetPx, 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidFractionThrowsException() {
        IOSSheetDetent.Fraction("invalid", 1.5f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyDetentsListThrowsException() {
        resolveSheetDetents(1000f, emptyList())
    }
}
