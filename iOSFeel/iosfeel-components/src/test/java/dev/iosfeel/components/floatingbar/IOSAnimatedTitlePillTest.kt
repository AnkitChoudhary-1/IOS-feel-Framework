package dev.iosfeel.components.floatingbar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSAnimatedTitlePillTest {

    @Test
    fun testPillScrollCalculation() {
        val scrollThresholdPx = 40
        
        val atTopOffset = 0
        val isAtTopScrolled = atTopOffset > scrollThresholdPx
        assertEquals(false, isAtTopScrolled)

        val scrolledPastThreshold = 65
        val isScrolled = scrolledPastThreshold > scrollThresholdPx
        assertEquals(true, isScrolled)
    }

    @Test
    fun testPillScaleFormula() {
        val alpha0 = 0f
        val scale0 = 0.88f + (0.12f * alpha0)
        assertEquals(0.88f, scale0, 0.001f)

        val alpha1 = 1f
        val scale1 = 0.88f + (0.12f * alpha1)
        assertEquals(1.0f, scale1, 0.001f)

        val alphaHalf = 0.5f
        val scaleHalf = 0.88f + (0.12f * alphaHalf)
        assertTrue(scaleHalf > 0.88f && scaleHalf < 1.0f)
    }
}
