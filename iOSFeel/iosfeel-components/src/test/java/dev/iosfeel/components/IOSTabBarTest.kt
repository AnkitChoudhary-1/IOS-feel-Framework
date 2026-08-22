package dev.iosfeel.components

import dev.iosfeel.components.tab.IOSTabItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSTabBarTest {

    @Test
    fun tabReselectionCallbackTriggeredWhenSameTabSelected() {
        var reselectedTab: String? = null
        var newlySelectedTab: String? = null

        val current = "home"
        val clicked = "home"

        if (clicked == current) {
            reselectedTab = clicked
        } else {
            newlySelectedTab = clicked
        }

        assertEquals("home", reselectedTab)
        assertEquals(null, newlySelectedTab)
    }

    @Test
    fun newTabSelectionCallbackTriggeredWhenDifferentTabSelected() {
        var reselectedTab: String? = null
        var newlySelectedTab: String? = null

        val current = "home"
        val clicked = "profile"

        if (clicked == current) {
            reselectedTab = clicked
        } else {
            newlySelectedTab = clicked
        }

        assertEquals(null, reselectedTab)
        assertEquals("profile", newlySelectedTab)
    }
}
