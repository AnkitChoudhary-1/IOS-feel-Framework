package dev.iosfeel.components

import dev.iosfeel.components.interaction.calculateIOSPressAlpha
import dev.iosfeel.components.interaction.calculateIOSPressScale
import org.junit.Assert.assertEquals
import org.junit.Test

class IOSPressMathTest {

    @Test
    fun pressScaleInterpolation() {
        // Zero progress gives 1.0
        assertEquals(1.0f, calculateIOSPressScale(0f, 0.975f), 0.0001f)

        // 1.0 progress gives target scale
        assertEquals(0.975f, calculateIOSPressScale(1f, 0.975f), 0.0001f)

        // 0.5 progress gives halfway
        assertEquals(0.9875f, calculateIOSPressScale(0.5f, 0.975f), 0.0001f)
    }

    @Test
    fun pressScaleBoundsCoercion() {
        // Negative progress coerced to 0f
        assertEquals(1.0f, calculateIOSPressScale(-0.5f, 0.975f), 0.0001f)

        // Greater than 1.0 progress coerced to 1.0
        assertEquals(0.975f, calculateIOSPressScale(1.5f, 0.975f), 0.0001f)
    }

    @Test
    fun pressAlphaInterpolation() {
        assertEquals(1.0f, calculateIOSPressAlpha(0f, 0.6f), 0.0001f)
        assertEquals(0.6f, calculateIOSPressAlpha(1f, 0.6f), 0.0001f)
        assertEquals(0.8f, calculateIOSPressAlpha(0.5f, 0.6f), 0.0001f)
    }
}
