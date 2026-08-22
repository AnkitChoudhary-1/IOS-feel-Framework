package dev.iosfeel.components

import dev.iosfeel.components.slider.denormalizeSliderValue
import dev.iosfeel.components.slider.normalizeSliderValue
import dev.iosfeel.components.slider.snapToStep
import org.junit.Assert.assertEquals
import org.junit.Test

class IOSSliderMathTest {

    @Test
    fun normalizeValueInRange() {
        val range = 100f..200f
        assertEquals(0f, normalizeSliderValue(100f, range), 0.0001f)
        assertEquals(1f, normalizeSliderValue(200f, range), 0.0001f)
        assertEquals(0.5f, normalizeSliderValue(150f, range), 0.0001f)
    }

    @Test
    fun denormalizeValueInRange() {
        val range = 0f..100f
        assertEquals(0f, denormalizeSliderValue(0f, range), 0.0001f)
        assertEquals(100f, denormalizeSliderValue(1f, range), 0.0001f)
        assertEquals(25f, denormalizeSliderValue(0.25f, range), 0.0001f)
    }

    @Test
    fun stepSnappingMath() {
        val steps = 3 // 4 intervals: 0, 0.25, 0.5, 0.75, 1.0
        assertEquals(0.25f, snapToStep(0.22f, steps), 0.001f)
        assertEquals(0.50f, snapToStep(0.54f, steps), 0.001f)
        assertEquals(1.0f, snapToStep(0.95f, steps), 0.001f)
    }
}
