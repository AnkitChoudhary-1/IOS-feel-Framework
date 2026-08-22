package dev.iosfeel.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSNavigationStateTest {

    @Test
    fun stackPushIncreasesSizeAndUpdatesCurrent() {
        val state = IOSNavigationState(
            initialEntries = listOf(IOSNavigationEntry("home", "home_route"))
        )

        assertEquals(1, state.size)
        assertEquals("home", state.current.key)
        assertNull(state.previous)
        assertFalse(state.canGoBack)

        state.push(IOSNavigationEntry("profile", "profile_route"))

        assertEquals(2, state.size)
        assertEquals("profile", state.current.key)
        assertEquals("home", state.previous?.key)
        assertTrue(state.canGoBack)
    }

    @Test
    fun stackPopDecreasesSizeAndReturnsPopped() {
        val state = IOSNavigationState(
            initialEntries = listOf(
                IOSNavigationEntry("home", "home_route"),
                IOSNavigationEntry("profile", "profile_route")
            )
        )

        val popped = state.pop()
        assertEquals("profile", popped?.key)
        assertEquals(1, state.size)
        assertEquals("home", state.current.key)
        assertFalse(state.canGoBack)
    }

    @Test
    fun cannotPopRootReturnsNull() {
        val state = IOSNavigationState(
            initialEntries = listOf(IOSNavigationEntry("home", "home_route"))
        )

        val popped = state.pop()
        assertNull(popped)
        assertEquals(1, state.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateKeysAreRejected() {
        val state = IOSNavigationState(
            initialEntries = listOf(IOSNavigationEntry("home", "home_route"))
        )

        state.push(IOSNavigationEntry("home", "home_route_2"))
    }

    @Test
    fun multiScreenStackTraversal() {
        val state = IOSNavigationState(
            initialEntries = listOf(IOSNavigationEntry("home", "home"))
        )

        state.push(IOSNavigationEntry("profile", "profile"))
        state.push(IOSNavigationEntry("post", "post"))
        state.push(IOSNavigationEntry("comments", "comments"))

        assertEquals(4, state.size)
        assertEquals("comments", state.current.key)
        assertEquals("post", state.previous?.key)

        state.pop()
        assertEquals("post", state.current.key)
        assertEquals("profile", state.previous?.key)

        state.pop()
        assertEquals("profile", state.current.key)
        assertEquals("home", state.previous?.key)
    }
}
