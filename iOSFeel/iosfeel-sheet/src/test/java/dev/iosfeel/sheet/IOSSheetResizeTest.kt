package dev.iosfeel.sheet

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sheet.detent.IOSSheetDetent
import dev.iosfeel.sheet.detent.IOSSheetDetentResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSSheetResizeTest {

    @Test
    fun detentRecalculatesAfterResize() {
        val phone = IOSSheetDetentResolver.resolveDetents(
            detents = listOf(IOSSheetDetent.Medium),
            containerHeightPx = 2400f
        ).first()

        val landscape = IOSSheetDetentResolver.resolveDetents(
            detents = listOf(IOSSheetDetent.Medium),
            containerHeightPx = 1000f
        ).first()

        assertNotEquals(phone.value, landscape.value)
        assertEquals(2400f * 0.45f, phone.value, 0.001f)
        assertEquals(1000f * 0.45f, landscape.value, 0.001f)
    }

    @Test
    fun findResolvedDetentFindsMatchingDetent() {
        val resolved = IOSSheetDetentResolver.resolveDetents(
            detents = listOf(
                IOSSheetDetent.Large,
                IOSSheetDetent.Medium,
                IOSSheetDetent.Compact
            ),
            containerHeightPx = 1000f
        )

        val found = resolved.find { it.key == IOSSheetDetent.Medium }
        assertEquals(IOSSheetDetent.Medium, found?.key)
        assertEquals(450f, found?.value ?: 0f, 0.001f)
    }
}
