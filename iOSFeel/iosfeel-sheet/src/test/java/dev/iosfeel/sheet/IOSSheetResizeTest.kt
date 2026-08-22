package dev.iosfeel.sheet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IOSSheetResizeTest {

    @Test
    fun detentRecalculatesAfterResize() {
        val phone = resolveSheetDetents(
            containerHeightPx = 2400f,
            detents = listOf(IOSSheetDetent.Medium)
        ).first()

        val landscape = resolveSheetDetents(
            containerHeightPx = 1000f,
            detents = listOf(IOSSheetDetent.Medium)
        ).first()

        assertNotEquals(phone.offsetPx, landscape.offsetPx)
        assertEquals(2400f * 0.45f, phone.offsetPx, 0.001f)
        assertEquals(1000f * 0.45f, landscape.offsetPx, 0.001f)
    }

    @Test
    fun findResolvedDetentFindsMatchingDetent() {
        val resolved = resolveSheetDetents(
            containerHeightPx = 1000f,
            detents = listOf(
                IOSSheetDetent.Large,
                IOSSheetDetent.Medium,
                IOSSheetDetent.Compact
            )
        )

        val found = findResolvedDetent(IOSSheetDetent.Medium, resolved)
        assertEquals(IOSSheetDetent.Medium, found?.detent)
        assertEquals(450f, found?.offsetPx ?: 0f, 0.001f)
    }
}
