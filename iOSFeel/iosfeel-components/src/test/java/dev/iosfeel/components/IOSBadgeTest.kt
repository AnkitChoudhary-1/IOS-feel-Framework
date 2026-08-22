package dev.iosfeel.components

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSBadgeTest {

    @Test
    fun badgeCountFormatting() {
        fun formatBadge(count: Int?): String? {
            return when {
                count == null -> null
                count > 99 -> "99+"
                else -> count.toString()
            }
        }

        assertEquals(null, formatBadge(null))
        assertEquals("5", formatBadge(5))
        assertEquals("99", formatBadge(99))
        assertEquals("99+", formatBadge(100))
        assertEquals("99+", formatBadge(500))
    }
}
