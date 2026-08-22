package dev.iosfeel.components

import dev.iosfeel.components.segmented.IOSSegmentedItem
import org.junit.Assert.assertEquals
import org.junit.Test

class IOSSegmentedTest {

    @Test
    fun segmentedItemsIndexLookup() {
        val items = listOf(
            IOSSegmentedItem("posts", "Posts"),
            IOSSegmentedItem("reels", "Reels"),
            IOSSegmentedItem("tagged", "Tagged")
        )

        val selectedIndex = items.indexOfFirst { it.value == "reels" }
        assertEquals(1, selectedIndex)

        val invalidIndex = items.indexOfFirst { it.value == "stories" }
        assertEquals(-1, invalidIndex)
    }

    @Test
    fun segmentedItemsFraction() {
        val count = 3
        val index = 1
        val fraction = index.toFloat() / (count - 1)
        assertEquals(0.5f, fraction, 0.001f)
    }
}
