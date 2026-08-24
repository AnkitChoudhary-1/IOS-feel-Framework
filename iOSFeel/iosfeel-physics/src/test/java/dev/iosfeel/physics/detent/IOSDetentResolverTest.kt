package dev.iosfeel.physics.detent

import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSDetentResolverTest {

    private enum class Tab { Home, Library, Search, Settings }

    private val tabs = listOf(
        IOSDetent(value = 0.0f, key = Tab.Home),
        IOSDetent(value = 0.33f, key = Tab.Library),
        IOSDetent(value = 0.66f, key = Tab.Search),
        IOSDetent(value = 1.0f, key = Tab.Settings)
    )

    @Test
    fun `low velocity resolves to nearest detent`() {
        val decision = IOSDetentResolver.resolve(
            position = 0.30f,
            velocity = 0.05f,
            detents = tabs,
            velocityThreshold = 0.5f
        )

        assertEquals(Tab.Library, decision.target.key)
        assertEquals(IOSDetentDecisionReason.Nearest, decision.reason)
    }

    @Test
    fun `forward flick jumps to next higher detent`() {
        val decision = IOSDetentResolver.resolve(
            position = 0.30f,
            velocity = 1.5f,
            detents = tabs,
            velocityThreshold = 0.5f
        )

        assertEquals(Tab.Library, decision.target.key)
        assertEquals(IOSDetentDecisionReason.VelocityForward, decision.reason)
    }

    @Test
    fun `forward flick while at library jumps to search`() {
        val decision = IOSDetentResolver.resolve(
            position = 0.34f,
            velocity = 2.0f,
            detents = tabs,
            velocityThreshold = 0.5f
        )

        assertEquals(Tab.Search, decision.target.key)
        assertEquals(IOSDetentDecisionReason.VelocityForward, decision.reason)
    }

    @Test
    fun `backward flick jumps to previous lower detent`() {
        val decision = IOSDetentResolver.resolve(
            position = 0.70f,
            velocity = -1.8f,
            detents = tabs,
            velocityThreshold = 0.5f
        )

        assertEquals(Tab.Search, decision.target.key)
        assertEquals(IOSDetentDecisionReason.VelocityBackward, decision.reason)
    }

    @Test
    fun `forward flick at max detent stays at max detent`() {
        val decision = IOSDetentResolver.resolve(
            position = 1.0f,
            velocity = 3.0f,
            detents = tabs,
            velocityThreshold = 0.5f
        )

        assertEquals(Tab.Settings, decision.target.key)
        assertEquals(IOSDetentDecisionReason.Nearest, decision.reason)
    }

    @Test
    fun `backward flick at min detent stays at min detent`() {
        val decision = IOSDetentResolver.resolve(
            position = 0.0f,
            velocity = -3.0f,
            detents = tabs,
            velocityThreshold = 0.5f
        )

        assertEquals(Tab.Home, decision.target.key)
        assertEquals(IOSDetentDecisionReason.Nearest, decision.reason)
    }
}
