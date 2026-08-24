package dev.iosfeel.sheet

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sheet.detent.IOSSheetDetent
import dev.iosfeel.sheet.detent.IOSSheetDetentResolver
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSSheetDetentTest {

    @Test
    fun resolveSheetDetentsSortsByOffset() {
        val containerHeight = 1000f
        val detents = listOf(
            IOSSheetDetent.Compact,
            IOSSheetDetent.Medium,
            IOSSheetDetent.Large
        )

        val resolved = IOSSheetDetentResolver.resolveDetents(detents, containerHeight)

        assertEquals(3, resolved.size)
        // Large is highest (lowest offset = 80px)
        assertEquals(IOSSheetDetent.Large, resolved[0].key)
        assertEquals(80f, resolved[0].value, 0.001f)

        // Medium is middle (offset = 450px)
        assertEquals(IOSSheetDetent.Medium, resolved[1].key)
        assertEquals(450f, resolved[1].value, 0.001f)

        // Compact is lowest (highest offset = 780px)
        assertEquals(IOSSheetDetent.Compact, resolved[2].key)
        assertEquals(780f, resolved[2].value, 0.001f)
    }

    @Test
    fun customFractionDetentResolvesCorrectly() {
        val containerHeight = 1000f
        val detents = listOf(
            IOSSheetDetent.Fraction("custom_quarter", 0.25f)
        )

        val resolved = IOSSheetDetentResolver.resolveDetents(detents, containerHeight)
        assertEquals(1, resolved.size)
        assertEquals(750f, resolved[0].value, 0.001f)
    }
}
