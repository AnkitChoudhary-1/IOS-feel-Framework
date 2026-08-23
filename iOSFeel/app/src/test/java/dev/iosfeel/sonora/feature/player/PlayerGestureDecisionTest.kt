package dev.iosfeel.sonora.feature.player

import dev.iosfeel.components.expandable.IOSExpandableSurfaceConfig
import dev.iosfeel.components.expandable.IOSExpandableSurfaceState
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerGestureDecisionTest {

    @Test
    fun lowProgressSlowReleaseCollapses() {
        val target = decidePlayerTarget(
            progress = 0.3f,
            velocity = 0.2f,
            config = PlayerGestureConfig()
        )
        assertEquals(0f, target, 0.001f)
    }

    @Test
    fun highProgressSlowReleaseExpands() {
        val target = decidePlayerTarget(
            progress = 0.65f,
            velocity = 0.1f,
            config = PlayerGestureConfig()
        )
        assertEquals(1f, target, 0.001f)
    }

    @Test
    fun fastUpwardFlickExpands() {
        val target = decidePlayerTarget(
            progress = 0.2f,
            velocity = 2.0f,
            config = PlayerGestureConfig()
        )
        assertEquals(1f, target, 0.001f)
    }

    @Test
    fun fastDownwardFlickCollapses() {
        val target = decidePlayerTarget(
            progress = 0.8f,
            velocity = -2.0f,
            config = PlayerGestureConfig()
        )
        assertEquals(0f, target, 0.001f)
    }

    @Test
    fun frameworkDecideTargetMatchesSonoraConfig() {
        val config = IOSExpandableSurfaceConfig(
            expansionThreshold = 0.5f,
            velocityThreshold = 1.15f
        )

        assertEquals(0f, IOSExpandableSurfaceState.decideTarget(0.3f, 0f, config), 0.001f)
        assertEquals(1f, IOSExpandableSurfaceState.decideTarget(0.7f, 0f, config), 0.001f)
        assertEquals(1f, IOSExpandableSurfaceState.decideTarget(0.1f, 1.5f, config), 0.001f)
        assertEquals(0f, IOSExpandableSurfaceState.decideTarget(0.9f, -1.5f, config), 0.001f)
    }
}
