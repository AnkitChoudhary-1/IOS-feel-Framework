package dev.iosfeel.scroll

import org.junit.Assert.assertTrue
import org.junit.Test

class IOSScrollResistanceTest {

    @Test
    fun initialOverscrollMovesContent() {
        val config = IOSScrollConfig()

        val result = applyIOSScrollResistance(
            currentOverscroll = 0f,
            delta = 100f,
            config = config
        )

        assertTrue("Expected result > 0 but was $result", result > 0f)
        assertTrue("Expected result < 100 but was $result", result < 100f)
    }

    @Test
    fun resistanceGetsStrongerFartherFromBoundary() {
        val config = IOSScrollConfig()

        val near = applyIOSScrollResistance(
            currentOverscroll = 10f,
            delta = 50f,
            config = config
        ) - 10f

        val far = applyIOSScrollResistance(
            currentOverscroll = 180f,
            delta = 50f,
            config = config
        ) - 180f

        assertTrue("Expected far delta ($far) < near delta ($near)", far < near)
    }

    @Test
    fun overscrollIsCappedAtMaximum() {
        val config = IOSScrollConfig(maxOverscrollPx = 200f)

        val result = applyIOSScrollResistance(
            currentOverscroll = 199f,
            delta = 500f,
            config = config
        )

        assertTrue("Expected result <= 200 but was $result", result <= 200f)
    }

    @Test
    fun resistanceNeverBecomesNegative() {
        val config = IOSScrollConfig()

        val multiplier = calculateIOSResistanceMultiplier(
            overscroll = 10000f,
            config = config
        )

        assertTrue("Expected multiplier > 0 but was $multiplier", multiplier > 0f)
        assertTrue("Expected multiplier >= 0.05 but was $multiplier", multiplier >= 0.05f)
    }
}
